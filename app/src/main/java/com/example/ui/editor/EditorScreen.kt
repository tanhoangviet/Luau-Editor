package com.example.ui.editor

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.CodeFile

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.activeTheme.collectAsState()
    val isSidebarExpanded by viewModel.isSidebarExpanded.collectAsState()
    val activeSidebarTab by viewModel.activeSidebarTab.collectAsState()
    val files by viewModel.allFiles.collectAsState()
    val openFileIds by viewModel.openFileIds.collectAsState()
    val activeFileId by viewModel.activeFileId.collectAsState()
    val isModified by viewModel.isModified.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val replaceQuery by viewModel.replaceQuery.collectAsState()
    val consoleLog by viewModel.consoleLog.collectAsState()
    val isConsoleRunning by viewModel.isConsoleRunning.collectAsState()
    val wordWrap by viewModel.wordWrap.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()

    // Workspace & settings tab flow
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isSettingsActive by viewModel.isSettingsActive.collectAsState()
    val enabledPlugins by viewModel.enabledPlugins.collectAsState()

    val isBundling by viewModel.isBundling.collectAsState()
    val bundleSuccess by viewModel.bundleSuccess.collectAsState()
    val bundlerLogs by viewModel.bundlerLogs.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val isDarkluaDownloaded by viewModel.isDarkluaDownloaded.collectAsState()
    val isBetaInterface by viewModel.isBetaInterface.collectAsState()
    val errorsList by viewModel.errorsList.collectAsState()
    val useTreehubBundler by viewModel.useTreehubBundler.collectAsState()

    // Configurable switches reflecting the ViewModel States
    val autoSave by viewModel.autoSave.collectAsState()
    val showMinimap by viewModel.showMinimap.collectAsState()
    val showLineNumbers by viewModel.showLineNumbers.collectAsState()
    val fontFamilySelection by viewModel.fontFamily.collectAsState()
    val cursorStyleSelection by viewModel.cursorStyle.collectAsState()
    val luauTypecheck by viewModel.luauTypecheck.collectAsState()
    val luauOptimization by viewModel.luauOptimization.collectAsState()
    val cursorBlinkingSelection by viewModel.cursorBlinking.collectAsState()
    val tabSizeSelection by viewModel.tabSize.collectAsState()

    var showCreateFileDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

    val activeFile = files.find { it.id == activeFileId }
    val openFilesList = files.filter { it.id in openFileIds }

    val focusManager = LocalFocusManager.current
    val mainBgColor = activeTheme.backgroundColor
    val textColor = activeTheme.textColor

    val currentThemeBg = if (isBetaInterface) Color(0xEC090B0F) else mainBgColor
    val currentSidebarBg = if (isBetaInterface) Color(0xD2050608) else activeTheme.sidebarBgColor

    Surface(
        modifier = modifier.fillMaxSize(),
        color = if (isBetaInterface) Color(0xFF060709) else mainBgColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isBetaInterface) {
                // Glass-liquid radiant backend flows
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(Color(0x388B5CF6), Color.Transparent),
                            radius = size.width * 0.75f
                        ),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.25f)
                    )
                    drawCircle(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(Color(0x2810B981), Color.Transparent),
                            radius = size.width * 0.85f
                        ),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.75f)
                    )
                }
            }
            Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().statusBarsPadding()) {
            
            // Main IDE Area: Activity Bar + Sidebar Panel + Editor Tabs Workspace
            Row(modifier = Modifier.weight(1f)) {
                
                // 1. ACTIVITY BAR (Far Left Selector Column with Settings Trigger)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(54.dp)
                        .background(currentSidebarBg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ActivityIconButton(
                        icon = Icons.Default.Folder,
                        contentDescription = "File Explorer",
                        isActive = activeSidebarTab == "explorer" && isSidebarExpanded && !isSettingsActive,
                        theme = activeTheme,
                        onClick = {
                            viewModel.setSettingsActive(false)
                            if (activeSidebarTab == "explorer" && isSidebarExpanded) {
                                viewModel.setSidebarExpanded(false)
                            } else {
                                viewModel.setSidebarTab("explorer")
                            }
                        }
                    )

                    ActivityIconButton(
                        icon = Icons.Default.Search,
                        contentDescription = "Find & Replace",
                        isActive = activeSidebarTab == "search" && isSidebarExpanded && !isSettingsActive,
                        theme = activeTheme,
                        onClick = {
                            viewModel.setSettingsActive(false)
                            if (activeSidebarTab == "search" && isSidebarExpanded) {
                                viewModel.setSidebarExpanded(false)
                            } else {
                                viewModel.setSidebarTab("search")
                            }
                        }
                    )

                    ActivityIconButton(
                        icon = Icons.Default.Terminal,
                        contentDescription = "Console Logs",
                        isActive = activeSidebarTab == "console" && isSidebarExpanded && !isSettingsActive,
                        theme = activeTheme,
                        onClick = {
                            viewModel.setSettingsActive(false)
                            if (activeSidebarTab == "console" && isSidebarExpanded) {
                                viewModel.setSidebarExpanded(false)
                            } else {
                                viewModel.setSidebarTab("console")
                            }
                        }
                    )

                    ActivityIconButton(
                        icon = Icons.Default.Build,
                        contentDescription = "Build Bundle (Darklua)",
                        isActive = activeSidebarTab == "bundle" && isSidebarExpanded && !isSettingsActive,
                        theme = activeTheme,
                        onClick = {
                            viewModel.setSettingsActive(false)
                            if (activeSidebarTab == "bundle" && isSidebarExpanded) {
                                viewModel.setSidebarExpanded(false)
                            } else {
                                viewModel.setSidebarTab("bundle")
                            }
                        }
                    )

                    ActivityIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Open Settings Tab",
                        isActive = isSettingsActive,
                        theme = activeTheme,
                        onClick = {
                            viewModel.setSettingsActive(true)
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Play arrow action (Compile Luau snippet run)
                    IconButton(
                        onClick = { viewModel.runActiveCode() },
                        enabled = activeFileId != null && !isConsoleRunning && !isSettingsActive,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .size(40.dp)
                            .background(
                                if (isConsoleRunning) Color(0x33FFA500) else if (activeFileId != null) Color(0x2238BDF8) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isConsoleRunning) Color.Yellow else if (activeFileId != null) Color(0xFF38BDF8) else Color(0x1F858585),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = if (isConsoleRunning) Icons.Default.HourglassEmpty else Icons.Default.PlayArrow,
                            contentDescription = "Run Luau interpreter simulation",
                            tint = if (isConsoleRunning) Color.Yellow else if (activeFileId != null) Color(0xFF38BDF8) else activeTheme.textColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Separator Line for Activity Bar
                Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFF2B2B2B)))

                // 2. SIDEBAR PANEL (Collapsible Dynamic Controls Drawer)
                AnimatedVisibility(
                    visible = isSidebarExpanded && !isSettingsActive,
                    enter = slideInHorizontally(animationSpec = spring()) { -it } + fadeIn(),
                    exit = slideOutHorizontally(animationSpec = spring()) { -it } + fadeOut()
                ) {
                    Row(modifier = Modifier.fillMaxHeight().width(231.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(230.dp)
                                .background(activeTheme.sidebarBgColor)
                        ) {
                            // Sidebar Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = when (activeSidebarTab) {
                                        "explorer" -> "LUAU EXPLORER"
                                        "search" -> "FIND & REPLACE"
                                        "console" -> "OUTPUT VM TERMINAL"
                                        "bundle" -> "DARKLUA BUNDLER"
                                        else -> "SIDEBAR"
                                    },
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = activeTheme.textColor.copy(alpha = 0.7f)
                                    )
                                )

                                IconButton(
                                    onClick = { viewModel.setSidebarExpanded(false) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Collapse sidebar",
                                        tint = activeTheme.textColor.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFF2B2B2B))

                            // Sidebar Tab Bob
                            Box(modifier = Modifier.weight(1f)) {
                                when (activeSidebarTab) {
                                    "explorer" -> ExplorerTabContent(
                                        files = files,
                                        activeFileId = activeFileId,
                                        activeTheme = activeTheme,
                                        onFileClick = { id -> viewModel.openFile(id) },
                                        onFileDelete = { id -> viewModel.deleteFile(id) },
                                        onCreateFileClick = { showCreateFileDialog = true }
                                    )
                                    "search" -> SearchTabContent(
                                        searchQuery = searchQuery,
                                        replaceQuery = replaceQuery,
                                        activeTheme = activeTheme,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        onReplaceChange = { viewModel.setReplaceQuery(it) },
                                        onReplaceClick = {
                                            viewModel.performReplace()
                                            focusManager.clearFocus()
                                        }
                                    )
                                    "console" -> ConsoleTabContent(
                                        consoleLog = consoleLog,
                                        activeTheme = activeTheme,
                                        isConsoleRunning = isConsoleRunning
                                    )
                                    "bundle" -> BundleTabContent(
                                        viewModel = viewModel,
                                        activeTheme = activeTheme
                                    )
                                }
                            }
                        }

                        // Separator line at end of Sidebar
                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFF2B2B2B)))
                    }
                }

                // 3. EDITOR WORKSPACE PANEL
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    
                    // VS-Code TabBar containing files AND the special Settings tab
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .background(activeTheme.inactiveTabColor),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Open Code Files List
                        items(openFilesList, key = { it.id }) { tabFile ->
                            val isTabActive = tabFile.id == activeFileId && !isSettingsActive
                            val tabBgColor = if (isTabActive) activeTheme.activeTabColor else activeTheme.inactiveTabColor
                            val tabTextColor = if (isTabActive) activeTheme.textColor else activeTheme.textColor.copy(alpha = 0.5f)

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(tabBgColor)
                                    .clickable { viewModel.openFile(tabFile.id) }
                            ) {
                                // Active Tab Top Highlight bar in accent color
                                if (isTabActive) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .background(activeTheme.caretColor)
                                            .align(Alignment.TopCenter)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Custom Roblox-themed script symbol for all files
                                    Text(
                                        text = "🌀",
                                        fontSize = 11.sp
                                    )

                                    Text(
                                        text = tabFile.name,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = if (isTabActive) FontWeight.Bold else FontWeight.Normal,
                                            color = tabTextColor
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 110.dp)
                                    )

                                    if (isModified && isTabActive) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFFFFA500))
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.closeFile(tabFile.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close file tab",
                                            tint = activeTheme.textColor.copy(alpha = 0.4f),
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFF2B2B2B)))
                                }
                            }
                        }

                        // VS-CODE SETTINGS TAB INTERACTION (Integrates cleanly inside TabRow)
                        if (isSettingsOpen) {
                            item {
                                val isTabActive = isSettingsActive
                                val tabBgColor = if (isTabActive) activeTheme.activeTabColor else activeTheme.inactiveTabColor
                                val tabTextColor = if (isTabActive) activeTheme.textColor else activeTheme.textColor.copy(alpha = 0.5f)

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .background(tabBgColor)
                                        .clickable { viewModel.setSettingsActive(true) }
                                ) {
                                    if (isTabActive) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(activeTheme.caretColor)
                                                .align(Alignment.TopCenter)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings icon",
                                            tint = if (isTabActive) activeTheme.caretColor else tabTextColor.copy(alpha = 0.6f),
                                            modifier = Modifier.size(13.dp)
                                        )

                                        Text(
                                            text = "Settings",
                                            style = TextStyle(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                fontWeight = if (isTabActive) FontWeight.Bold else FontWeight.Normal,
                                                color = tabTextColor
                                            ),
                                            maxLines = 1
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.closeSettings() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close Settings tab",
                                                tint = activeTheme.textColor.copy(alpha = 0.4f),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFF2B2B2B)))
                                    }
                                }
                            }
                        }
                    }

                    // Bottom separator for active tab row
                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Controls row (shows helper states unless settings is focused)
                    if (!isSettingsActive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(activeTheme.backgroundColor)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { viewModel.triggerUndo() }, enabled = activeFileId != null, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Undo, contentDescription = "Undo typing", tint = activeTheme.textColor.copy(alpha = if (activeFileId != null) 0.8f else 0.3f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.triggerRedo() }, enabled = activeFileId != null, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Redo, contentDescription = "Redo typing", tint = activeTheme.textColor.copy(alpha = if (activeFileId != null) 0.8f else 0.3f), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.saveActiveFile() }, enabled = activeFileId != null && isModified && !autoSave, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Default.Save,
                                        contentDescription = "Save code file",
                                        tint = if (isModified) Color(0xFF38BDF8) else activeTheme.textColor.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (activeFile != null) "workspace > ${activeFile.name}" else "no active file",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = activeTheme.textColor.copy(alpha = 0.5f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }

                        HorizontalDivider(color = Color(0xFF2B2B2B))
                    }

                    // Main workspace Content Frame (Renders Editor OR vs-code configuration dashboard)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(currentThemeBg)
                    ) {
                        if (isSettingsActive) {
                            // VS CODE GUI CONFIGURATION PANEL
                            SettingsDashboard(viewModel = viewModel, activeTheme = activeTheme)
                        } else if (activeFile != null) {
                            // TEXT EDITOR LAYOUT
                            var textFieldValue by viewModel.activeTextFieldValue
                            var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                            val customHighlight = CodeSyntaxHighlightTransformation(activeFile.language, activeTheme)

                            val editorScrollState = rememberScrollState()
                            val editorModifier = if (wordWrap) {
                                Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp).verticalScroll(rememberScrollState())
                            } else {
                                Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp).verticalScroll(rememberScrollState()).horizontalScroll(editorScrollState)
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = editorModifier.weight(1f)) {
                                    BasicTextField(
                                        value = textFieldValue,
                                        onValueChange = { viewModel.updateActiveText(it) },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp),
                                        onTextLayout = { textLayoutResult = it },
                                        textStyle = TextStyle(
                                            fontFamily = when (fontFamilySelection) {
                                                "JetBrains Mono" -> FontFamily.Monospace
                                                "Fira Code" -> FontFamily.Monospace
                                                "Space Grotesk" -> FontFamily.SansSerif
                                                else -> FontFamily.Monospace
                                              },
                                            fontSize = fontSize.sp,
                                            color = textColor,
                                            lineHeight = (fontSize * 1.35f).sp
                                        ),
                                        cursorBrush = SolidColor(activeTheme.caretColor),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.None),
                                        visualTransformation = customHighlight,
                                        decorationBox = { innerTextField ->
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                // 1. Line numbers column
                                                if (showLineNumbers) {
                                                    val layout = textLayoutResult
                                                    val visualLineInfos = remember(layout, textFieldValue.text) {
                                                        val list = mutableListOf<String>()
                                                        if (layout != null) {
                                                            val text = layout.layoutInput.text
                                                            var lastParagraph = -1
                                                            var paragraphIndex = 0
                                                            var charIndex = 0
                                                            val lineCount = layout.lineCount
                                                            for (i in 0 until lineCount) {
                                                                val startOffset = try {
                                                                    layout.getLineStart(i)
                                                                } catch (e: Exception) {
                                                                    0
                                                                }
                                                                // Advance paragraph index up to startOffset of the current visual line
                                                                while (charIndex < startOffset && charIndex < text.length) {
                                                                    if (text[charIndex] == '\n') {
                                                                        paragraphIndex++
                                                                    }
                                                                    charIndex++
                                                                }
                                                                if (paragraphIndex != lastParagraph) {
                                                                    list.add((paragraphIndex + 1).toString())
                                                                    lastParagraph = paragraphIndex
                                                                } else {
                                                                    list.add("") // empty line indicator for wrapped lines
                                                                }
                                                            }
                                                        } else {
                                                            val linesCount = textFieldValue.text.lines().size
                                                            for (i in 1..linesCount) {
                                                                list.add(i.toString())
                                                            }
                                                        }
                                                        list
                                                    }

                                                    Column(
                                                        modifier = Modifier
                                                            .width(36.dp)
                                                            .padding(end = 8.dp),
                                                        horizontalAlignment = Alignment.End
                                                    ) {
                                                        for (info in visualLineInfos) {
                                                            val lineNum = info.toIntOrNull()
                                                            val hasError = lineNum != null && errorsList.any { it.line == lineNum }
                                                            Text(
                                                                text = if (hasError) "● $info" else info,
                                                                style = TextStyle(
                                                                    fontFamily = FontFamily.Monospace,
                                                                    fontSize = fontSize.sp,
                                                                    lineHeight = (fontSize * 1.35f).sp,
                                                                    color = if (hasError) Color(0xFFEF4444) else activeTheme.lineNumberColor,
                                                                    fontWeight = if (hasError) FontWeight.Bold else FontWeight.Normal
                                                                )
                                                            )
                                                        }
                                                    }

                                                    // Divider for Line Numbers Column
                                                    Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color(0xFF2B2B2B)))
                                                }

                                                // 2. Editor Surface Canvas
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(start = 10.dp)
                                                ) {
                                                    innerTextField()
                                                }
                                            }
                                        }
                                    )
                                }

                                // Interactive Mini-map (VS-Code style blueprint tracker)
                                if (showMinimap) {
                                    Box(
                                        modifier = Modifier
                                            .width(54.dp)
                                            .fillMaxHeight()
                                            .background(activeTheme.sidebarBgColor.copy(alpha = 0.3f))
                                            .border(1.dp, Color(0xFF2B2B2B))
                                            .padding(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            textFieldValue.text.lines().take(50).forEach { line ->
                                                val widthFactor = (line.trim().length * 1.5).coerceAtMost(30.0).dp
                                                Box(
                                                    modifier = Modifier
                                                        .width(widthFactor)
                                                        .height(2.dp)
                                                        .background(
                                                            if (line.trim().startsWith("--")) {
                                                                activeTheme.commentColor.copy(alpha = 0.4f)
                                                            } else if (line.trim().startsWith("local") || line.trim().contains("function")) {
                                                                activeTheme.keywordColor.copy(alpha = 0.5f)
                                                            } else {
                                                                activeTheme.textColor.copy(alpha = 0.25f)
                                                            }
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Floating Glassmorphic Alert Tooltip for real-time error detection
                                if (errorsList.isNotEmpty()) {
                                    val firstError = errorsList.first()
                                    androidx.compose.foundation.layout.Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = androidx.compose.ui.Alignment.BottomCenter
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .padding(bottom = 16.dp, start = 14.dp, end = 14.dp)
                                                .fillMaxWidth(0.92f)
                                                .shadow(12.dp, RoundedCornerShape(12.dp)),
                                            colors = CardDefaults.cardColors(containerColor = if (isBetaInterface) Color(0xF21C0E11) else Color(0xFF261216)),
                                            border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Warning,
                                                        contentDescription = "Luau Error Check Loi",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Line ${firstError.line}: Check Lỗi Sai Tooltip",
                                                        style = TextStyle(
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = Color(0xFFEF4444)
                                                        )
                                                    )
                                                    Text(
                                                        text = firstError.message,
                                                        style = TextStyle(
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 9.sp,
                                                            color = Color.LightGray
                                                        )
                                                    )
                                                 }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // File select tips placeholder state
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Empty slate",
                                    tint = activeTheme.textColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Workspace Empty",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = activeTheme.textColor.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap the plus (+) button inside the File Explorer sidebar to create and open Luau modules.",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = activeTheme.textColor.copy(alpha = 0.35f)
                                    ),
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }

                    // 4. SYMBOL HELPER BAR
                    if (activeFileId != null && !isSettingsActive) {
                        QuickSymbolsSuggestionsBar(
                            activeTheme = activeTheme,
                            onSymbolClick = { viewModel.insertSymbol(it) }
                        )
                        if (enabledPlugins["colorpicker"] == true) {
                            QuickColorPickerBar(
                                activeTheme = activeTheme,
                                onColorSelect = { viewModel.insertSymbol(it) }
                            )
                        }
                    }
                }
            }

            // 5. STATUS BAR (Tactical HUD status)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(activeTheme.bottomBarColor)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isSettingsActive) "⚙ SETTINGS CONFIG" else if (isModified) "● MODIFIED" else "✔ SYNCED",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (isSettingsActive) Color.White else if (isModified) Color(0xFFFBBF24) else Color(0xFF4ADE80)
                        )
                    )

                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )

                    Text(
                        text = "LUAU ENGINE",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    )
                }

                val lnCol = if (activeFile != null && !isSettingsActive) {
                    val selection = viewModel.activeTextFieldValue.value.selection
                    val text = viewModel.activeTextFieldValue.value.text
                    val lines = text.substring(0, selection.start.coerceIn(0, text.length)).lines()
                    val lineNum = lines.size
                    val colNum = lines.lastOrNull()?.length ?: 0
                    "Ln $lineNum, Col ${colNum + 1}"
                } else {
                    "Luau Editor Workspace"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = lnCol,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )

                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )

                    Text(
                        text = activeTheme.name,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }
            }
            if (isBetaInterface) {
                DynamicIslandOverlay(
                    errorsCount = errorsList.size,
                    isBundling = isBundling,
                    bundleSuccess = bundleSuccess
                )
            }
        }
    }

    // 6. CREATE FILE DIALOG (Enforces Luau file boundaries)
    if (showCreateFileDialog) {
        Dialog(onDismissRequest = { showCreateFileDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = activeTheme.sidebarBgColor),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2B2B2B)),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "CREATE NEW LUAU FILE",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = activeTheme.textColor
                        )
                    )

                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("Script name (e.g., character_hooks)", color = activeTheme.textColor.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeTheme.caretColor,
                            unfocusedBorderColor = Color(0xFF2B2B2B),
                            focusedTextColor = activeTheme.textColor,
                            unfocusedTextColor = activeTheme.textColor
                        ),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = activeTheme.backgroundColor),
                        border = BorderStroke(1.dp, Color(0x1F858585)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🌀", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "Target Engine Mode: LUAU",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = activeTheme.textColor)
                                )
                                Text(
                                    text = "Safe sandbox execution + Luau type assertions enabled.",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateFileDialog = false }) {
                            Text("CANCEL", color = activeTheme.textColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newFileName.isNotBlank()) {
                                    viewModel.createNewFile(newFileName, "luau")
                                    newFileName = ""
                                    showCreateFileDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = activeTheme.caretColor)
                        ) {
                            Text("CREATE", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDashboard(
    viewModel: EditorViewModel,
    activeTheme: CodeTheme
) {
    val autoSave by viewModel.autoSave.collectAsState()
    val showMinimap by viewModel.showMinimap.collectAsState()
    val showLineNumbers by viewModel.showLineNumbers.collectAsState()
    val isBetaInterface by viewModel.isBetaInterface.collectAsState()
    val useTreehubBundler by viewModel.useTreehubBundler.collectAsState()
    val fontFamilySelection by viewModel.fontFamily.collectAsState()
    val cursorStyleSelection by viewModel.cursorStyle.collectAsState()
    val luauTypecheck by viewModel.luauTypecheck.collectAsState()
    val luauOptimization by viewModel.luauOptimization.collectAsState()
    val cursorBlinkingSelection by viewModel.cursorBlinking.collectAsState()
    val tabSizeSelection by viewModel.tabSize.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val wordWrap by viewModel.wordWrap.collectAsState()

    val useExternalStorage by viewModel.useExternalStorage.collectAsState()
    val hasStoragePermission by viewModel.hasStoragePermission.collectAsState()
    val enabledPlugins by viewModel.enabledPlugins.collectAsState()
    val pluginLogs by viewModel.pluginLogs.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        viewModel.checkStoragePermission(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(activeTheme.backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // High fidelity Image banner representation inside settings
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2B2B2B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.settings_banner),
                        contentDescription = "Luau Core configurations panel banner outline",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Dark neon glassmorphic scrim overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xE60F1419))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "⚙", fontSize = 24.sp, color = activeTheme.caretColor)
                            Text(
                                text = "LUAU WORKSPACE SETTINGS",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Modify formatting rules, compiler strict levels, and visual highlight themes according to Roblox framework standards.",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.LightGray.copy(alpha = 0.7f),
                                lineHeight = 13.sp
                            )
                        )
                    }
                }
            }
        }

        // VISUAL SYSTEM CODE THEME PREVIEW SECTION (Theme dạng Card với Ảnh Banner Preview Theme)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT CODE THEME & PREVIEW",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = activeTheme.textColor.copy(alpha = 0.5f)
                    )
                )

                // Renders beautifully scaled visual Cards representing themes with real live-colored viewport previews
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CodeTheme.themes.chunked(2).forEach { themePair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            themePair.forEach { theme ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ThemePreviewCard(
                                        theme = theme,
                                        isActive = theme.id == activeTheme.id,
                                        onClick = { viewModel.setTheme(theme) }
                                    )
                                }
                            }
                            if (themePair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // TEXT EDITOR RULES SECTION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = activeTheme.sidebarBgColor),
                border = BorderStroke(1.dp, Color(0xFF2B2B2B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "TEXT EDITOR WORKSPACE",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = activeTheme.carrot() // we fallback safely
                        )
                    )

                    // Font Size slide configuration
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Font Size",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "${fontSize.toInt()}px",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = activeTheme.caretColor)
                            )
                        }
                        Slider(
                            value = fontSize,
                            onValueChange = { viewModel.setFontSize(it) },
                            valueRange = 10f..24f,
                            colors = SliderDefaults.colors(
                                thumbColor = activeTheme.caretColor,
                                activeTrackColor = activeTheme.caretColor,
                                inactiveTrackColor = Color(0x33858585)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Word wrapper option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Word Wrap",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Breaks overflowing textual lines cleanly inside script views.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = wordWrap,
                            onCheckedChange = { viewModel.setWordWrap(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeTheme.caretColor,
                                checkedTrackColor = activeTheme.caretColor.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // AutoSave option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Save Workspace",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Automatically updates database on text focus loss and key-press increments.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = autoSave,
                            onCheckedChange = { viewModel.setAutoSave(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeTheme.caretColor,
                                checkedTrackColor = activeTheme.caretColor.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Show Minimap option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Editor Minimap",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Displays custom colored thread outline guide on the right side of views.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = showMinimap,
                            onCheckedChange = { viewModel.setShowMinimap(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeTheme.caretColor,
                                checkedTrackColor = activeTheme.caretColor.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Show Line Numbers block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Line Numbers",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Renders indices along the left edge of code blocks.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = showLineNumbers,
                            onCheckedChange = { viewModel.setShowLineNumbers(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeTheme.caretColor,
                                checkedTrackColor = activeTheme.caretColor.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }
                }
            }
        }

        // LUAU COMPILER AND PRECISE SCHEDULER ENGINE SETTINGS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = activeTheme.sidebarBgColor),
                border = BorderStroke(1.dp, Color(0xFF2B2B2B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "LUAU PARSING & ENGINES",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = activeTheme.caretColor
                        )
                    )

                    // Font Family choices selection chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Core Editor Font Family",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                        )
                        val fonts = listOf("JetBrains Mono", "Fira Code", "Space Grotesk")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            fonts.forEach { font ->
                                val active = fontFamilySelection == font
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (active) activeTheme.caretColor.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (active) activeTheme.caretColor else Color(0x33858585),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { viewModel.setFontFamily(font) }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = font,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = if (active) activeTheme.textColor else activeTheme.textColor.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Tab size configuration
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Tab Space Indent size",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                        )
                        val spacesList = listOf(2, 4, 8)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            spacesList.forEach { size ->
                                val active = tabSizeSelection == size
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (active) activeTheme.caretColor.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (active) activeTheme.caretColor else Color(0x33858585),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { viewModel.setTabSize(size) }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "$size spaces",
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = if (active) activeTheme.textColor else activeTheme.textColor.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Static typechecking switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Strict Static Typechecking",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Enforces typed syntax rules (Type annotations, declarations warnings).",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = luauTypecheck,
                            onCheckedChange = { viewModel.setLuauTypecheck(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeTheme.caretColor,
                                checkedTrackColor = activeTheme.caretColor.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Sandbox JIT Optimization toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Perform Sandbox JIT Optimization",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Aggressively maximizes bytecode translation speeds for loops.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = luauOptimization,
                            onCheckedChange = { viewModel.setLuauOptimization(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = activeTheme.caretColor,
                                checkedTrackColor = activeTheme.caretColor.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Cursor blink styling choices
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Cursor blinking model",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                        )
                        val blinks = listOf("Smooth", "Blink", "Solid")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            blinks.forEach { blink ->
                                val active = cursorBlinkingSelection == blink
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (active) activeTheme.caretColor.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (active) activeTheme.caretColor else Color(0x33858585),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { viewModel.setCursorBlinking(blink) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = blink,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = if (active) activeTheme.textColor else activeTheme.textColor.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Caret Cursor styling choice
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Caret Shape layout representation",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                        )
                        val shapes = listOf("Line", "Block", "Underline")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            shapes.forEach { shape ->
                                val active = cursorStyleSelection == shape
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (active) activeTheme.caretColor.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (active) activeTheme.caretColor else Color(0x33858585),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { viewModel.setCursorStyle(shape) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = shape,
                                        style = TextStyle(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = if (active) activeTheme.textColor else activeTheme.textColor.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BETA iOS 26 GLASS ENGINE CONFIGURATION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isBetaInterface) Color(0xD90E1116) else activeTheme.sidebarBgColor),
                border = BorderStroke(1.dp, if (isBetaInterface) Color(0xFF8B5CF6).copy(alpha = 0.4f) else Color(0xFF2B2B2B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "🚀 iOS 26 GLASS & TARGET SYSTEMS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isBetaInterface) Color(0xFFA78BFA) else activeTheme.caretColor
                        )
                    )

                    // iOS 26 Beta Interface Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Beta iOS 26 Interface",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Enables Dynamic Island HUD, Glass reflection, and fluid liquid backdrops.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = isBetaInterface,
                            onCheckedChange = { viewModel.setBetaInterface(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isBetaInterface) Color(0xFF8B5CF6) else activeTheme.caretColor,
                                checkedTrackColor = if (isBetaInterface) Color(0xFF8B5CF6).copy(alpha = 0.3f) else activeTheme.caretColor.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Use Treehub Bundler Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use Treehub Bundler",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Uses Treehub packs instead of default darklua command logs processing pipeline.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = useTreehubBundler,
                            onCheckedChange = { viewModel.setUseTreehubBundler(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isBetaInterface) Color(0xFF10B981) else activeTheme.caretColor,
                                checkedTrackColor = if (isBetaInterface) Color(0xFF10B981).copy(alpha = 0.3f) else activeTheme.caretColor.copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }
                }
            }
        }

        // LOCAL DISK STORAGE CONFIGURATION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = activeTheme.sidebarBgColor),
                border = BorderStroke(1.dp, Color(0xFF2B2B2B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "MOBILE LOCAL DISK WORKSPACE",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use Storage Workspace",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Saves, opens, and compiles Roblox scripts directly in /storage/emulated/0/LuauWorkspace.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = useExternalStorage,
                            onCheckedChange = { checked ->
                                viewModel.setUseExternalStorage(checked, context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF38BDF8),
                                checkedTrackColor = Color(0xFF38BDF8).copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    if (useExternalStorage) {
                        viewModel.checkStoragePermission(context)
                        if (!hasStoragePermission) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x33EF4444), shape = RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFEF4444), shape = RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = Color(0xFFFCA5A5),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "STORAGE PERMISSION REQUIRED",
                                            style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFFCA5A5))
                                        )
                                    }
                                    Text(
                                        text = "To access Roblox Luau projects locally, grant the device storage reader permission guidelines.",
                                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFFFECACA), lineHeight = 12.sp)
                                    )

                                    Button(
                                        onClick = {
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                                try {
                                                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                                        data = android.net.Uri.parse("package:" + context.packageName)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                                    context.startActivity(intent)
                                                }
                                            } else {
                                                permissionLauncher.launch(
                                                    arrayOf(
                                                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                    )
                                                )
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("GRANT PERMISSION", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        } else {
                            viewModel.scanExternalWorkspace()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x3310B981), shape = RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF10B981), shape = RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFFA7F3D0),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Workspace synced at: /storage/emulated/0/LuauWorkspace",
                                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFFA7F3D0))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // MOBILE VS CODE PLUGINS CONFIGURATION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = activeTheme.sidebarBgColor),
                border = BorderStroke(1.dp, Color(0xFF2B2B2B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "VS CODE ENGINE EXTENSIONS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFA78BFA)
                        )
                    )

                    // Autocomplete Plugin Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Intellisense Auto-suggestions",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Enabled Roblox global methods context completion queries.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = enabledPlugins["autocomplete"] ?: false,
                            onCheckedChange = { viewModel.setPluginEnabled("autocomplete", it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFA78BFA),
                                checkedTrackColor = Color(0xFFA78BFA).copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Colorpicker Plugin Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quick Color Selector Bar",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Displays custom color palette drawer for Roblox Color3 items insertions.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = enabledPlugins["colorpicker"] ?: false,
                            onCheckedChange = { viewModel.setPluginEnabled("colorpicker", it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFA78BFA),
                                checkedTrackColor = Color(0xFFA78BFA).copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Darklua Plugin Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Darklua Code Bundler Compiler",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = activeTheme.textColor)
                            )
                            Text(
                                text = "Enables bundling and pre-processing modules for final builds.",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
                            )
                        }
                        Switch(
                            checked = enabledPlugins["darklua"] ?: false,
                            onCheckedChange = { viewModel.setPluginEnabled("darklua", it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFA78BFA),
                                checkedTrackColor = Color(0xFFA78BFA).copy(alpha = 0.3f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2B2B2B))

                    // Live active plugin logs
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "VS Code API Console Logs:",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = activeTheme.textColor.copy(alpha = 0.6f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(Color(0xFF0F1419), shape = RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFF2B2B2B), shape = RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(pluginLogs.reversed()) { log ->
                                    Text(
                                        text = log,
                                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = if (log.contains("[ERROR]")) Color(0xFFEF4444) else if (log.contains("[SUCCESS]")) Color(0xFF10B981) else Color(0xFFE2E8F0))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Safety spacer
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// VISUAL SYSTEM THEME PREVIEW CARD IMPLEMENTATION
@Composable
fun ThemePreviewCard(
    theme: CodeTheme,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = theme.backgroundColor),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) theme.caretColor else Color(0xFF2B2B2B)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // Window Header (Chrome-like)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.sidebarBgColor)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    // Chrome traffic lights indicators
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFFFF5F56), shape = RoundedCornerShape(2.5.dp)))
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFFFFBD2E), shape = RoundedCornerShape(2.5.dp)))
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFF27C93F), shape = RoundedCornerShape(2.5.dp)))
                }
                
                Text(
                    text = theme.name,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor.copy(alpha = 0.8f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isActive) theme.caretColor else Color.Transparent, shape = RoundedCornerShape(3.dp))
                )
            }
            
            // Miniature styled developer workspace panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(theme.backgroundColor)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                // Mock line indices
                Column(
                    modifier = Modifier.width(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..4) {
                        Text(
                            text = i.toString(),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 7.sp,
                                color = theme.lineNumberColor,
                                lineHeight = 8.sp
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.width(0.7.dp).fillMaxHeight().background(theme.lineNumberColor.copy(alpha = 0.15f)))
                Spacer(modifier = Modifier.width(6.dp))
                
                // Pure live token syntax highlighted mockup text representer
                Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                    // local player = 100
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("local ", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.keywordColor, fontWeight = FontWeight.Bold))
                        Text("p ", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.textColor))
                        Text("= ", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.keywordColor))
                        Text("24", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.numberColor))
                    }
                    
                    // task.spawn()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("task", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.tagColor))
                        Text(".", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.textColor))
                        Text("spawn", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.functionColor))
                        Text("(", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.textColor))
                        Text(")", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.textColor))
                    }

                    // -- monitor loops
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("-- state loop", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.commentColor))
                    }

                    // print("ok")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("print", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.tagColor))
                        Text("(", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.textColor))
                        Text("\"ok\"", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.stringColor))
                        Text(")", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = theme.textColor))
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean,
    theme: CodeTheme,
    onClick: () -> Unit
) {
    val barColor = if (isActive) theme.caretColor else Color.Transparent
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(3.dp)
                .background(barColor)
                .align(Alignment.CenterStart)
        )

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) theme.textColor else theme.textColor.copy(alpha = 0.4f),
            modifier = Modifier
                .size(22.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun ExplorerTabContent(
    files: List<CodeFile>,
    activeFileId: Int?,
    activeTheme: CodeTheme,
    onFileClick: (Int) -> Unit,
    onFileDelete: (Int) -> Unit,
    onCreateFileClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SCRIPTS IN WORKSPACE (${files.size})",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = activeTheme.textColor.copy(alpha = 0.5f)
                )
            )

            IconButton(
                onClick = onCreateFileClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new file",
                    tint = activeTheme.caretColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(files, key = { it.id }) { file ->
                val isFileActive = file.id == activeFileId
                val itemBgColor = if (isFileActive) activeTheme.backgroundColor else Color.Transparent
                val itemTextColor = if (isFileActive) activeTheme.textColor else activeTheme.textColor.copy(alpha = 0.7f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(itemBgColor)
                        .clickable { onFileClick(file.id) }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🌀",
                            fontSize = 12.sp
                        )

                        Text(
                            text = file.name,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = itemTextColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!file.isDefault) {
                        IconButton(
                            onClick = { onFileDelete(file.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete file",
                                tint = activeTheme.textColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTabContent(
    searchQuery: String,
    replaceQuery: String,
    activeTheme: CodeTheme,
    onSearchChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onReplaceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { Text("Find text", color = activeTheme.textColor.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = activeTheme.caretColor,
                unfocusedBorderColor = Color(0xFF2B2B2B),
                focusedTextColor = activeTheme.textColor,
                unfocusedTextColor = activeTheme.textColor
            ),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = replaceQuery,
            onValueChange = onReplaceChange,
            label = { Text("Replace with", color = activeTheme.textColor.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = activeTheme.caretColor,
                unfocusedBorderColor = Color(0xFF2B2B2B),
                focusedTextColor = activeTheme.textColor,
                unfocusedTextColor = activeTheme.textColor
            ),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onReplaceClick,
            enabled = searchQuery.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = activeTheme.caretColor),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(36.dp)
        ) {
            Text(
                text = "REPLACE ALL",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun ConsoleTabContent(
    consoleLog: String,
    activeTheme: CodeTheme,
    isConsoleRunning: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isConsoleRunning) "⚡ EXECUTING..." else "● VM IDLE READY",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isConsoleRunning) Color.Yellow else Color(0xFF4ADE80)
                )
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F1419))
                .border(1.dp, Color(0x3338BDF8), shape = RoundedCornerShape(6.dp))
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Text(
                text = consoleLog,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF38BDF8)
                )
            )
        }
    }
}

