package com.example.ui.editor

import androidx.compose.ui.graphics.Color

data class CodeTheme(
    val id: String,
    val name: String,
    val backgroundColor: Color,
    val textColor: Color,
    val lineNumberColor: Color,
    val caretColor: Color,
    val keywordColor: Color,
    val stringColor: Color,
    val commentColor: Color,
    val numberColor: Color,
    val tagColor: Color,
    val attributeColor: Color,
    val functionColor: Color,
    val selectedLineColor: Color,
    val bottomBarColor: Color,
    val sidebarBgColor: Color,
    val activeTabColor: Color,
    val inactiveTabColor: Color
) {
    companion object {
        val ElegantDark = CodeTheme(
            id = "elegant_dark",
            name = "Elegant Dark",
            backgroundColor = Color(0xFF1E1E1E), // editor block
            textColor = Color(0xFFD4D4D4),
            lineNumberColor = Color(0xFF858585),
            caretColor = Color(0xFF007ACC),
            keywordColor = Color(0xFFC586C0),
            stringColor = Color(0xFFCE9178),
            commentColor = Color(0xFF6A9955),
            numberColor = Color(0xFFB5CEA8),
            tagColor = Color(0xFF569CD6),
            attributeColor = Color(0xFF9CDCFE),
            functionColor = Color(0xFFDCDCAA),
            selectedLineColor = Color(0x4D264F78), // bg-[#264f78]/30 in ARGB (30% is 4D)
            bottomBarColor = Color(0xFF007ACC),
            sidebarBgColor = Color(0xFF181818),
            activeTabColor = Color(0xFF1E1E1E),
            inactiveTabColor = Color(0xFF181818)
        )

        val VSCodeDark = CodeTheme(
            id = "vscode_dark",
            name = "VS Code Dark",
            backgroundColor = Color(0xFF1E1E1E),
            textColor = Color(0xFFD4D4D4),
            lineNumberColor = Color(0xFF858585),
            caretColor = Color(0xFF007ACC),
            keywordColor = Color(0xFF569CD6),
            stringColor = Color(0xFFCE9178),
            commentColor = Color(0xFF6A9955),
            numberColor = Color(0xFFB5CEA8),
            tagColor = Color(0xFF569CD6),
            attributeColor = Color(0xFF9CDCFE),
            functionColor = Color(0xFFDCDCAA),
            selectedLineColor = Color(0xFF282828),
            bottomBarColor = Color(0xFF007ACC),
            sidebarBgColor = Color(0xFF252526),
            activeTabColor = Color(0xFF1E1E1E),
            inactiveTabColor = Color(0xFF2D2D2D)
        )

        val Monokai = CodeTheme(
            id = "monokai",
            name = "Monokai Pro",
            backgroundColor = Color(0xFF272822),
            textColor = Color(0xFFF8F8F2),
            lineNumberColor = Color(0xFF90908A),
            caretColor = Color(0xFFF92672),
            keywordColor = Color(0xFFF92672),
            stringColor = Color(0xFFE6DB74),
            commentColor = Color(0xFF75715E),
            numberColor = Color(0xFFAE81FF),
            tagColor = Color(0xFFF92672),
            attributeColor = Color(0xFFA6E22E),
            functionColor = Color(0xFF66D9EF),
            selectedLineColor = Color(0xFF3E3D32),
            bottomBarColor = Color(0xFF414339),
            sidebarBgColor = Color(0xFF1E1F1C),
            activeTabColor = Color(0xFF272822),
            inactiveTabColor = Color(0xFF34352F)
        )

        val OneDarkPro = CodeTheme(
            id = "one_dark_pro",
            name = "One Dark Pro",
            backgroundColor = Color(0xFF282C34),
            textColor = Color(0xFFABB2BF),
            lineNumberColor = Color(0xFF4B5263),
            caretColor = Color(0xFF528BFF),
            keywordColor = Color(0xFFC678DD),
            stringColor = Color(0xFF98C379),
            commentColor = Color(0xFF5C6370),
            numberColor = Color(0xFFD19A66),
            tagColor = Color(0xFFE06C75),
            attributeColor = Color(0xFFD19A66),
            functionColor = Color(0xFF61AFEF),
            selectedLineColor = Color(0xFF2C313C),
            bottomBarColor = Color(0xFF3E4451),
            sidebarBgColor = Color(0xFF21252B),
            activeTabColor = Color(0xFF282C34),
            inactiveTabColor = Color(0xFF21252B)
        )

        val GithubLight = CodeTheme(
            id = "github_light",
            name = "GitHub Light",
            backgroundColor = Color(0xFFF6F8FA),
            textColor = Color(0xFF24292E),
            lineNumberColor = Color(0xFF57606A),
            caretColor = Color(0xFF0969DA),
            keywordColor = Color(0xFFCF222E),
            stringColor = Color(0xFF0A3069),
            commentColor = Color(0xFF6E7781),
            numberColor = Color(0xFF0550AE),
            tagColor = Color(0xFF116329),
            attributeColor = Color(0xFF953800),
            functionColor = Color(0xFF8250DF),
            selectedLineColor = Color(0xFFEAEFF2),
            bottomBarColor = Color(0xFF0969DA),
            sidebarBgColor = Color(0xFFFFFFFF),
            activeTabColor = Color(0xFFF6F8FA),
            inactiveTabColor = Color(0xFFEAEFF2)
        )

        val themes = listOf(ElegantDark, VSCodeDark, OneDarkPro, Monokai, GithubLight)
    }
}
