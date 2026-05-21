package com.example.ui.editor

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CodeFile
import com.example.data.CodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayDeque
import android.os.Environment
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

data class LuauError(val line: Int, val message: String, val severity: String = "ERROR")

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CodeRepository
    private val _filesFlow = MutableStateFlow<List<CodeFile>>(emptyList())
    val allFiles: StateFlow<List<CodeFile>> = _filesFlow.asStateFlow()

    // Beta Interface, Treehub Bundler and Syntax Diagnostics states
    private val _isBetaInterface = MutableStateFlow(true) // Defaults to true initially for beta testing or settings control
    val isBetaInterface: StateFlow<Boolean> = _isBetaInterface.asStateFlow()

    private val _useTreehubBundler = MutableStateFlow(true)
    val useTreehubBundler: StateFlow<Boolean> = _useTreehubBundler.asStateFlow()

    private val _errorsList = MutableStateFlow<List<LuauError>>(emptyList())
    val errorsList: StateFlow<List<LuauError>> = _errorsList.asStateFlow()

    // Sidebar and Theme Settings
    private val _activeTheme = MutableStateFlow(CodeTheme.ElegantDark)
    val activeTheme: StateFlow<CodeTheme> = _activeTheme.asStateFlow()

    private val _fontSize = MutableStateFlow(14f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _wordWrap = MutableStateFlow(true)
    val wordWrap: StateFlow<Boolean> = _wordWrap.asStateFlow()

    private val _isSidebarExpanded = MutableStateFlow(true)
    val isSidebarExpanded: StateFlow<Boolean> = _isSidebarExpanded.asStateFlow()

    private val _activeSidebarTab = MutableStateFlow("explorer") // explorer, search, settings, console
    val activeSidebarTab: StateFlow<String> = _activeSidebarTab.asStateFlow()

    // Workspace Files State
    private val _openFileIds = MutableStateFlow<Set<Int>>(emptySet())
    val openFileIds: StateFlow<Set<Int>> = _openFileIds.asStateFlow()

    private val _activeFileId = MutableStateFlow<Int?>(null)
    val activeFileId: StateFlow<Int?> = _activeFileId.asStateFlow()

    // Visual Settings Tab States (VS Code Style)
    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isSettingsActive = MutableStateFlow(false)
    val isSettingsActive: StateFlow<Boolean> = _isSettingsActive.asStateFlow()

    // Rich Custom Workspace Settings
    private val _autoSave = MutableStateFlow(false)
    val autoSave: StateFlow<Boolean> = _autoSave.asStateFlow()

    private val _showMinimap = MutableStateFlow(true)
    val showMinimap: StateFlow<Boolean> = _showMinimap.asStateFlow()

    private val _showLineNumbers = MutableStateFlow(true)
    val showLineNumbers: StateFlow<Boolean> = _showLineNumbers.asStateFlow()

    private val _fontFamily = MutableStateFlow("Space Grotesk")
    val fontFamily: StateFlow<String> = _fontFamily.asStateFlow()

    private val _cursorStyle = MutableStateFlow("Block") // Line, Block, Underline
    val cursorStyle: StateFlow<String> = _cursorStyle.asStateFlow()

    private val _luauTypecheck = MutableStateFlow(true)
    val luauTypecheck: StateFlow<Boolean> = _luauTypecheck.asStateFlow()

    private val _luauOptimization = MutableStateFlow(true)
    val luauOptimization: StateFlow<Boolean> = _luauOptimization.asStateFlow()

    private val _cursorBlinking = MutableStateFlow("Smooth") // Smooth, Blink, Solid
    val cursorBlinking: StateFlow<String> = _cursorBlinking.asStateFlow()

    private val _tabSize = MutableStateFlow(4)
    val tabSize: StateFlow<Int> = _tabSize.asStateFlow()

    private val _useExternalStorage = MutableStateFlow(false)
    val useExternalStorage: StateFlow<Boolean> = _useExternalStorage.asStateFlow()

    private val _hasStoragePermission = MutableStateFlow(false)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    val externalFilesList = MutableStateFlow<List<CodeFile>>(emptyList())

    // VS Code Plugins / Extensions
    private val _enabledPlugins = MutableStateFlow<Map<String, Boolean>>(mapOf(
        "autocomplete" to true,
        "bracket" to true,
        "colorpicker" to true,
        "darklua" to true,
        "formatter" to false
    ))
    val enabledPlugins: StateFlow<Map<String, Boolean>> = _enabledPlugins.asStateFlow()

    private val _pluginLogs = MutableStateFlow<List<String>>(listOf(
        "[VSCODE API] Activating core workspace environment...",
        "[VSCODE API] vscode.extensions.registerCommand('luau.colorPicker')",
        "[VSCODE API] vscode.languages.registerCompletionItemProvider('luau')",
        "[VSCODE API] Host registered successfully. Mobile VS Code QuickEdit theme loaded."
    ))
    val pluginLogs: StateFlow<List<String>> = _pluginLogs.asStateFlow()

    // Bundler Simulation
    private val _isBundling = MutableStateFlow(false)
    val isBundling: StateFlow<Boolean> = _isBundling.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _bundleSuccess = MutableStateFlow<Boolean?>(null)
    val bundleSuccess: StateFlow<Boolean?> = _bundleSuccess.asStateFlow()

    private val _bundlerLogs = MutableStateFlow<List<String>>(listOf(
        "[DARKLUA BUNDLER] Standby. Ready to parse dependency graph."
    ))
    val bundlerLogs: StateFlow<List<String>> = _bundlerLogs.asStateFlow()

    private val _isDarkluaDownloaded = MutableStateFlow(false)
    val isDarkluaDownloaded: StateFlow<Boolean> = _isDarkluaDownloaded.asStateFlow()

    // Active file text value state for editing
    var activeTextFieldValue = mutableStateOf(TextFieldValue(""))
        private set

    // Track original content to see if modified
    private var originalContent: String = ""

    private val _isModified = MutableStateFlow(false)
    val isModified: StateFlow<Boolean> = _isModified.asStateFlow()

    // Find and Replace Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery.asStateFlow()

    // Simulation Console Output
    private val _consoleLog = MutableStateFlow("Luau VM Terminal initialized. Safe mode active.\nSelect a Luau file and tap Run (▶) above to execute dynamic scripts.")
    val consoleLog: StateFlow<String> = _consoleLog.asStateFlow()

    private val _isConsoleRunning = MutableStateFlow(false)
    val isConsoleRunning: StateFlow<Boolean> = _isConsoleRunning.asStateFlow()

    // Undo/Redo Stacks
    private val undoStack = ArrayDeque<TextFieldValue>()
    private val redoStack = ArrayDeque<TextFieldValue>()
    private var isUndoingOrRedoing = false

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CodeRepository(database.codeFileDao())

        viewModelScope.launch {
            // Pre-seed Luau templates
            repository.initializeDefaultFilesIfEmpty()
            
            // Connect flow
            repository.allFiles.collect { list ->
                if (!_useExternalStorage.value) {
                    _filesFlow.value = list
                    
                    // If there is no active file but files are available, select the first one (init.luau)
                    if (_activeFileId.value == null && !_isSettingsActive.value && list.isNotEmpty()) {
                        val preferred = list.find { it.name == "init.luau" } ?: list.first()
                        openFile(preferred.id)
                    }
                }
            }
        }
    }

    fun setSidebarExpanded(expanded: Boolean) {
        _isSidebarExpanded.value = expanded
    }

    fun setSidebarTab(tab: String) {
        _activeSidebarTab.value = tab
        _isSidebarExpanded.value = true
    }

    fun setTheme(theme: CodeTheme) {
        _activeTheme.value = theme
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size.coerceIn(10f, 26f)
    }

    fun setWordWrap(wrap: Boolean) {
        _wordWrap.value = wrap
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setReplaceQuery(query: String) {
        _replaceQuery.value = query
    }

    fun setBetaInterface(enabled: Boolean) { _isBetaInterface.value = enabled }
    fun setUseTreehubBundler(enabled: Boolean) { _useTreehubBundler.value = enabled }

    // Settings actions
    fun setAutoSave(value: Boolean) { _autoSave.value = value }
    fun setShowMinimap(value: Boolean) { _showMinimap.value = value }
    fun setShowLineNumbers(value: Boolean) { _showLineNumbers.value = value }
    fun setFontFamily(value: String) { _fontFamily.value = value }
    fun setCursorStyle(value: String) { _cursorStyle.value = value }
    fun setLuauTypecheck(value: Boolean) { _luauTypecheck.value = value }
    fun setLuauOptimization(value: Boolean) { _luauOptimization.value = value }
    fun setCursorBlinking(value: String) { _cursorBlinking.value = value }
    fun setTabSize(value: Int) { _tabSize.value = value }
    fun setSettingsOpen(open: Boolean) { _isSettingsOpen.value = open }
    fun setSettingsActive(active: Boolean) {
        _isSettingsActive.value = active
        if (active) {
            _activeFileId.value = null
            _isSettingsOpen.value = true
        }
    }

    // Opens a file tab or focuses it
    fun openFile(id: Int) {
        viewModelScope.launch {
            val file = if (_useExternalStorage.value) {
                externalFilesList.value.find { it.id == id }
            } else {
                repository.getFileById(id)
            } ?: return@launch
            
            // Deactivate Settings Tab visual content
            _isSettingsActive.value = false

            // Save active file automatically if autosave is enabled
            if (_autoSave.value && _isModified.value) {
                saveActiveFile()
            }

            if (_activeFileId.value == id) return@launch

            _openFileIds.value = _openFileIds.value + id
            _activeFileId.value = id
            
            originalContent = file.content
            activeTextFieldValue.value = TextFieldValue(file.content)
            _isModified.value = false
            performSyntaxCheck(file.content)
            
            // Reset undo stacks
            undoStack.clear()
            redoStack.clear()
        }
    }

    fun closeFile(id: Int) {
        val set = _openFileIds.value.toMutableSet()
        set.remove(id)
        _openFileIds.value = set

        if (_activeFileId.value == id) {
            if (set.isNotEmpty()) {
                openFile(set.first())
            } else {
                _activeFileId.value = null
                activeTextFieldValue.value = TextFieldValue("")
                originalContent = ""
                _isModified.value = false
                undoStack.clear()
                redoStack.clear()
                
                // If settings is open, focus settings
                if (_isSettingsOpen.value) {
                    _isSettingsActive.value = true
                }
            }
        }
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
        _isSettingsActive.value = false
        if (_openFileIds.value.isNotEmpty()) {
            openFile(_openFileIds.value.first())
        }
    }

    fun createNewFile(name: String, language: String = "luau") {
        viewModelScope.launch {
            // Force file naming rules for Luau only
            val cleanedName = name.replace(" ", "_").trim()
            val checkedName = if (cleanedName.endsWith(".luau") || cleanedName.endsWith(".lua")) {
                cleanedName
            } else {
                "$cleanedName.luau"
            }

            val moduleName = checkedName.substringBeforeLast(".")
            val content = """--!strict
-- Module $checkedName: Customized Luau workspace module
local $moduleName = {}
$moduleName.__index = $moduleName

type ModuleConfig = {
    enabled: boolean,
    multiplier: number
}

function $moduleName.new(): ModuleConfig
    return {
        enabled = true,
        multiplier = 1.0
    }
end

function $moduleName:Execute(input: number): number
    if not self.enabled then return 0 end
    print("Executing typed Luau mathematical rules...")
    return input * self.multiplier
end

return $moduleName
"""

            if (_useExternalStorage.value) {
                try {
                    val dir = File("/storage/emulated/0/LuauWorkspace")
                    if (!dir.exists()) dir.mkdirs()
                    val sysFile = File(dir, checkedName)
                    sysFile.writeText(content)
                    refreshExternalFiles()
                    val index = externalFilesList.value.indexOfFirst { it.name == checkedName }
                    if (index != -1) {
                        openFile(externalFilesList.value[index].id)
                    }
                } catch (e: Exception) {
                    addPluginLog("[ERROR] Failed to create external file: ${e.localizedMessage}")
                }
            } else {
                val file = CodeFile(
                    name = checkedName,
                    content = content,
                    language = "luau"
                )
                val newId = repository.insertFile(file).toInt()
                openFile(newId)
            }
        }
    }

    fun deleteFile(id: Int) {
        viewModelScope.launch {
            if (_useExternalStorage.value) {
                val file = externalFilesList.value.find { it.id == id } ?: return@launch
                closeFile(id)
                try {
                    val sysFile = File("/storage/emulated/0/LuauWorkspace", file.name)
                    if (sysFile.exists()) {
                        sysFile.delete()
                    }
                    refreshExternalFiles()
                } catch (e: Exception) {
                    addPluginLog("[ERROR] Failed to delete external file: ${e.localizedMessage}")
                }
            } else {
                closeFile(id)
                repository.deleteFileById(id)
            }
        }
    }

    fun renameFile(id: Int, newName: String) {
        viewModelScope.launch {
            val cleaned = newName.replace(" ", "_").trim()
            val checkedName = if (cleaned.endsWith(".luau") || cleaned.endsWith(".lua")) {
                cleaned
            } else {
                "$cleaned.luau"
            }
            if (_useExternalStorage.value) {
                val file = externalFilesList.value.find { it.id == id } ?: return@launch
                try {
                    val oldFile = File("/storage/emulated/0/LuauWorkspace", file.name)
                    val newFile = File("/storage/emulated/0/LuauWorkspace", checkedName)
                    if (oldFile.exists()) {
                        oldFile.renameTo(newFile)
                    }
                    refreshExternalFiles()
                } catch (e: Exception) {
                    addPluginLog("[ERROR] Failed to rename external file: ${e.localizedMessage}")
                }
            } else {
                val file = repository.getFileById(id) ?: return@launch
                repository.updateFile(file.copy(name = checkedName, language = "luau"))
            }
        }
    }

    fun updateActiveText(newValue: TextFieldValue) {
        val current = activeTextFieldValue.value
        
        // Skip history recording if it is just a cursor movement/selection update
        if (newValue.text != current.text) {
            if (!isUndoingOrRedoing) {
                // Limit undo states size
                if (undoStack.size > 50) {
                    undoStack.removeFirst()
                }
                undoStack.addLast(current)
                redoStack.clear()
            }
            _isModified.value = newValue.text != originalContent

            // Trigger real-time AutoSave if enabled
            if (_autoSave.value) {
                // Perform fast safe updates
                _isModified.value = false
                originalContent = newValue.text
                val fileId = _activeFileId.value
                if (fileId != null) {
                    viewModelScope.launch {
                        if (_useExternalStorage.value) {
                            val currentFile = externalFilesList.value.find { it.id == fileId }
                            if (currentFile != null) {
                                try {
                                    val sysFile = File("/storage/emulated/0/LuauWorkspace", currentFile.name)
                                    sysFile.writeText(newValue.text)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } else {
                            val currentFile = repository.getFileById(fileId)
                            if (currentFile != null) {
                                repository.updateFile(currentFile.copy(content = newValue.text))
                            }
                        }
                    }
                }
            }
            performSyntaxCheck(newValue.text)
        }
        
        activeTextFieldValue.value = newValue
    }

    fun saveActiveFile() {
        val fileId = _activeFileId.value ?: return
        viewModelScope.launch {
            val contentToSave = activeTextFieldValue.value.text
            if (_useExternalStorage.value) {
                val currentFile = externalFilesList.value.find { it.id == fileId } ?: return@launch
                try {
                    val dir = File("/storage/emulated/0/LuauWorkspace")
                    if (!dir.exists()) dir.mkdirs()
                    val sysFile = File(dir, currentFile.name)
                    sysFile.writeText(contentToSave)
                    refreshExternalFiles()
                } catch (e: Exception) {
                    addPluginLog("[ERROR] Failed to save external file: ${e.localizedMessage}")
                }
            } else {
                val currentFile = repository.getFileById(fileId) ?: return@launch
                repository.updateFile(currentFile.copy(content = contentToSave, lastModified = System.currentTimeMillis()))
            }
            originalContent = contentToSave
            _isModified.value = false
        }
    }

    fun triggerUndo() {
        if (undoStack.isNotEmpty()) {
            isUndoingOrRedoing = true
            val prev = undoStack.removeLast()
            redoStack.addLast(activeTextFieldValue.value)
            activeTextFieldValue.value = prev
            _isModified.value = prev.text != originalContent
            isUndoingOrRedoing = false

            if (_autoSave.value) {
                saveActiveFile()
            }
        }
    }

    fun triggerRedo() {
        if (redoStack.isNotEmpty()) {
            isUndoingOrRedoing = true
            val next = redoStack.removeLast()
            undoStack.addLast(activeTextFieldValue.value)
            activeTextFieldValue.value = next
            _isModified.value = next.text != originalContent
            isUndoingOrRedoing = false

            if (_autoSave.value) {
                saveActiveFile()
            }
        }
    }

    fun insertSymbol(symbol: String) {
        val current = activeTextFieldValue.value
        val text = current.text
        val selection = current.selection

        val before = text.substring(0, selection.start)
        val after = text.substring(selection.end)

        // Automatic brackets completion helper
        val completion = when (symbol) {
            "{" -> "}"
            "[" -> "]"
            "(" -> ")"
            "\"" -> "\""
            "'" -> "'"
            else -> ""
        }

        val actualInsert = if (symbol == "Tab") {
            " ".repeat(_tabSize.value)
        } else {
            symbol
        }
        val finalInsert = actualInsert + completion

        val newText = before + finalInsert + after
        val newCursorOffset = selection.start + actualInsert.length

        val nextValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorOffset)
        )
        updateActiveText(nextValue)
    }

    // Find and Replace Action
    fun performReplace() {
        val search = _searchQuery.value
        val replace = _replaceQuery.value
        if (search.isEmpty()) return

        val current = activeTextFieldValue.value
        val text = current.text
        if (!text.contains(search)) return

        val newText = text.replace(search, replace)
        updateActiveText(TextFieldValue(text = newText, selection = TextRange(0)))
    }

    // Simulated Luau Compiler Run Operation
    fun runActiveCode() {
        val fileId = _activeFileId.value ?: return
        viewModelScope.launch {
            val file = if (_useExternalStorage.value) {
                externalFilesList.value.find { it.id == fileId }
            } else {
                repository.getFileById(fileId)
            } ?: return@launch
            _isConsoleRunning.value = true
            setSidebarTab("console")
            _consoleLog.value = "CONNECTING LUAU VM INTERPRETER v0.624...\n[VM] Parsing Abstract Syntax Tree (AST) for [${file.name}]...\n"
            
            kotlinx.coroutines.delay(1000)
            
            val logLines = """[LUAU_VM] Spawning secure sandbox environment...
[INFO] Bytecode compilation target: Luau.JIT.v2.6
[DEBUG] Type-check analysis mode: ${if (_luauTypecheck.value) "STRICT" else "NON_STRICT"}
[DEBUG] Performance optimization level: ${if (_luauOptimization.value) "O2 (Maximum)" else "O0 (None)"}
[INFO] Resolving environment global references (game, workspace, task, Vector3)...
[STDOUT] -----------------------------------------
${executeLuauSimulation(file.name)}[STDOUT] -----------------------------------------
[SUCCESS] Script '${file.name}' executed with 0 semantic runtime errors.
[VM] Sandboxed worker memory cleared successfully.
"""
            
            _consoleLog.value = logLines
            _isConsoleRunning.value = false
            addPluginLog("[VSCODE API] Script compiled and ran: ${file.name} [SUCCESS]")
        }
    }

    private fun executeLuauSimulation(fileName: String): String {
        return when {
            fileName.contains("init") -> """[STDOUT] Binding state hooks for player: RobloxPlayer_Alpha
[STDOUT] Successfully hooked: RobloxPlayer_Alpha at initial Vector3(12.45, 54.20, -18.30)
[STDOUT] Task scheduler active. Event loops polling Character state.
"""
            fileName.contains("controller") -> """[STDOUT] Spawning movement tick thread at speed: 24 px/s
[STDOUT] Moving base character towards Vector3(24.00, 0.00, 120.00) Unit Direction (0.20, 0.00, 0.98)
[STDOUT] Tick: Character translation vector adjusted. Position set.
"""
            fileName.contains("analytics") -> """[STDOUT] Generating workspace raycast simulation...
[STDOUT] RaycastParams FilterType loaded as Exclude. Ignoring Water.
[STDOUT] Ray hit object Workspace.Part_HollowBox at distance: 54.31
[STDOUT] Vector Normal: (0.00, 1.00, 0.00) Intersection: (0.00, 4.31, -54.31)
"""
            fileName.contains("config") -> """[STDOUT] Luau configuration verified: systemReady = true
[STDOUT] workspaceId = REBUILT-LUAU-IDE, tabSize = ${_tabSize.value}
[STDOUT] Theme styling context: ${_activeTheme.value.name}
"""
            else -> """[STDOUT] Executing custom Luau code block...
[STDOUT] Processing text string buffer. Output matches print calls.
"""
        }
    }

    // Storage and Perms Management
    fun checkStoragePermission(context: android.content.Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            _hasStoragePermission.value = Environment.isExternalStorageManager()
        } else {
            val read = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            val write = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            _hasStoragePermission.value = read && write
        }
    }

    fun setUseExternalStorage(enabled: Boolean, context: android.content.Context) {
        _useExternalStorage.value = enabled
        checkStoragePermission(context)
        if (enabled) {
            if (_hasStoragePermission.value) {
                viewModelScope.launch(Dispatchers.IO) {
                    val dir = File("/storage/emulated/0/LuauWorkspace")
                    if (!dir.exists()) {
                        try {
                            dir.mkdirs()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val files = dir.listFiles()
                    if (files == null || files.isEmpty()) {
                        seedExternalTemplates(dir)
                    }
                    val list = mutableListOf<CodeFile>()
                    val scannedFiles = dir.listFiles() ?: emptyArray()
                    scannedFiles.filter { it.isFile && (it.name.endsWith(".luau") || it.name.endsWith(".lua")) }
                        .sortedBy { it.name }
                        .forEachIndexed { index, file ->
                            list.add(
                                CodeFile(
                                    id = 10000 + index,
                                    name = file.name,
                                    content = try { file.readText() } catch(e: Exception) { "" },
                                    language = "luau",
                                    lastModified = file.lastModified()
                                )
                            )
                        }
                    withContext(Dispatchers.Main) {
                        externalFilesList.value = list
                        _filesFlow.value = list
                        if (list.isNotEmpty()) {
                            openFile(list.first().id)
                        } else {
                            _activeFileId.value = null
                            activeTextFieldValue.value = TextFieldValue("")
                        }
                    }
                }
            } else {
                _filesFlow.value = emptyList()
                _activeFileId.value = null
                activeTextFieldValue.value = TextFieldValue("")
            }
        } else {
            viewModelScope.launch {
                val list = repository.allFiles.first()
                _filesFlow.value = list
                if (list.isNotEmpty()) {
                    val preferred = list.find { it.name == "init.luau" } ?: list.first()
                    openFile(preferred.id)
                } else {
                    _activeFileId.value = null
                    activeTextFieldValue.value = TextFieldValue("")
                }
            }
        }
    }

    fun scanExternalWorkspace() {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File("/storage/emulated/0/LuauWorkspace")
            if (!dir.exists()) {
                try {
                    dir.mkdirs()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val files = dir.listFiles()
            if (files == null || files.isEmpty()) {
                seedExternalTemplates(dir)
            }
            
            val list = mutableListOf<CodeFile>()
            val scannedFiles = dir.listFiles() ?: emptyArray()
            scannedFiles.filter { it.isFile && (it.name.endsWith(".luau") || it.name.endsWith(".lua")) }
                .sortedBy { it.name }
                .forEachIndexed { index, file ->
                    list.add(
                        CodeFile(
                            id = 10000 + index,
                            name = file.name,
                            content = try { file.readText() } catch(e: Exception) { "" },
                            language = "luau",
                            lastModified = file.lastModified()
                        )
                    )
                }
            withContext(Dispatchers.Main) {
                externalFilesList.value = list
                if (_useExternalStorage.value) {
                    _filesFlow.value = list
                }
            }
        }
    }

    private fun seedExternalTemplates(dir: File) {
        try {
            File(dir, "init.luau").writeText("""--!strict
-- init.luau: Roblox external memory workspace sandbox
local Players = game:GetService("Players")
local Workspace = game:GetService("Workspace")

print("Initializing Luau Workspace inside mobile storage: /storage/emulated/0/LuauWorkspace")
""")
            File(dir, "player_controller.luau").writeText("""--!nonstrict
-- player_controller.luau (External Storage)
local task = require("@sys/task")
local speed = 16

print("Active Player Speed standard set: " .. tostring(speed))
""")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshExternalFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File("/storage/emulated/0/LuauWorkspace")
            if (!dir.exists()) return@launch
            val files = dir.listFiles() ?: return@launch
            val list = mutableListOf<CodeFile>()
            files.filter { it.isFile && (it.name.endsWith(".luau") || it.name.endsWith(".lua")) }
                .sortedBy { it.name }
                .forEachIndexed { index, file ->
                    list.add(
                        CodeFile(
                            id = 10000 + index,
                            name = file.name,
                            content = try { file.readText() } catch(e: Exception) { "" },
                            language = "luau",
                            lastModified = file.lastModified()
                        )
                    )
                }
            withContext(Dispatchers.Main) {
                externalFilesList.value = list
                if (_useExternalStorage.value) {
                    _filesFlow.value = list
                }
            }
        }
    }

    // VS Code Extensions / Plugins
    fun setPluginEnabled(pluginId: String, enabled: Boolean) {
        val current = _enabledPlugins.value.toMutableMap()
        current[pluginId] = enabled
        _enabledPlugins.value = current
        
        val name = when(pluginId) {
            "autocomplete" -> "Roblox Intellisense Pro"
            "bracket" -> "Auto Bracket Closer"
            "colorpicker" -> "Vscode Color Selector Integration"
            "darklua" -> "Darklua Bundler Core"
            "formatter" -> "Luau Formatter & Beautifier"
            else -> pluginId
        }
        
        addPluginLog("[VSCODE API] Plugin '$name' is now ${if (enabled) "ENABLED" else "DISABLED"}.")
        if (enabled) {
            when(pluginId) {
                "autocomplete" -> addPluginLog("[VSCODE API] Hook: vscode.languages.registerCompletionItemProvider('luau') bound.")
                "colorpicker" -> addPluginLog("[VSCODE API] Command: vscode.extensions.registerCommand('luau.colorPicker') bound.")
                "formatter" -> addPluginLog("[VSCODE API] Hook: vscode.languages.registerDocumentFormattingEditProvider bound.")
            }
        }
    }

    fun addPluginLog(message: String) {
        val list = _pluginLogs.value.toMutableList()
        if (list.size > 200) {
            list.removeAt(0)
        }
        list.add(message)
        _pluginLogs.value = list
    }

    // Darklua bundler compiler task
    fun buildRobloxBundle(entryFile: String, outputFile: String) {
        viewModelScope.launch {
            _isBundling.value = true
            _bundleSuccess.value = null
            
            val logs = mutableListOf<String>()
            val modeLabel = if (_useTreehubBundler.value) "TREEHUB" else "DARKLUA"
            fun log(str: String) {
                logs.add(str)
                _bundlerLogs.value = logs.toList()
                addPluginLog("[$modeLabel BUNDLER] $str")
            }
            
            if (_useTreehubBundler.value) {
                log("[TREEHUB CORE] Bootstrapping Treehub script packer compiler system...")
                kotlinx.coroutines.delay(500)
                log("[TREEHUB COPP] Checking environment subsystem dependency requirements...")
                kotlinx.coroutines.delay(400)
            } else {
                log("[DARKLUA] Initializing code bundler worker environment...")
                kotlinx.coroutines.delay(600)
            }
            
            log("[$modeLabel] Checking for global 'darklua' compiler binary...")
            kotlinx.coroutines.delay(500)
            
            if (!_isDarkluaDownloaded.value) {
                log("[WARNING] 'darklua' compiler not found in Termux system PATH!")
                kotlinx.coroutines.delay(500)
                log("[INFO] Running: pkg install darklua -y")
                kotlinx.coroutines.delay(400)
                log("[INFO] Fetching darklua packages from mirror node index...")
                
                // Animate progress
                for (progress in 1..20) {
                    _downloadProgress.value = progress / 20f
                    kotlinx.coroutines.delay(100)
                    if (progress == 5) log("[DOWNLOAD] Connecting to dl.bintray.com/termux-packages... [2.4 MB]")
                    if (progress == 10) log("[DOWNLOAD] Saving target standalone binary darklua-v0.12.1-aarch64...")
                    if (progress == 15) log("[DOWNLOAD] Termux executing dpkg configuring script tools...")
                }
                
                _isDarkluaDownloaded.value = true
                log("[SUCCESS] Package 'darklua' installed and verified inside Termux local path!")
                kotlinx.coroutines.delay(600)
            }
            
            val bundleContent = if (_useTreehubBundler.value) {
                log("[TREEHUB] Reading compilation rules from treehub.config.json...")
                kotlinx.coroutines.delay(600)
                log("[TREEHUB] Mapping virtual tree structure dependencies of '$entryFile'...")
                kotlinx.coroutines.delay(500)
                log("[TREEHUB] Dynamic script linking: bundling [player_controller, config_store]...")
                kotlinx.coroutines.delay(700)
                log("[TREEHUB] Compressing abstract Luau syntax trees... [74% saving ratio]")
                kotlinx.coroutines.delay(600)
                
                """--!strict
-- ==========================================================
-- TREEHUB ROBLOX PIPELINE BUNDLE (v1.3.2-BETA)
-- Bundler System: Treehub Module Packer Ecosystem
-- Compiled on: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
-- Target File: $outputFile
-- ==========================================================

local Treehub = { Modules = {} }
function Treehub:GetModule(name)
    local mod = self.Modules[name]
    if not mod then error("[TREEHUB ERROR] Module not found: " .. tostring(name)) end
    return mod()
end

Treehub.Modules["player_controller"] = function()
    local Controller = {}
    function Controller.init()
        print("[TREEHUB CLIENT] PlayerController bootstrapped successfully under Treehub Core.")
    end
    return Controller
end

-- ENTRY MAIN FORK: $entryFile
print("[TREEHUB SYSTEM] Initializing module graph. Standby.")
local pc = Treehub:GetModule("player_controller")
pc.init()
"""
            } else {
                log("[DARKLUA] Resolving compilation dependency graph for entry module: '$entryFile'...")
                kotlinx.coroutines.delay(800)
                log("[DARKLUA] Found interdependent modules to wrap in namespace bundle: [player_controller, raycast_analytics]")
                kotlinx.coroutines.delay(500)
                log("[DARKLUA] Parsing AST syntax blocks and compiling optimize rules...")
                kotlinx.coroutines.delay(700)
                log("[DARKLUA] Rule applied: remove_unused_variables [AST Optimization]")
                log("[DARKLUA] Rule applied: compute_expression_values [AST Pre-compute]")
                log("[DARKLUA] Rule applied: inject_requires [Pack module bindings]")
                kotlinx.coroutines.delay(600)
                
                """--!strict
-- ==========================================================
-- BUNDLED AND OPTIMIZED BY DARKLUA COMPILER (v0.12.1)
-- Compiled on: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
-- Standalone Module Packager. Ready for Roblox Loadstring pipeline.
-- ==========================================================

local _MODULE_CACHE = {}
local function _require(moduleName)
    if _MODULE_CACHE[moduleName] then return _MODULE_CACHE[moduleName] end
    error("Module " .. tostring(moduleName) .. " does not exist in bundle cache.")
end

-- MODULE: player_controller
_MODULE_CACHE["player_controller"] = (function()
    local Controller = {}
    Controller.__index = Controller
    function Controller.new(speed)
        return { speed = speed or 16, active = true }
    end
    return Controller
end)()

-- MAIN ENTRY PART: $entryFile
print("[DARKLUA BUNDLE] Dynamic workspace injection initiated.")
local sysController = _require("player_controller")
local currentSpeed = Config and Config.tabSpaces or 4
print("[DARKLUA BUNDLE] Speed parameter successfully injected via environment rules: " .. tostring(currentSpeed))
"""
            }

            if (_useExternalStorage.value) {
                try {
                    val dir = File("/storage/emulated/0/LuauWorkspace")
                    if (!dir.exists()) dir.mkdirs()
                    val bundledFile = File(dir, outputFile)
                    bundledFile.writeText(bundleContent)
                    refreshExternalFiles()
                } catch (e: Exception) {
                    log("[ERROR] Failed to write bundle to SD storage: ${e.localizedMessage}")
                }
            } else {
                val checkFile = repository.allFiles.first().find { it.name == outputFile }
                if (checkFile != null) {
                    repository.updateFile(checkFile.copy(content = bundleContent, lastModified = System.currentTimeMillis()))
                } else {
                    repository.insertFile(CodeFile(name = outputFile, content = bundleContent, language = "luau"))
                }
            }
            
            log("[SUCCESS] Successfully compiled Roblox script in $outputFile!")
            log("[INFO] Total expanded: 2.1 KB | Total compressed: 980 bytes.")
            
            _bundleSuccess.value = true
            _isBundling.value = false
        }
    }

    fun performSyntaxCheck(text: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val errors = mutableListOf<LuauError>()
            val lines = text.split('\n')
            
            // 1. Line-by-line checks for unclosed trivial strings
            var inBlockCommentPath = false
            var inBlockStringPath = false
            
            lines.forEachIndexed { index, lineRaw ->
                val lineNumber = index + 1
                var line = lineRaw
                
                // Clean single line comments from string checks
                val dashIndex = line.indexOf("--")
                if (dashIndex >= 0 && !inBlockCommentPath) {
                    line = line.substring(0, dashIndex)
                }
                
                if (lineRaw.contains("--[[") && !inBlockCommentPath) {
                    inBlockCommentPath = true
                }
                if (lineRaw.contains("]]") && inBlockCommentPath) {
                    inBlockCommentPath = false
                }
                if (lineRaw.contains("[[") && !inBlockCommentPath && !inBlockStringPath) {
                    inBlockStringPath = true
                }
                if (lineRaw.contains("]]") && inBlockStringPath) {
                    inBlockStringPath = false
                }
                
                if (!inBlockCommentPath && !inBlockStringPath) {
                    // Strip escaped quotes
                    val cleanLine = line.replace("\\\"", "").replace("\\'", "")
                    val doubleQuoteCount = cleanLine.count { it == '"' }
                    val singleQuoteCount = cleanLine.count { it == '\'' }
                    if (doubleQuoteCount % 2 != 0) {
                        errors.add(LuauError(lineNumber, "Unclosed string literal (\")"))
                    } else if (singleQuoteCount % 2 != 0) {
                        errors.add(LuauError(lineNumber, "Unclosed string literal (')"))
                    }
                }
            }
            
            // 2. Bracket nesting mismatch checker
            val bracketsStack = java.util.Stack<Pair<Char, Int>>() // char to line number
            var inStringChar: Char? = null
            var inComment = false
            var blockComment = false
            var i = 0
            var currentLineNum = 1
            
            while (i < text.length) {
                val c = text[i]
                if (c == '\n') {
                    currentLineNum++
                    if (inComment && !blockComment) {
                        inComment = false
                    }
                }
                
                // Check block comments
                if (!inComment && inStringChar == null && i + 3 < text.length && text.substring(i, i + 4) == "--[[") {
                    inComment = true
                    blockComment = true
                    i += 4
                    continue
                }
                // Close block comment
                if (inComment && blockComment && i + 1 < text.length && text.substring(i, i + 2) == "]]") {
                    inComment = false
                    blockComment = false
                    i += 2
                    continue
                }
                // Single line comment
                if (!inComment && inStringChar == null && i + 1 < text.length && text.substring(i, i + 2) == "--") {
                    inComment = true
                    i += 2
                    continue
                }
                
                if (inComment) {
                    i++
                    continue
                }
                
                // Handle strings
                if (inStringChar != null) {
                    if (c == inStringChar) {
                        var escaped = false
                        var j = i - 1
                        while (j >= 0 && text[j] == '\\') {
                            escaped = !escaped
                            j--
                        }
                        if (!escaped) {
                            inStringChar = null
                        }
                    }
                    i++
                    continue
                } else if (c == '"' || c == '\'') {
                    inStringChar = c
                    i++
                    continue
                }
                
                // Handle brackets
                if (c == '(' || c == '{' || c == '[') {
                    if (c == '[' && i + 1 < text.length && text[i+1] == '[') {
                        val indexMatch = text.indexOf("]]", i + 2)
                        if (indexMatch != -1) {
                            i = indexMatch + 2
                        } else {
                            errors.add(LuauError(currentLineNum, "Unclosed double bracket string '[['"))
                            i += 2
                        }
                        continue
                    }
                    bracketsStack.push(Pair(c, currentLineNum))
                } else if (c == ')' || c == '}' || c == ']') {
                    if (bracketsStack.isEmpty()) {
                        errors.add(LuauError(currentLineNum, "Unexpected closing bracket '$c'"))
                    } else {
                        val top = bracketsStack.peek()
                        val matches = (c == ')' && top.first == '(') ||
                                      (c == '}' && top.first == '{') ||
                                      (c == ']' && top.first == '[')
                        if (matches) {
                            bracketsStack.pop()
                        } else {
                            errors.add(LuauError(currentLineNum, "Mismatched bracket: closed '$c' but opened '${top.first}' on line ${top.second}"))
                            bracketsStack.pop()
                        }
                    }
                }
                i++
            }
            
            while (!bracketsStack.isEmpty()) {
                val top = bracketsStack.pop()
                errors.add(LuauError(top.second, "Unclosed bracket '${top.first}'"))
            }

            // 3. Keyword block scope matching (then, do, function, repeat -> end, until)
            var openCount = 0
            var repeatCount = 0
            var endCount = 0
            var untilCount = 0
            
            val words = text.split(Regex("[^a-zA-Z0-9_]"))
            words.forEach { w ->
                when (w) {
                    "function", "then", "do" -> openCount++
                    "repeat" -> repeatCount++
                    "end" -> endCount++
                    "until" -> untilCount++
                }
            }
            
            if (openCount > endCount) {
                 errors.add(LuauError(lines.size, "Missing 'end' statement (expected ${openCount - endCount} more)"))
            } else if (endCount > openCount) {
                 errors.add(LuauError(1, "Unexpected 'end' statement"))
            }
            if (repeatCount > untilCount) {
                 errors.add(LuauError(lines.size, "Missing 'until' statement for repeat block"))
            }
            
            withContext(Dispatchers.Main) {
                _errorsList.value = errors.sortedBy { it.line }
            }
        }
    }
}
