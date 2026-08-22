package com.inspiredandroid.kai.build.terminal

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

const val COLOR_DEFAULT_FG = -1
const val COLOR_DEFAULT_BG = -2
const val COLOR_TRUECOLOR_FLAG = 0x01000000

/**
 * Packs 24-bit RGB into an Int representation for TerminalCell.
 */
fun packTrueColor(r: Int, g: Int, b: Int): Int {
    val cr = r.coerceIn(0, 255)
    val cg = g.coerceIn(0, 255)
    val cb = b.coerceIn(0, 255)
    return COLOR_TRUECOLOR_FLAG or (cr shl 16) or (cg shl 8) or cb
}

/** One cell in the terminal grid. Supports 16-color ANSI, 256-color, and 24-bit TrueColor RGB. */
@Immutable
data class TerminalCell(
    val char: Char = ' ',
    val fg: Int = COLOR_DEFAULT_FG,
    val bg: Int = COLOR_DEFAULT_BG,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val dim: Boolean = false,
    val inverse: Boolean = false,
    val strikethrough: Boolean = false,
)

/**
 * Immutable view of the screen for Compose. [revision] bumps on every change so
 * collectors recompose even when dimensions stay the same.
 */
@Immutable
data class TerminalSnapshot(
    val columns: Int,
    val rows: Int,
    /** Row-major cells, size = columns * rows (or including scrollback viewport). */
    val cells: List<TerminalCell>,
    val cursorCol: Int,
    val cursorRow: Int,
    val cursorVisible: Boolean,
    /** DECCKM state — decides how the key row encodes arrows. */
    val applicationCursorKeys: Boolean = false,
    /** What the running app asked to hear about touches, if anything. */
    val mouse: TerminalMouseState = TerminalMouseState(),
    val revision: Long,
    val hyperlinks: ImmutableList<String> = persistentListOf(),
    /** Total lines stored in history buffer. */
    val historySize: Int = 0,
    /** Current view scroll offset (0 = live screen bottom, >0 = scrolled up into history). */
    val scrollOffset: Int = 0,
) {
    fun cellAt(col: Int, row: Int): TerminalCell {
        if (col !in 0 until columns || row !in 0 until rows) return TerminalCell()
        return cells[row * columns + col]
    }

    companion object {
        fun blank(columns: Int = DEFAULT_COLUMNS, rows: Int = DEFAULT_ROWS) = TerminalSnapshot(
            columns = columns,
            rows = rows,
            cells = List(columns * rows) { TerminalCell() },
            cursorCol = 0,
            cursorRow = 0,
            cursorVisible = true,
            applicationCursorKeys = false,
            mouse = TerminalMouseState(),
            revision = 0L,
            hyperlinks = persistentListOf(),
            historySize = 0,
            scrollOffset = 0,
        )
    }
}

const val DEFAULT_COLUMNS = 80
const val DEFAULT_ROWS = 24

const val MIN_COLUMNS = 20
const val MAX_COLUMNS = 300
const val MIN_ROWS = 8
const val MAX_ROWS = 120
const val MAX_SCROLLBACK_LINES = 5000

