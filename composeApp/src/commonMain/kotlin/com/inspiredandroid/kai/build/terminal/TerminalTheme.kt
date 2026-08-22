package com.inspiredandroid.kai.build.terminal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Curated mobile terminal color themes optimized for OLED/LCD screens and contrast.
 */
@Immutable
enum class TerminalTheme(
    val id: String,
    val displayName: String,
    val palette: List<Color>,
    val background: Color,
    val foreground: Color,
) {
    TERMUX_BLACK(
        id = "termux",
        displayName = "Termux Black",
        palette = listOf(
            Color(0xFF000000), // 0: Black
            Color(0xFFCC0000), // 1: Red
            Color(0xFF4E9A06), // 2: Green
            Color(0xFFC4A000), // 3: Yellow
            Color(0xFF3465A4), // 4: Blue
            Color(0xFF75507B), // 5: Magenta
            Color(0xFF06989A), // 6: Cyan
            Color(0xFFD3D7CF), // 7: Light Gray
            Color(0xFF555753), // 8: Dark Gray
            Color(0xFFEF2929), // 9: Bright Red
            Color(0xFF8AE234), // 10: Bright Green
            Color(0xFFFCE94F), // 11: Bright Yellow
            Color(0xFF729FCF), // 12: Bright Blue
            Color(0xFFAD7FA8), // 13: Bright Magenta
            Color(0xFF34E2E2), // 14: Bright Cyan
            Color(0xFFEEEEEC), // 15: Bright White
        ),
        background = Color(0xFF000000),
        foreground = Color(0xFFFFFFFF),
    ),
    CLASSIC_DARK(
        id = "classic",
        displayName = "Classic Dark",
        palette = listOf(
            Color(0xFF0C0C0C),
            Color(0xFFC50F1F),
            Color(0xFF13A10E),
            Color(0xFFC19C00),
            Color(0xFF0037DA),
            Color(0xFF881798),
            Color(0xFF3A96DD),
            Color(0xFFCCCCCC),
            Color(0xFF767676),
            Color(0xFFE74856),
            Color(0xFF16C60C),
            Color(0xFFF9F1A5),
            Color(0xFF3B78FF),
            Color(0xFFB4009E),
            Color(0xFF61D6D6),
            Color(0xFFF2F2F2),
        ),
        background = Color(0xFF0C0C0C),
        foreground = Color(0xFFCCCCCC),
    ),
    DRACULA(
        id = "dracula",
        displayName = "Dracula",
        palette = listOf(
            Color(0xFF21222C),
            Color(0xFFFF5555),
            Color(0xFF50FA7B),
            Color(0xFFF1FA8C),
            Color(0xFFBD93F9),
            Color(0xFFFF79C6),
            Color(0xFF8BE9FD),
            Color(0xFFF8F8F2),
            Color(0xFF6272A4),
            Color(0xFFFF6E6E),
            Color(0xFF69FF94),
            Color(0xFFFFFFA5),
            Color(0xFFD6ACFF),
            Color(0xFFFF92DF),
            Color(0xFFA4FFFF),
            Color(0xFFFFFFFF),
        ),
        background = Color(0xFF1E1F29),
        foreground = Color(0xFFF8F8F2),
    ),
    CATPPUCCIN_MOCHA(
        id = "catppuccin",
        displayName = "Catppuccin",
        palette = listOf(
            Color(0xFF181825),
            Color(0xFFF38BA8),
            Color(0xFFA6E3A1),
            Color(0xFFF9E2AF),
            Color(0xFF89B4FA),
            Color(0xFFF5C2E7),
            Color(0xFF94E2D5),
            Color(0xFFBAC2DE),
            Color(0xFF585B70),
            Color(0xFFF38BA8),
            Color(0xFFA6E3A1),
            Color(0xFFF9E2AF),
            Color(0xFF89B4FA),
            Color(0xFFF5C2E7),
            Color(0xFF94E2D5),
            Color(0xFFA6ADC8),
        ),
        background = Color(0xFF11111B),
        foreground = Color(0xFFCDD6F4),
    ),
    NORD(
        id = "nord",
        displayName = "Nord",
        palette = listOf(
            Color(0xFF2E3440),
            Color(0xFFBF616A),
            Color(0xFFA3BE8C),
            Color(0xFFEBCB8B),
            Color(0xFF81A1C1),
            Color(0xFFB48EAD),
            Color(0xFF88C0D0),
            Color(0xFFECEFF4),
            Color(0xFF4C566A),
            Color(0xFFBF616A),
            Color(0xFFA3BE8C),
            Color(0xFFEBCB8B),
            Color(0xFF81A1C1),
            Color(0xFFB48EAD),
            Color(0xFF8FBCBB),
            Color(0xFFECEFF4),
        ),
        background = Color(0xFF242933),
        foreground = Color(0xFFD8DEE9),
    ),
    MONOKAI(
        id = "monokai",
        displayName = "Monokai",
        palette = listOf(
            Color(0xFF1A1A1A),
            Color(0xFFF92672),
            Color(0xFFA6E22E),
            Color(0xFFFD971F),
            Color(0xFF66D9EF),
            Color(0xFFAE81FF),
            Color(0xFF38CCD1),
            Color(0xFFF8F8F2),
            Color(0xFF75715E),
            Color(0xFFF92672),
            Color(0xFFA6E22E),
            Color(0xFFE6DB74),
            Color(0xFF66D9EF),
            Color(0xFFAE81FF),
            Color(0xFF38CCD1),
            Color(0xFFF8F8F2),
        ),
        background = Color(0xFF181818),
        foreground = Color(0xFFF8F8F2),
    ),
    MATRIX_GREEN(
        id = "matrix",
        displayName = "Matrix",
        palette = listOf(
            Color(0xFF0D1117),
            Color(0xFF00FF66),
            Color(0xFF00FF41),
            Color(0xFF33FF33),
            Color(0xFF00CC00),
            Color(0xFF00AA00),
            Color(0xFF00FF88),
            Color(0xFF00FF66),
            Color(0xFF005500),
            Color(0xFF00FF66),
            Color(0xFF00FF41),
            Color(0xFF66FF66),
            Color(0xFF00DD00),
            Color(0xFF00BB00),
            Color(0xFF00FF99),
            Color(0xFF99FF99),
        ),
        background = Color(0xFF050B05),
        foreground = Color(0xFF00FF66),
    ),
    ARI_NEON(
        id = "ari_neon",
        displayName = "Ari Neon",
        palette = listOf(
            Color(0xFF0B0F19), // 0: Midnight Obsidian BG
            Color(0xFFFF3366), // 1: Crimson Red
            Color(0xFF00FFCC), // 2: Neon Mint
            Color(0xFFFFD54F), // 3: Radiant Amber
            Color(0xFF00E5FF), // 4: Electric Cyan
            Color(0xFFB388FF), // 5: Radiant Violet
            Color(0xFF7000FF), // 6: Deep Purple
            Color(0xFFE0E6ED), // 7: Clean Platinum
            Color(0xFF475569), // 8: Slate Gray
            Color(0xFFFF5588), // 9: Bright Crimson
            Color(0xFF33FFDD), // 10: Bright Mint
            Color(0xFFFFDD33), // 11: Bright Yellow
            Color(0xFF33EBFF), // 12: Bright Cyan
            Color(0xFFCC99FF), // 13: Bright Violet
            Color(0xFF9933FF), // 14: Accent Purple
            Color(0xFFFFFFFF), // 15: Pure White
        ),
        background = Color(0xFF0B0F19),
        foreground = Color(0xFFE0E6ED),
    ),
    SOLARIZED_DARK(
        id = "solarized",
        displayName = "Solarized",
        palette = listOf(
            Color(0xFF073642),
            Color(0xFFDC322F),
            Color(0xFF859900),
            Color(0xFFB58900),
            Color(0xFF268BD2),
            Color(0xFFD33682),
            Color(0xFF2AA198),
            Color(0xFFEEE8D5),
            Color(0xFF002B36),
            Color(0xFFCB4B16),
            Color(0xFF586E75),
            Color(0xFF657B83),
            Color(0xFF839496),
            Color(0xFF6C71C4),
            Color(0xFF93A1A1),
            Color(0xFFFDF6E3),
        ),
        background = Color(0xFF002B36),
        foreground = Color(0xFF839496),
    );

    fun resolveColor(code: Int, isBg: Boolean): Color {
        if (code == COLOR_DEFAULT_FG) return foreground
        if (code == COLOR_DEFAULT_BG) return background
        if ((code and COLOR_TRUECOLOR_FLAG) != 0) {
            val r = (code shr 16) and 0xFF
            val g = (code shr 8) and 0xFF
            val b = code and 0xFF
            return Color(r, g, b)
        }
        if (code in 0..15) {
            return palette.getOrElse(code) { if (isBg) background else foreground }
        }
        if (code in 16..231) {
            // 6x6x6 color cube
            val c = code - 16
            val r = (c / 36) * 51
            val g = ((c % 36) / 6) * 51
            val b = (c % 6) * 51
            return Color(r, g, b)
        }
        if (code in 232..255) {
            // 24-step grayscale ramp
            val gray = 8 + (code - 232) * 10
            return Color(gray, gray, gray)
        }
        return if (isBg) background else foreground
    }

    companion object {
        val DEFAULT = TERMUX_BLACK
        fun fromId(id: String?): TerminalTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
