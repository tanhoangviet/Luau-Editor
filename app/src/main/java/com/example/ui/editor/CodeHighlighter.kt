package com.example.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CodeSyntaxHighlightTransformation(
    private val language: String,
    private val theme: CodeTheme
) : VisualTransformation {
    private var lastText: String? = null
    private var lastResult: TransformedText? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val stringContent = text.text
        if (stringContent == lastText && lastResult != null) {
            return lastResult!!
        }
        val highlighted = highlightCode(stringContent, language, theme)
        val transformed = TransformedText(highlighted, OffsetMapping.Identity)
        lastText = stringContent
        lastResult = transformed
        return transformed
    }
}

fun highlightCode(text: String, language: String, theme: CodeTheme): AnnotatedString {
    val builder = AnnotatedString.Builder(text)
    val length = text.length
    if (length == 0) return builder.toAnnotatedString()

    // Keep track of characters that have been styled to prevent keyword overlaps in strings/comments
    val styled = BooleanArray(length)

    // 1. Luau Block Comments & Single line comments
    val commentRegexes = listOf(
        Regex("--\\[\\[[\\s\\S]*?\\]\\]"), // Block comment --[[ ... ]]
        Regex("--.*")                      // Single line comment -- ...
    )

    for (regex in commentRegexes) {
        regex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = (match.range.last + 1).coerceAtMost(length)
            if (start < end && !styled.sliceArray(start until end).any { it }) {
                builder.addStyle(SpanStyle(color = theme.commentColor), start, end)
                for (i in start until end) styled[i] = true
            }
        }
    }

    // 2. Luau Strings: single quotes, double quotes, and long bracket block strings [[ ... ]]
    val stringRegexes = listOf(
        Regex("\"([^\"\\\\]|\\\\.)*\""),
        Regex("'([^'\\\\]|\\\\.)*'"),
        Regex("\\[\\[[\\s\\S]*?\\]\\]")
    )

    for (regex in stringRegexes) {
        regex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = (match.range.last + 1).coerceAtMost(length)
            if (start < end && !styled.sliceArray(start until end).any { it }) {
                builder.addStyle(SpanStyle(color = theme.stringColor), start, end)
                for (i in start until end) styled[i] = true
            }
        }
    }

    // 3. Luau Keywords (luau syntax structure)
    val keywords = listOf(
        "and", "break", "do", "else", "elseif", "end", "false", "for", "function", 
        "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true", 
        "until", "while", "export", "type", "typeof", "declare", "self"
    )

    val keywordPattern = "\\b(${keywords.joinToString("|")})\\b"
    Regex(keywordPattern).findAll(text).forEach { match ->
        val start = match.range.first
        val end = (match.range.last + 1).coerceAtMost(length)
        if (start < end && !styled.sliceArray(start until end).any { it }) {
            builder.addStyle(SpanStyle(color = theme.keywordColor, fontWeight = FontWeight.Bold), start, end)
            for (i in start until end) styled[i] = true
        }
    }

    // 4. Luau Builtins / Library calls (Roblox & general Lua API context)
    val builtins = listOf(
        "game", "workspace", "script", "Instance", "Vector3", "UDim2", "Color3", 
        "task", "math", "table", "string", "coroutine", "debug", "os", "utf8", 
        "require", "print", "warn", "error", "pcall", "xpcall", "next", "pairs", 
        "ipairs", "tonumber", "tostring", "Enum", "RaycastParams", "BasePart", "Humanoid", "Player"
    )

    val builtinPattern = "\\b(${builtins.joinToString("|")})\\b"
    Regex(builtinPattern).findAll(text).forEach { match ->
        val start = match.range.first
        val end = (match.range.last + 1).coerceAtMost(length)
        if (start < end && !styled.sliceArray(start until end).any { it }) {
            builder.addStyle(SpanStyle(color = theme.tagColor, fontWeight = FontWeight.Medium), start, end)
            for (i in start until end) styled[i] = true
        }
    }

    // 5. Types / Type Casts (e.g., number, boolean, string, void, thread, vector)
    val types = listOf(
        "number", "boolean", "string", "thread", "table", "any", "void", "unknown", 
        "never", "ControllerType", "CharacterState", "RaycastInfo"
    )
    val typesPattern = "\\b(${types.joinToString("|")})\\b"
    Regex(typesPattern).findAll(text).forEach { match ->
        val start = match.range.first
        val end = (match.range.last + 1).coerceAtMost(length)
        if (start < end && !styled.sliceArray(start until end).any { it }) {
            builder.addStyle(SpanStyle(color = theme.attributeColor), start, end)
            for (i in start until end) styled[i] = true
        }
    }

    // 6. Functions in Luau: identifier before parenthesis
    Regex("\\b([a-zA-Z_]\\w*)(?=\\s*\\()").findAll(text).forEach { match ->
        val start = match.range.first
        val end = (match.range.last + 1).coerceAtMost(length)
        if (start < end && !styled.sliceArray(start until end).any { it }) {
            builder.addStyle(SpanStyle(color = theme.functionColor), start, end)
            for (i in start until end) styled[i] = true
        }
    }

    // 7. Numbers (all float/decimals, integers)
    Regex("\\b\\d+(\\.\\d+)?\\b").findAll(text).forEach { match ->
        val start = match.range.first
        val end = (match.range.last + 1).coerceAtMost(length)
        if (start < end && !styled.sliceArray(start until end).any { it }) {
            builder.addStyle(SpanStyle(color = theme.numberColor), start, end)
            for (i in start until end) styled[i] = true
        }
    }

    // Pair code font-family styling
    builder.addStyle(
        style = SpanStyle(fontFamily = FontFamily.Monospace),
        start = 0,
        end = length
    )

    return builder.toAnnotatedString()
}