@Composable
fun BundleTabContent(
    viewModel: EditorViewModel,
    activeTheme: CodeTheme
) {
    val isBundling by viewModel.isBundling.collectAsState()
    val bundleSuccess by viewModel.bundleSuccess.collectAsState()
    val bundlerLogs by viewModel.bundlerLogs.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val isDarkluaDownloaded by viewModel.isDarkluaDownloaded.collectAsState()

    var entryFile by remember { mutableStateOf("main.luau") }
    var outputFile by remember { mutableStateOf("bundle.lua") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Entry Script Target",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
            )
            BasicTextField(
                value = entryFile,
                onValueChange = { entryFile = it },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                cursorBrush = SolidColor(activeTheme.caretColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F1419))
                    .border(1.dp, Color(0xFF2B2B2B), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Output Distribution file",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
            )
            BasicTextField(
                value = outputFile,
                onValueChange = { outputFile = it },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                cursorBrush = SolidColor(activeTheme.caretColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F1419))
                    .border(1.dp, Color(0xFF2B2B2B), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        if (isBundling) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!isDarkluaDownloaded) "Downloading standalone Darklua..." else "Bundling abstract AST nodes...",
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Yellow)
                    )
                    Text(
                        text = "${(downloadProgress * 100).toInt()}%",
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                    )
                }
                LinearProgressIndicator(
                    progress = downloadProgress,
                    color = Color.Yellow,
                    trackColor = Color(0x33FFA500),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }

        Button(
            onClick = { viewModel.buildRobloxBundle(entryFile, outputFile) },
            enabled = !isBundling,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isBundling) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "DARKLUA BUNDLING...",
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Build Bundle compiler execution action button icon label outline",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "BUILD BUNDLE (DARKLUA)",
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
            }
        }

        if (bundleSuccess != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (bundleSuccess == true) Color(0x2210B981) else Color(0x22EF4444),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        if (bundleSuccess == true) Color(0xFF10B981) else Color(0xFFEF4444),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = if (bundleSuccess == true) "✔ BUNDLE SUCCESS! Production file emitted." else "✗ BUNDLE FAILED! Check diagnostics log console.",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bundleSuccess == true) Color(0xFF4ADE80) else Color(0xFFF87171)
                    )
                )
            }
        }

        Text(
            text = "Diagnostics console / build logs:",
            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = activeTheme.textColor.copy(alpha = 0.5f))
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F1419))
                .border(1.dp, Color(0x11FFFFFF), shape = RoundedCornerShape(6.dp))
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                bundlerLogs.forEach { log ->
                    Text(
                        text = log,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = if (log.contains("[SUCCESS]")) Color(0xFF4ADE80)
                            else if (log.contains("[WARNING]")) Color(0xFFFBBF24)
                            else if (log.contains("[ERROR]")) Color(0xFFF87171)
                            else Color(0xFFE2E8F0)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun QuickSymbolsSuggestionsBar(
    activeTheme: CodeTheme,
    onSymbolClick: (String) -> Unit
) {
    val helperKeys = listOf(
        "Tab", "local ", "function ", "end", "task.wait(", "task.spawn(", "Vector3.new(", "game:GetService(", "{", "}", "[", "]", "(", ")", ";", ":", "=", "+", "-", "*", "/", "<", ">", "\"", "'", ",", "."
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(activeTheme.sidebarBgColor)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(helperKeys) { key ->
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(activeTheme.backgroundColor)
                        .border(1.dp, Color(0x1C858585), shape = RoundedCornerShape(4.dp))
                        .clickable { onSymbolClick(key) }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key.trim(),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = activeTheme.textColor
                        )
                    )
                }
            }
        }
    }
}

