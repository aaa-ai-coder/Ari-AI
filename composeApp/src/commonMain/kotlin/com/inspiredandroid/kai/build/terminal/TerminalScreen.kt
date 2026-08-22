package com.inspiredandroid.kai.build.terminal

import kotlinx.collections.immutable.toImmutableList

/**
 * High-performance Termux-grade character-cell screen buffer + scrollback history.
 * Supports 24-bit TrueColor RGB, 256-color palette, 16 ANSI colors, SGR styling attributes,
 * alternate screen buffer (for vim/nano/htop/tmux), and up to 5,000 lines of scrollback.
 */
class TerminalScreen(
    columns: Int = DEFAULT_COLUMNS,
    rows: Int = DEFAULT_ROWS,
) {
    var columns: Int = columns.coerceAtLeast(1)
        private set
    var rows: Int = rows.coerceAtLeast(1)
        private set

    // Primary screen buffers
    private var chars = CharArray(this.columns * this.rows) { ' ' }
    private var fg = IntArray(this.columns * this.rows) { COLOR_DEFAULT_FG }
    private var bg = IntArray(this.columns * this.rows) { COLOR_DEFAULT_BG }
    private var bold = BooleanArray(this.columns * this.rows)
    private var italic = BooleanArray(this.columns * this.rows)
    private var underline = BooleanArray(this.columns * this.rows)
    private var dim = BooleanArray(this.columns * this.rows)
    private var inverse = BooleanArray(this.columns * this.rows)
    private var strikethrough = BooleanArray(this.columns * this.rows)

    // Alternate screen buffers (for TUI apps: vim, nano, htop, etc.)
    private var isAltScreen: Boolean = false
    private var altChars = CharArray(this.columns * this.rows) { ' ' }
    private var altFg = IntArray(this.columns * this.rows) { COLOR_DEFAULT_FG }
    private var altBg = IntArray(this.columns * this.rows) { COLOR_DEFAULT_BG }
    private var altBold = BooleanArray(this.columns * this.rows)
    private var altItalic = BooleanArray(this.columns * this.rows)
    private var altUnderline = BooleanArray(this.columns * this.rows)
    private var altDim = BooleanArray(this.columns * this.rows)
    private var altInverse = BooleanArray(this.columns * this.rows)
    private var altStrikethrough = BooleanArray(this.columns * this.rows)

    // Scrollback history (only collected in primary buffer)
    private val scrollbackHistory = ArrayDeque<List<TerminalCell>>()

    /** User scroll offset in history buffer (0 = bottom / active screen, >0 = scrolled up). */
    var scrollOffset: Int = 0
        private set

    var cursorCol: Int = 0
        private set
    var cursorRow: Int = 0
        private set
    var cursorVisible: Boolean = true
        private set

    // Saved cursor state (DEC / ANSI)
    private var savedCursorCol: Int = 0
    private var savedCursorRow: Int = 0
    private var savedFg: Int = COLOR_DEFAULT_FG
    private var savedBg: Int = COLOR_DEFAULT_BG
    private var savedBold: Boolean = false
    private var savedItalic: Boolean = false
    private var savedUnderline: Boolean = false

    // Scroll region (DECSTBM)
    private var scrollTop: Int = 0
    private var scrollBottom: Int = rows - 1

    /** DECCKM (private mode 1) */
    var applicationCursorKeys: Boolean = false
        private set

    /** Mouse reporting */
    private var mouseClick: Boolean = false
    private var mouseButtonMotion: Boolean = false
    private var mouseAnyMotion: Boolean = false
    private var mouseSgr: Boolean = false

    internal val mouseState: TerminalMouseState
        get() = TerminalMouseState(
            tracking = when {
                mouseAnyMotion -> TerminalMouseTracking.AnyMotion
                mouseButtonMotion -> TerminalMouseTracking.ButtonMotion
                mouseClick -> TerminalMouseTracking.Click
                else -> TerminalMouseTracking.None
            },
            encoding = if (mouseSgr) TerminalMouseEncoding.Sgr else TerminalMouseEncoding.X10,
        )

    // Current pen attributes
    var currentFg: Int = COLOR_DEFAULT_FG
        private set
    var currentBg: Int = COLOR_DEFAULT_BG
        private set
    var currentBold: Boolean = false
        private set
    var currentItalic: Boolean = false
        private set
    var currentUnderline: Boolean = false
        private set
    var currentDim: Boolean = false
        private set
    var currentInverse: Boolean = false
        private set
    var currentStrikethrough: Boolean = false
        private set

    private val hyperlinks = LinkedHashMap<String, String>()
    private var revision: Long = 0L
    private val parser = VtParser(this)

    fun write(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size - offset) {
        if (length <= 0) return
        val text = bytes.decodeToString(offset, offset + length, throwOnInvalidSequence = false)
        if (text.isNotEmpty()) {
            parser.feed(text)
            revision++
        }
    }

    fun writeText(text: String) {
        if (text.isEmpty()) return
        parser.feed(text)
        revision++
    }

    fun scrollHistory(delta: Int) {
        val maxOffset = if (isAltScreen) 0 else scrollbackHistory.size
        scrollOffset = (scrollOffset + delta).coerceIn(0, maxOffset)
        revision++
    }

    fun scrollToBottom() {
        if (scrollOffset != 0) {
            scrollOffset = 0
            revision++
        }
    }

    fun setScrollRegion(top: Int, bottom: Int) {
        scrollTop = top.coerceIn(0, rows - 1)
        scrollBottom = bottom.coerceIn(scrollTop, rows - 1)
    }

    fun resetScrollRegion() {
        scrollTop = 0
        scrollBottom = rows - 1
    }

    fun saveCursor() {
        savedCursorCol = cursorCol
        savedCursorRow = cursorRow
        savedFg = currentFg
        savedBg = currentBg
        savedBold = currentBold
        savedItalic = currentItalic
        savedUnderline = currentUnderline
    }

    fun restoreCursor() {
        cursorCol = savedCursorCol.coerceIn(0, columns - 1)
        cursorRow = savedCursorRow.coerceIn(0, rows - 1)
        currentFg = savedFg
        currentBg = savedBg
        currentBold = savedBold
        currentItalic = savedItalic
        currentUnderline = savedUnderline
    }

    fun enterAltScreen() {
        if (!isAltScreen) {
            isAltScreen = true
            scrollOffset = 0
            saveCursor()
            clearScreenBuffer(altChars, altFg, altBg, altBold, altItalic, altUnderline, altDim, altInverse, altStrikethrough)
            cursorCol = 0
            cursorRow = 0
            revision++
        }
    }

    fun exitAltScreen() {
        if (isAltScreen) {
            isAltScreen = false
            scrollOffset = 0
            restoreCursor()
            revision++
        }
    }

    fun resize(newColumns: Int, newRows: Int) {
        val nc = newColumns.coerceIn(MIN_COLUMNS, MAX_COLUMNS)
        val nr = newRows.coerceIn(MIN_ROWS, MAX_ROWS)
        if (nc == columns && nr == rows) return

        val newChars = CharArray(nc * nr) { ' ' }
        val newFg = IntArray(nc * nr) { COLOR_DEFAULT_FG }
        val newBg = IntArray(nc * nr) { COLOR_DEFAULT_BG }
        val newBold = BooleanArray(nc * nr)
        val newItalic = BooleanArray(nc * nr)
        val newUnderline = BooleanArray(nc * nr)
        val newDim = BooleanArray(nc * nr)
        val newInverse = BooleanArray(nc * nr)
        val newStrikethrough = BooleanArray(nc * nr)

        val copyCols = minOf(columns, nc)
        val copyRows = minOf(rows, nr)
        for (row in 0 until copyRows) {
            for (col in 0 until copyCols) {
                val oi = row * columns + col
                val ni = row * nc + col
                newChars[ni] = activeChars()[oi]
                newFg[ni] = activeFg()[oi]
                newBg[ni] = activeBg()[oi]
                newBold[ni] = activeBold()[oi]
                newItalic[ni] = activeItalic()[oi]
                newUnderline[ni] = activeUnderline()[oi]
                newDim[ni] = activeDim()[oi]
                newInverse[ni] = activeInverse()[oi]
                newStrikethrough[ni] = activeStrikethrough()[oi]
            }
        }

        columns = nc
        rows = nr
        resetScrollRegion()

        if (isAltScreen) {
            altChars = newChars
            altFg = newFg
            altBg = newBg
            altBold = newBold
            altItalic = newItalic
            altUnderline = newUnderline
            altDim = newDim
            altInverse = newInverse
            altStrikethrough = newStrikethrough
        } else {
            chars = newChars
            fg = newFg
            bg = newBg
            bold = newBold
            italic = newItalic
            underline = newUnderline
            dim = newDim
            inverse = newInverse
            strikethrough = newStrikethrough
        }

        cursorCol = cursorCol.coerceIn(0, columns - 1)
        cursorRow = cursorRow.coerceIn(0, rows - 1)
        revision++
    }

    fun clear() {
        clearScreenBuffer(activeChars(), activeFg(), activeBg(), activeBold(), activeItalic(), activeUnderline(), activeDim(), activeInverse(), activeStrikethrough())
        cursorCol = 0
        cursorRow = 0
        resetSgr()
        cursorVisible = true
        applicationCursorKeys = false
        mouseClick = false
        mouseButtonMotion = false
        mouseAnyMotion = false
        mouseSgr = false
        hyperlinks.clear()
        revision++
    }

    fun clearScrollback() {
        scrollbackHistory.clear()
        scrollOffset = 0
        revision++
    }

    private fun clearScreenBuffer(
        cArr: CharArray,
        fArr: IntArray,
        bArr: IntArray,
        boldArr: BooleanArray,
        italicArr: BooleanArray,
        underlineArr: BooleanArray,
        dimArr: BooleanArray,
        inverseArr: BooleanArray,
        strikethroughArr: BooleanArray,
    ) {
        for (i in cArr.indices) {
            cArr[i] = ' '
            fArr[i] = COLOR_DEFAULT_FG
            bArr[i] = COLOR_DEFAULT_BG
            boldArr[i] = false
            italicArr[i] = false
            underlineArr[i] = false
            dimArr[i] = false
            inverseArr[i] = false
            strikethroughArr[i] = false
        }
    }

    fun snapshot(): TerminalSnapshot {
        val totalCells = columns * rows
        val cells = ArrayList<TerminalCell>(totalCells)

        val curChars = activeChars()
        val curFg = activeFg()
        val curBg = activeBg()
        val curBold = activeBold()
        val curItalic = activeItalic()
        val curUnderline = activeUnderline()
        val curDim = activeDim()
        val curInverse = activeInverse()
        val curStrikethrough = activeStrikethrough()

        val historyCount = if (isAltScreen) 0 else scrollbackHistory.size
        val effectiveOffset = scrollOffset.coerceIn(0, historyCount)

        if (effectiveOffset == 0 || isAltScreen) {
            // Live active screen
            for (i in 0 until totalCells) {
                cells.add(
                    TerminalCell(
                        char = curChars[i],
                        fg = curFg[i],
                        bg = curBg[i],
                        bold = curBold[i],
                        italic = curItalic[i],
                        underline = curUnderline[i],
                        dim = curDim[i],
                        inverse = curInverse[i],
                        strikethrough = curStrikethrough[i],
                    ),
                )
            }
        } else {
            // Scrolled up into scrollback history
            val startHistIndex = historyCount - effectiveOffset
            val historySliceEnd = minOf(historyCount, startHistIndex + rows)
            var linesRendered = 0

            for (hIdx in startHistIndex until historySliceEnd) {
                val histLine = scrollbackHistory[hIdx]
                for (col in 0 until columns) {
                    cells.add(histLine.getOrElse(col) { TerminalCell() })
                }
                linesRendered++
            }

            // Fill remaining rows from the top of the active screen
            val remainingRows = rows - linesRendered
            for (r in 0 until remainingRows) {
                for (c in 0 until columns) {
                    val idx = r * columns + c
                    cells.add(
                        TerminalCell(
                            char = curChars[idx],
                            fg = curFg[idx],
                            bg = curBg[idx],
                            bold = curBold[idx],
                            italic = curItalic[idx],
                            underline = curUnderline[idx],
                            dim = curDim[idx],
                            inverse = curInverse[idx],
                            strikethrough = curStrikethrough[idx],
                        ),
                    )
                }
            }
        }

        return TerminalSnapshot(
            columns = columns,
            rows = rows,
            cells = cells,
            cursorCol = cursorCol.coerceIn(0, (columns - 1).coerceAtLeast(0)),
            cursorRow = cursorRow.coerceIn(0, (rows - 1).coerceAtLeast(0)),
            cursorVisible = cursorVisible && (effectiveOffset == 0),
            applicationCursorKeys = applicationCursorKeys,
            mouse = mouseState,
            revision = revision,
            hyperlinks = hyperlinks.values.toImmutableList(),
            historySize = historyCount,
            scrollOffset = effectiveOffset,
        )
    }

    // --- Active buffer helpers ------------------------------------------------
    private fun activeChars() = if (isAltScreen) altChars else chars
    private fun activeFg() = if (isAltScreen) altFg else fg
    private fun activeBg() = if (isAltScreen) altBg else bg
    private fun activeBold() = if (isAltScreen) altBold else bold
    private fun activeItalic() = if (isAltScreen) altItalic else italic
    private fun activeUnderline() = if (isAltScreen) altUnderline else underline
    private fun activeDim() = if (isAltScreen) altDim else dim
    private fun activeInverse() = if (isAltScreen) altInverse else inverse
    private fun activeStrikethrough() = if (isAltScreen) altStrikethrough else strikethrough

    // --- Hyperlink Management -------------------------------------------------
    internal fun noteHyperlink(uri: String): Boolean {
        val trimmed = uri.trim().trimEnd('\u0000', '\r', '\n', ' ')
        if (trimmed.isEmpty()) return false
        var url = URL_REGEX.find(trimmed)?.value ?: trimmed
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false
        url = url.trimEnd('.', ',', ';', ':', ')', ']', '"', '\'')
        val key = hyperlinkKey(url)
        if (key.isEmpty()) return false
        val existing = hyperlinks[key]
        if (existing != null && existing.length >= url.length) return false
        if (existing != null) hyperlinks.remove(key)
        hyperlinks[key] = url
        while (hyperlinks.size > MAX_HYPERLINKS) {
            val oldest = hyperlinks.keys.first()
            hyperlinks.remove(oldest)
        }
        revision++
        return true
    }

    fun clearHyperlinks() {
        if (hyperlinks.isEmpty()) return
        hyperlinks.clear()
        revision++
    }

    internal fun noteTextUrls(text: String): Boolean {
        var added = false
        URL_REGEX.findAll(text).forEach { if (noteHyperlink(it.value)) added = true }
        return added
    }

    private companion object {
        const val MAX_HYPERLINKS = 5
        val URL_REGEX = Regex("""https?://[^\s\u001b"'<>)\]]+""")
        fun hyperlinkKey(url: String): String {
            val noFrag = url.substringBefore('#')
            val base = noFrag.substringBefore('?')
            return base.trimEnd('/')
        }
    }

    // --- Character & Cursor Operations ---------------------------------------
    internal fun putChar(ch: Char) {
        when (ch) {
            '\n' -> {
                lineFeed()
                return
            }
            '\r' -> {
                cursorCol = 0
                return
            }
            '\b' -> {
                if (cursorCol > 0) cursorCol--
                return
            }
            '\t' -> {
                val next = ((cursorCol / 8) + 1) * 8
                cursorCol = next.coerceAtMost(columns - 1)
                return
            }
            '\u0007' -> return
        }
        if (ch.code < 32 && ch != '\u001b') return

        if (cursorCol >= columns) {
            cursorCol = 0
            lineFeed()
        }

        val curChars = activeChars()
        val curFg = activeFg()
        val curBg = activeBg()
        val curBold = activeBold()
        val curItalic = activeItalic()
        val curUnderline = activeUnderline()
        val curDim = activeDim()
        val curInverse = activeInverse()
        val curStrikethrough = activeStrikethrough()

        val i = index(cursorCol, cursorRow)
        curChars[i] = ch
        curFg[i] = currentFg
        curBg[i] = currentBg
        curBold[i] = currentBold
        curItalic[i] = currentItalic
        curUnderline[i] = currentUnderline
        curDim[i] = currentDim
        curInverse[i] = currentInverse
        curStrikethrough[i] = currentStrikethrough
        cursorCol++
    }

    internal fun lineFeed() {
        if (cursorRow < scrollBottom) {
            cursorRow++
        } else {
            scrollUp()
        }
    }

    internal fun reverseIndex() {
        if (cursorRow > scrollTop) {
            cursorRow--
        } else {
            scrollDown()
        }
    }

    internal fun setCursor(col: Int, row: Int) {
        cursorCol = col.coerceIn(0, columns - 1)
        cursorRow = row.coerceIn(0, rows - 1)
    }

    internal fun moveCursor(dCol: Int, dRow: Int) {
        setCursor(cursorCol + dCol, cursorRow + dRow)
    }

    internal fun setCursorVisible(visible: Boolean) {
        cursorVisible = visible
    }

    internal fun setApplicationCursorKeys(enable: Boolean) {
        applicationCursorKeys = enable
    }

    internal fun setMouseTracking(mode: Int, enable: Boolean) {
        when (mode) {
            1000 -> mouseClick = enable
            1002 -> mouseButtonMotion = enable
            1003 -> mouseAnyMotion = enable
            else -> return
        }
        revision++
    }

    internal fun setMouseSgrEncoding(enable: Boolean) {
        mouseSgr = enable
        revision++
    }

    internal fun setSgr(params: List<Int>) {
        if (params.isEmpty()) {
            resetSgr()
            return
        }
        var i = 0
        while (i < params.size) {
            when (val p = params[i]) {
                0 -> resetSgr()
                1 -> currentBold = true
                2 -> currentDim = true
                3 -> currentItalic = true
                4 -> currentUnderline = true
                7 -> currentInverse = true
                9 -> currentStrikethrough = true
                21, 24 -> currentUnderline = false
                22 -> {
                    currentBold = false
                    currentDim = false
                }
                23 -> currentItalic = false
                27 -> currentInverse = false
                29 -> currentStrikethrough = false
                39 -> currentFg = COLOR_DEFAULT_FG
                49 -> currentBg = COLOR_DEFAULT_BG
                in 30..37 -> currentFg = p - 30
                in 90..97 -> currentFg = p - 90 + 8
                in 40..47 -> currentBg = p - 40
                in 100..107 -> currentBg = p - 100 + 8
                38 -> {
                    if (i + 2 < params.size && params[i + 1] == 5) {
                        // 256-color: 38;5;index
                        currentFg = params[i + 2].coerceIn(0, 255)
                        i += 2
                    } else if (i + 4 < params.size && params[i + 1] == 2) {
                        // TrueColor 24-bit RGB: 38;2;r;g;b
                        currentFg = packTrueColor(params[i + 2], params[i + 3], params[i + 4])
                        i += 4
                    }
                }
                48 -> {
                    if (i + 2 < params.size && params[i + 1] == 5) {
                        // 256-color: 48;5;index
                        currentBg = params[i + 2].coerceIn(0, 255)
                        i += 2
                    } else if (i + 4 < params.size && params[i + 1] == 2) {
                        // TrueColor 24-bit RGB: 48;2;r;g;b
                        currentBg = packTrueColor(params[i + 2], params[i + 3], params[i + 4])
                        i += 4
                    }
                }
            }
            i++
        }
    }

    internal fun eraseInDisplay(mode: Int) {
        when (mode) {
            0 -> clearRange(index(cursorCol, cursorRow), columns * rows)
            1 -> clearRange(0, index(cursorCol, cursorRow) + 1)
            2 -> clearRange(0, columns * rows)
            3 -> {
                // Erase saved scrollback lines
                clearRange(0, columns * rows)
                clearScrollback()
            }
        }
    }

    internal fun eraseInLine(mode: Int) {
        val rowStart = cursorRow * columns
        when (mode) {
            0 -> clearRange(rowStart + cursorCol, rowStart + columns)
            1 -> clearRange(rowStart, rowStart + cursorCol + 1)
            2 -> clearRange(rowStart, rowStart + columns)
        }
    }

    internal fun eraseChars(count: Int) {
        val n = count.coerceAtLeast(1)
        val start = index(cursorCol, cursorRow)
        val end = (start + n).coerceAtMost((cursorRow + 1) * columns)
        clearRange(start, end)
    }

    internal fun deleteChars(count: Int) {
        val n = count.coerceAtLeast(1)
        val rowStart = cursorRow * columns
        val rowEnd = rowStart + columns
        val from = rowStart + cursorCol
        val shiftEnd = (from + n).coerceAtMost(rowEnd)
        var dest = from
        var src = shiftEnd

        val curChars = activeChars()
        val curFg = activeFg()
        val curBg = activeBg()
        val curBold = activeBold()
        val curItalic = activeItalic()
        val curUnderline = activeUnderline()
        val curDim = activeDim()
        val curInverse = activeInverse()
        val curStrikethrough = activeStrikethrough()

        while (src < rowEnd) {
            curChars[dest] = curChars[src]
            curFg[dest] = curFg[src]
            curBg[dest] = curBg[src]
            curBold[dest] = curBold[src]
            curItalic[dest] = curItalic[src]
            curUnderline[dest] = curUnderline[src]
            curDim[dest] = curDim[src]
            curInverse[dest] = curInverse[src]
            curStrikethrough[dest] = curStrikethrough[src]
            dest++
            src++
        }
        clearRange(dest, rowEnd)
    }

    internal fun insertLines(count: Int) {
        val n = count.coerceAtLeast(1).coerceAtMost(scrollBottom - cursorRow + 1)
        for (row in scrollBottom downTo cursorRow + n) {
            copyRow(row - n, row)
        }
        for (row in cursorRow until cursorRow + n) {
            clearRow(row)
        }
    }

    internal fun deleteLines(count: Int) {
        val n = count.coerceAtLeast(1).coerceAtMost(scrollBottom - cursorRow + 1)
        for (row in cursorRow until scrollBottom - n + 1) {
            copyRow(row + n, row)
        }
        for (row in (scrollBottom - n + 1)..scrollBottom) {
            clearRow(row)
        }
    }

    private fun resetSgr() {
        currentFg = COLOR_DEFAULT_FG
        currentBg = COLOR_DEFAULT_BG
        currentBold = false
        currentItalic = false
        currentUnderline = false
        currentDim = false
        currentInverse = false
        currentStrikethrough = false
    }

    private fun index(col: Int, row: Int) = row * columns + col

    private fun clearRange(start: Int, end: Int) {
        val s = start.coerceIn(0, activeChars().size)
        val e = end.coerceIn(0, activeChars().size)

        val curChars = activeChars()
        val curFg = activeFg()
        val curBg = activeBg()
        val curBold = activeBold()
        val curItalic = activeItalic()
        val curUnderline = activeUnderline()
        val curDim = activeDim()
        val curInverse = activeInverse()
        val curStrikethrough = activeStrikethrough()

        for (i in s until e) {
            curChars[i] = ' '
            curFg[i] = COLOR_DEFAULT_FG
            curBg[i] = COLOR_DEFAULT_BG
            curBold[i] = false
            curItalic[i] = false
            curUnderline[i] = false
            curDim[i] = false
            curInverse[i] = false
            curStrikethrough[i] = false
        }
    }

    private fun clearRow(row: Int) {
        clearRange(row * columns, (row + 1) * columns)
    }

    private fun copyRow(fromRow: Int, toRow: Int) {
        val from = fromRow * columns
        val to = toRow * columns

        val curChars = activeChars()
        val curFg = activeFg()
        val curBg = activeBg()
        val curBold = activeBold()
        val curItalic = activeItalic()
        val curUnderline = activeUnderline()
        val curDim = activeDim()
        val curInverse = activeInverse()
        val curStrikethrough = activeStrikethrough()

        for (c in 0 until columns) {
            curChars[to + c] = curChars[from + c]
            curFg[to + c] = curFg[from + c]
            curBg[to + c] = curBg[from + c]
            curBold[to + c] = curBold[from + c]
            curItalic[to + c] = curItalic[from + c]
            curUnderline[to + c] = curUnderline[from + c]
            curDim[to + c] = curDim[from + c]
            curInverse[to + c] = curInverse[from + c]
            curStrikethrough[to + c] = curStrikethrough[from + c]
        }
    }

    fun scrollUp(count: Int = 1) {
        repeat(count) {
            // If we are at the top and in the primary buffer, record line 0 to scrollback history!
            if (scrollTop == 0 && !isAltScreen) {
                val rowCells = ArrayList<TerminalCell>(columns)
                val curChars = chars
                val curFg = fg
                val curBg = bg
                val curBold = bold
                val curItalic = italic
                val curUnderline = underline
                val curDim = dim
                val curInverse = inverse
                val curStrikethrough = strikethrough

                for (col in 0 until columns) {
                    rowCells.add(
                        TerminalCell(
                            char = curChars[col],
                            fg = curFg[col],
                            bg = curBg[col],
                            bold = curBold[col],
                            italic = curItalic[col],
                            underline = curUnderline[col],
                            dim = curDim[col],
                            inverse = curInverse[col],
                            strikethrough = curStrikethrough[col],
                        ),
                    )
                }
                scrollbackHistory.addLast(rowCells)
                if (scrollbackHistory.size > MAX_SCROLLBACK_LINES) {
                    scrollbackHistory.removeFirst()
                }
            }

            for (row in scrollTop until scrollBottom) {
                copyRow(row + 1, row)
            }
            clearRow(scrollBottom)
        }
    }

    fun scrollDown(count: Int = 1) {
        repeat(count) {
            for (row in scrollBottom downTo scrollTop + 1) {
                copyRow(row - 1, row)
            }
            clearRow(scrollTop)
        }
    }
}