// Extension to return fallback warm tint
fun CodeTheme.carrot(): Color {
    return if (this.id == "elegant_dark") Color(0xFFFFA500) else this.caretColor
}

@Composable
fun QuickColorPickerBar(
    activeTheme: CodeTheme,
    onColorSelect: (String) -> Unit
) {
    val predefinedColors = listOf(
        Pair("Red", Color(0xFFFF3B30) to "Color3.fromRGB(255, 59, 48)"),
        Pair("Orange", Color(0xFFFF9500) to "Color3.fromRGB(255, 149, 0)"),
        Pair("Yellow", Color(0xFFFFCC00) to "Color3.fromRGB(255, 204, 0)"),
        Pair("Green", Color(0xFF34C759) to "Color3.fromRGB(52, 199, 89)"),
        Pair("Teal", Color(0xFF30B0C7) to "Color3.fromRGB(48, 176, 199)"),
        Pair("Blue", Color(0xFF007AFF) to "Color3.fromRGB(0, 122, 255)"),
        Pair("Indigo", Color(0xFF5856D6) to "Color3.fromRGB(88, 86, 214)"),
        Pair("Purple", Color(0xFFAF52DE) to "Color3.fromRGB(175, 82, 222)"),
        Pair("Pink", Color(0xFFFF2D55) to "Color3.fromRGB(255, 45, 85)"),
        Pair("Black", Color(0xFF1C1C1E) to "Color3.fromRGB(28, 28, 30)"),
        Pair("White", Color(0xFFFFFFFF) to "Color3.fromRGB(255, 255, 255)")
    )

    var rVal by remember { mutableStateOf(128) }
    var gVal by remember { mutableStateOf(0) }
    var bVal by remember { mutableStateOf(255) }
    var expandSliders by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(activeTheme.sidebarBgColor)
            .border(1.dp, Color(0x11FFFFFF))
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                     imageVector = Icons.Default.FormatPaint,
                     contentDescription = "Color Picker",
                     tint = Color(0xFFA78BFA),
                     modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "LUAU QUICK COLOR TOOL",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFFA78BFA)
                    )
                )
            }

            TextButton(
                onClick = { expandSliders = !expandSliders },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text(
                    text = if (expandSliders) "◄ PALETTE" else "CUSTOM RGB ►",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = activeTheme.caretColor
                    )
                )
            }
        }

        if (expandSliders) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("R: $rVal", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White), modifier = Modifier.width(36.dp))
                    Slider(
                        value = rVal.toFloat(),
                        onValueChange = { rVal = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.weight(1f).height(12.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("G: $gVal", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White), modifier = Modifier.width(36.dp))
                    Slider(
                        value = gVal.toFloat(),
                        onValueChange = { gVal = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.weight(1f).height(12.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("B: $bVal", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White), modifier = Modifier.width(36.dp))
                    Slider(
                        value = bVal.toFloat(),
                        onValueChange = { bVal = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.weight(1f).height(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp, 20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(rVal, gVal, bVal))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )

                    Button(
                        onClick = { onColorSelect("Color3.fromRGB($rVal, $gVal, $bVal)") },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Text("INSERT RGB CODE", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(predefinedColors) { colorItem ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorItem.second.first)
                            .clickable { onColorSelect(colorItem.second.second) }
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (colorItem.second.first == Color.Black) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun androidx.compose.foundation.layout.BoxScope.DynamicIslandOverlay(
    errorsCount: Int,
    isBundling: Boolean,
    bundleSuccess: Boolean?
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Dynamic size animation based on state
    val height by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (expanded) 85.dp else 42.dp,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "DynamicIslandHeight"
    )
    val width by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (expanded) 320.dp else 240.dp,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "DynamicIslandWidth"
    )
    
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .align(Alignment.TopCenter)
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(22.dp))
            .clickable { expanded = !expanded }
            // Thick dark glassmorphism
            .background(Color(0xE6090A0C))
            .border(
                BorderStroke(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        // Subtle diagonal glare reflection overlay simulating real liquid-glass physical texture
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
        )
        
        // Content rendering with animation
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isBundling) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(16.dp)) {
                        CircularProgressIndicator(
                            color = Color(0xFFA78BFA),
                            strokeWidth = 2.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Text(
                            text = "Treehub Packing...",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (expanded) {
                            Text(
                                text = "Integrating bytecode nodes safely",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Text("⚡", fontSize = 14.sp)
            } else if (errorsCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0xFFEF4444), shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("!", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(
                            text = "$errorsCount Luau Issue${if (errorsCount > 1) "s" else ""}",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (expanded) {
                            Text(
                                text = "Tap tooltip check lỗi to fix mistakes",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Text("🔴", fontSize = 12.sp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0xFF10B981), shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(
                            text = "Coding Smoothly",
                            color = Color(0xFF6EE7B7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (expanded) {
                            Text(
                                text = "Treehub packaging pipeline is healthy",
                                color = Color.Gray,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Text("🟢", fontSize = 12.sp)
            }
        }
    }
}
