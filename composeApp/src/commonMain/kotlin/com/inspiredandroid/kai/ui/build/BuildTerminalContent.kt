package com.inspiredandroid.kai.ui.build

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.build.BuildTerminalSession
import com.inspiredandroid.kai.build.PlatformTerminalKeyboard
import com.inspiredandroid.kai.build.supportsRawTerminalInput
import com.inspiredandroid.kai.build.terminal.COLOR_DEFAULT_BG
import com.inspiredandroid.kai.build.terminal.MIN_COLUMNS
import com.inspiredandroid.kai.build.terminal.MIN_ROWS
import com.inspiredandroid.kai.build.terminal.TerminalCell
import com.inspiredandroid.kai.build.terminal.TerminalKey
import com.inspiredandroid.kai.build.terminal.TerminalKeyEncoder
import com.inspiredandroid.kai.build.terminal.TerminalModifiers
import com.inspiredandroid.kai.build.terminal.TerminalMouseEncoder
import com.inspiredandroid.kai.build.terminal.TerminalSnapshot
import com.inspiredandroid.kai.build.terminal.TerminalTheme
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.settings.monoStyle
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.kai_build_terminal_placeholder
import kai.composeapp.generated.resources.kai_build_terminal_raw_hint
import kai.composeapp.generated.resources.kai_build_terminal_run_content_description
import kai.composeapp.generated.resources.kai_build_terminal_running
import kai.composeapp.generated.resources.kai_build_terminal_show_keyboard_content_description
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/** Classic 16-color ANSI palette (dark terminal). */
internal val AnsiPalette = TerminalTheme.CLASSIC_DARK.palette

private const val MIN_FONT_SIZE_SP = 7f
private const val MAX_FONT_SIZE_SP = 24f
private const val DEFAULT_FONT_SIZE_SP = 11f

/** How long a viewport change has to hold still before the PTY hears about it. */
private const val RESIZE_SETTLE_MS = 80L

/** Characters measured in one run, to average out whole-pixel rounding. */
private const val ADVANCE_SAMPLE = 64

/**
 * One terminal cell in pixels, as the grid actually draws it.
 */
internal data class TerminalCellMetrics(
    val advance: Float,
    val firstLine: Int,
    val lineStep: Int,
) {
    /** Whole rows that fit in [height] pixels. */
    fun rowsIn(height: Int): Int = if (height < firstLine) 0 else 1 + (height - firstLine) / lineStep

    /** Column under [x] pixels from the left edge of the grid. */
    fun columnAt(x: Float): Int = (x / advance).toInt()

    /** Row under [y] pixels from the top of the grid. */
    fun rowAt(y: Float): Int = if (y < firstLine) 0 else 1 + ((y - firstLine) / lineStep).toInt()
}

private fun terminalCellMetrics(
    measurer: TextMeasurer,
    fontSize: TextUnit = DEFAULT_FONT_SIZE_SP.sp,
    lineHeight: TextUnit = (DEFAULT_FONT_SIZE_SP + 2f).sp,
): TerminalCellMetrics {
    val style = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize,
        lineHeight = lineHeight,
    )
    val run = measurer.measure("M".repeat(ADVANCE_SAMPLE), style = style, softWrap = false)
    val oneLine = measurer.measure("M", style = style)
    val twoLines = measurer.measure("M\nM", style = style)
    return TerminalCellMetrics(
        advance = (run.size.width / ADVANCE_SAMPLE.toFloat()).coerceAtLeast(1f),
        firstLine = oneLine.size.height.coerceAtLeast(1),
        lineStep = (twoLines.size.height - oneLine.size.height).coerceAtLeast(1),
    )
}

/**
 * Project workspace: active session's VT cell grid sized to viewport
 * with mobile phone quick controls, font zoom, theme selection, fast snippets, and in-terminal search.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BuildTerminalContent(
    session: BuildTerminalSession,
    onSubmitLine: (String) -> Unit,
    onKey: (TerminalKey, TerminalModifiers) -> Unit,
    onText: (String, TerminalModifiers) -> Unit,
    onMouse: (String) -> Unit,
    onResize: (columns: Int, rows: Int) -> Unit,
    onScrollHistory: (delta: Int) -> Unit = {},
    onScrollToBottom: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val terminal = session.terminal
    val busy = session.busy
    // Each tab keeps its own draft line.
    var input by remember(session.id) { mutableStateOf("") }
    var rawInput by remember { mutableStateOf(supportsRawTerminalInput) }
    var latched by remember { mutableStateOf(TerminalModifiers.None) }
    var showKeyboardRequest by remember { mutableIntStateOf(0) }
    val keyboardRaisedFor = remember { mutableSetOf<String>() }
    var focusInputRequest by remember { mutableIntStateOf(0) }
    val inputFocus = remember { FocusRequester() }
    val clipboardManager = LocalClipboardManager.current

    // Mobile personalization: Font scaling & Themes
    var currentTheme by remember { mutableStateOf(TerminalTheme.DEFAULT) }
    var fontSizeSp by remember { mutableFloatStateOf(DEFAULT_FONT_SIZE_SP) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showSnippetsMenu by remember { mutableStateOf(false) }

    // In-terminal search
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var volumeKeysScroll by remember { mutableStateOf(true) }

    val currentFontSize = fontSizeSp.sp
    val currentLineHeight = (fontSizeSp + 2f).sp

    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val hideInputBar = rawInput && imeVisible
    val bg = currentTheme.background

    LaunchedEffect(session.id, busy, rawInput) {
        if (busy && rawInput && keyboardRaisedFor.add(session.id)) showKeyboardRequest++
    }

    val consumeLatch: (TerminalModifiers) -> TerminalModifiers = { reported ->
        val merged = latched + reported
        latched = TerminalModifiers.None
        merged
    }

    val submit = {
        val line = input
        if (line.isNotEmpty() || busy) {
            onSubmitLine(line + "\r")
            input = ""
        }
    }

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = bg,
            tonalElevation = 2.dp,
        ) {
            Column {
                // Mobile Top Utility Header (Zoom, Theme, Search, Quick Snippets)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(currentTheme.palette[0].copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Toolbar actions: Snippets, Search, Paste, Zoom, Theme menu
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Snippets Menu
                        Box {
                            IconButton(
                                onClick = { showSnippetsMenu = true },
                                modifier = Modifier.size(28.dp).handCursor(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Quick Commands",
                                    tint = Color(0xFFF9E2AF),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = showSnippetsMenu,
                                onDismissRequest = { showSnippetsMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("⚡ Quick Commands", fontWeight = FontWeight.Bold) },
                                    onClick = { },
                                    enabled = false,
                                )
                                DropdownMenuItem(
                                    text = { Text("✨ antigravity (AGY Agent)") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("antigravity\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("📁 ls -lah (List Files)") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("ls -lah\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("🐙 git status") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("git status\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("🐙 git log --oneline -5") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("git log --oneline -5\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("🧹 clear screen") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("clear\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("📦 apt update && apt upgrade") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("apt-get update && apt-get upgrade -y\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("🌐 python3 http server 8080") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("python3 -m http.server 8080\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("📊 htop") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("htop\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("💾 df -h && free -m") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("df -h && free -m\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("🤖 agy --help (Antigravity)") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("antigravity --help\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("🐍 pip list") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("pip list\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("🔍 find . -maxdepth 2") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("find . -maxdepth 2 -not -path '*/.*'\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                                DropdownMenuItem(
                                    text = { Text("🌐 curl -s https://ifconfig.me") },
                                    onClick = {
                                        showSnippetsMenu = false
                                        onSubmitLine("curl -s https://ifconfig.me\r")
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                            }
                        }

                        // Search Button
                        IconButton(
                            onClick = {
                                showSearch = !showSearch
                                if (!showSearch) searchQuery = ""
                            },
                            modifier = Modifier.size(28.dp).handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Terminal",
                                tint = if (showSearch) currentTheme.palette[10] else currentTheme.palette[7],
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        IconButton(
                            onClick = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrEmpty()) {
                                    if (rawInput) {
                                        onText(text, TerminalModifiers.None)
                                    } else {
                                        input += text
                                    }
                                }
                            },
                            modifier = Modifier.size(28.dp).handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = currentTheme.palette[7],
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        IconButton(
                            onClick = {
                                if (fontSizeSp > MIN_FONT_SIZE_SP) fontSizeSp -= 1f
                            },
                            enabled = fontSizeSp > MIN_FONT_SIZE_SP,
                            modifier = Modifier.size(28.dp).handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                tint = currentTheme.palette[7],
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        IconButton(
                            onClick = {
                                if (fontSizeSp < MAX_FONT_SIZE_SP) fontSizeSp += 1f
                            },
                            enabled = fontSizeSp < MAX_FONT_SIZE_SP,
                            modifier = Modifier.size(28.dp).handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                tint = currentTheme.palette[7],
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showThemeMenu = true },
                                modifier = Modifier.size(28.dp).handCursor(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Theme",
                                    tint = currentTheme.palette[10],
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = showThemeMenu,
                                onDismissRequest = { showThemeMenu = false },
                            ) {
                                TerminalTheme.entries.forEach { theme ->
                                    DropdownMenuItem(
                                        text = { Text(theme.displayName) },
                                        onClick = {
                                            currentTheme = theme
                                            showThemeMenu = false
                                        },
                                        modifier = Modifier.handCursor(),
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = { Text(if (volumeKeysScroll) "🔊 Volume Scroll: ON" else "🔈 Volume Scroll: OFF") },
                                    onClick = {
                                        volumeKeysScroll = !volumeKeysScroll
                                        showThemeMenu = false
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                            }
                        }
                    }
                }

                // In-terminal search bar
                if (showSearch) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222222))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = currentTheme.palette[10],
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Find in terminal…", style = monoStyle(12.sp, Color.Gray)) },
                            textStyle = monoStyle(12.sp, Color.White),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = currentTheme.palette[10],
                            ),
                            modifier = Modifier.weight(1f).height(36.dp),
                        )
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                showSearch = false
                            },
                            modifier = Modifier.size(24.dp).handCursor(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                    ) {
                        val textMeasurer = rememberTextMeasurer()
                        val cell = remember(textMeasurer, LocalDensity.current, currentFontSize, currentLineHeight) {
                            terminalCellMetrics(textMeasurer, currentFontSize, currentLineHeight)
                        }
                        val maxW = constraints.maxWidth
                        val maxH = constraints.maxHeight
                        val cols = (maxW / cell.advance).toInt().coerceAtLeast(MIN_COLUMNS)
                        val rows = cell.rowsIn(maxH).coerceAtLeast(MIN_ROWS)

                        val settled = remember(session.id, currentFontSize) { mutableStateOf(false) }
                        LaunchedEffect(cols, rows, session.id, currentFontSize) {
                            if (settled.value) delay(RESIZE_SETTLE_MS)
                            settled.value = true
                            onResize(cols, rows)
                        }

                        val keyboardActive = rawInput && busy
                        val mouseActive = terminal.mouse.enabled && busy
                        TerminalGrid(
                            snapshot = terminal,
                            theme = currentTheme,
                            fontSize = currentFontSize,
                            lineHeight = currentLineHeight,
                            searchQuery = searchQuery,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    when {
                                        mouseActive -> terminalMouseInput(
                                            snapshot = terminal,
                                            cell = cell,
                                            onMouse = onMouse,
                                        )
                                        else -> Modifier
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = { showKeyboardRequest++ },
                                                )
                                            }
                                            .pointerInput(cell) {
                                                awaitEachGesture {
                                                    val down = awaitFirstDown(requireUnconsumed = false)
                                                    var lastY = down.position.y
                                                    var accumulatedY = 0f

                                                    while (true) {
                                                        val event = awaitPointerEvent()
                                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                                        val deltaY = change.position.y - lastY
                                                        lastY = change.position.y
                                                        accumulatedY += deltaY

                                                        val lineStep = cell.lineStep.toFloat().coerceAtLeast(1f)
                                                        val lines = (accumulatedY / lineStep).toInt()
                                                        if (lines != 0) {
                                                            accumulatedY -= (lines * lineStep)
                                                            // Dragging down (deltaY > 0) scrolls up into history
                                                            // Dragging up (deltaY < 0) scrolls down towards live screen
                                                            onScrollHistory(lines)
                                                        }

                                                        if (!change.pressed) break
                                                    }
                                                }
                                            }
                                    },
                                ),
                        )

                        // Floating jump-to-bottom indicator when scrolled into history
                        if (terminal.scrollOffset > 0) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E88E5),
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 10.dp)
                                    .handCursor()
                                    .clickable { onScrollToBottom() },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "↓ Back to Live (${terminal.scrollOffset})",
                                        style = monoStyle(11.5.sp, Color.White),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }

                        if (keyboardActive) {
                            PlatformTerminalKeyboard(
                                showKeyboardRequest = showKeyboardRequest,
                                onKey = { key, reported -> onKey(key, consumeLatch(reported)) },
                                onText = { text, reported -> onText(text, consumeLatch(reported)) },
                                modifier = Modifier.size(1.dp),
                                volumeKeysScroll = volumeKeysScroll,
                            )
                        }
                    }

                    // Compact single-line OSC 8 hyperlinks bar
                    if (terminal.hyperlinks.isNotEmpty()) {
                        TerminalHyperlinkBar(urls = terminal.hyperlinks, palette = currentTheme.palette)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

                TerminalKeyRow(
                    enabled = busy,
                    latched = latched,
                    onLatchChange = { latched = it },
                    onKey = { key -> onKey(key, consumeLatch(TerminalModifiers.None)) },
                    rawInput = rawInput,
                    onToggleInputMode = if (supportsRawTerminalInput) {
                        {
                            rawInput = !rawInput
                            if (!rawInput && imeVisible) focusInputRequest++
                        }
                    } else {
                        null
                    },
                    onText = { text -> onText(text, consumeLatch(TerminalModifiers.None)) },
                )

                if (!hideInputBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1A1A))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (busy) "›" else "$",
                            style = monoStyle(14.sp, currentTheme.palette[10]),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        if (rawInput) {
                            Text(
                                text = stringResource(Res.string.kai_build_terminal_raw_hint),
                                style = monoStyle(13.sp, currentTheme.palette[8]),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { showKeyboardRequest++ },
                                enabled = busy,
                                modifier = Modifier.handCursor(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = stringResource(
                                        Res.string.kai_build_terminal_show_keyboard_content_description,
                                    ),
                                    tint = currentTheme.palette[7],
                                )
                            }
                        } else {
                            val inspecting = LocalInspectionMode.current
                            LaunchedEffect(focusInputRequest) {
                                if (focusInputRequest > 0 && !inspecting) inputFocus.requestFocus()
                            }
                            TextField(
                                value = input,
                                onValueChange = { input = it },
                                modifier = Modifier.weight(1f).focusRequester(inputFocus),
                                enabled = busy,
                                textStyle = monoStyle(14.sp, currentTheme.palette[7]),
                                placeholder = {
                                    Text(
                                        text = stringResource(
                                            if (busy) {
                                                Res.string.kai_build_terminal_running
                                            } else {
                                                Res.string.kai_build_terminal_placeholder
                                            },
                                        ),
                                        style = monoStyle(14.sp, currentTheme.palette[8]),
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = currentTheme.palette[10],
                                    focusedTextColor = currentTheme.palette[7],
                                    unfocusedTextColor = currentTheme.palette[7],
                                    focusedPlaceholderColor = currentTheme.palette[8],
                                    unfocusedPlaceholderColor = currentTheme.palette[8],
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = { submit() }),
                                singleLine = true,
                            )
                        }

                        if (busy && !rawInput) {
                            IconButton(onClick = submit, modifier = Modifier.handCursor()) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(
                                        Res.string.kai_build_terminal_run_content_description,
                                    ),
                                    tint = currentTheme.palette[10],
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalSnippetChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF222222))
            .handCursor()
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = monoStyle(11.sp, Color(0xFFAAAAAA)),
        )
    }
}

/**
 * Compact single-line bar for URLs that TUIs embed as OSC 8 hyperlinks.
 * Takes minimal vertical height and scrolls horizontally if multiple links are present.
 */
@Composable
private fun TerminalHyperlinkBar(
    urls: ImmutableList<String>,
    palette: List<Color> = AnsiPalette,
) {
    var dismissed by remember(urls) { mutableStateOf(false) }
    LaunchedEffect(urls) {
        dismissed = false
        delay(HYPERLINK_DISPLAY_MS)
        dismissed = true
    }
    if (dismissed || urls.isEmpty()) return

    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141414))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            urls.forEach { url ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF202020))
                        .handCursor()
                        .clickable { runCatching { uriHandler.openUri(url) } }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🔗 " + url.removePrefix("https://").removePrefix("http://"),
                        style = monoStyle(11.sp, palette.getOrElse(14) { Color(0xFF61D6D6) }),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        IconButton(
            onClick = { dismissed = true },
            modifier = Modifier.size(24.dp).handCursor(),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = palette.getOrElse(8) { Color(0xFF767676) },
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private const val HYPERLINK_DISPLAY_MS = 2 * 60 * 1000L

/**
 * Style for one cell with TrueColor, 256 colors, text attributes, and font size.
 */
private fun cellSpanStyle(
    cell: TerminalCell,
    isCursor: Boolean,
    theme: TerminalTheme = TerminalTheme.DEFAULT,
    fontSize: TextUnit = DEFAULT_FONT_SIZE_SP.sp,
): SpanStyle {
    val rawFg = theme.resolveColor(cell.fg, isBg = false)
    val rawBg = theme.resolveColor(cell.bg, isBg = true)

    val fg = if (cell.dim) rawFg.copy(alpha = 0.6f) else rawFg
    val bg = rawBg

    val finalFg = if (isCursor) bg else if (cell.inverse) bg else fg
    val finalBg = if (isCursor) fg else if (cell.inverse) fg else if (cell.bg == COLOR_DEFAULT_BG) Color.Unspecified else bg

    val textDeco = when {
        cell.underline && cell.strikethrough -> androidx.compose.ui.text.style.TextDecoration.combine(
            listOf(androidx.compose.ui.text.style.TextDecoration.Underline, androidx.compose.ui.text.style.TextDecoration.LineThrough),
        )
        cell.underline -> androidx.compose.ui.text.style.TextDecoration.Underline
        cell.strikethrough -> androidx.compose.ui.text.style.TextDecoration.LineThrough
        else -> androidx.compose.ui.text.style.TextDecoration.None
    }

    return SpanStyle(
        color = finalFg,
        background = finalBg,
        fontWeight = if (cell.bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (cell.italic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
        textDecoration = textDeco,
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize,
        letterSpacing = 0.sp,
    )
}

/**
 * Flattens the cell grid into the string the terminal draws, with optional search highlighting.
 */
internal fun buildTerminalText(
    snapshot: TerminalSnapshot,
    theme: TerminalTheme = TerminalTheme.DEFAULT,
    fontSize: TextUnit = DEFAULT_FONT_SIZE_SP.sp,
    searchQuery: String = "",
): AnnotatedString {
    val plainBuilder = StringBuilder(snapshot.columns.coerceAtLeast(1))
    val spanStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()

    var run = StringBuilder(snapshot.columns.coerceAtLeast(1))
    var lastCell: TerminalCell? = null
    var lastIsCursor = false
    var currentPos = 0

    fun flushRun() {
        val cellToFlush = lastCell ?: return
        if (run.isEmpty()) return
        val len = run.length
        val style = cellSpanStyle(cellToFlush, lastIsCursor, theme, fontSize)
        spanStyles.add(AnnotatedString.Range(style, currentPos, currentPos + len))
        plainBuilder.append(run.toString())
        currentPos += len
        run = StringBuilder(snapshot.columns.coerceAtLeast(1))
    }

    for (row in 0 until snapshot.rows) {
        for (col in 0 until snapshot.columns) {
            val cell = snapshot.cellAt(col, row)
            val isCursor = snapshot.cursorVisible &&
                col == snapshot.cursorCol &&
                row == snapshot.cursorRow
            if (lastCell != null && (cell != lastCell || isCursor != lastIsCursor)) {
                flushRun()
            }
            lastCell = cell
            lastIsCursor = isCursor
            run.append(if (cell.char == Char.MIN_VALUE) ' ' else cell.char)
        }
        flushRun()
        if (row < snapshot.rows - 1) {
            plainBuilder.append('\n')
            currentPos++
        }
    }

    val fullText = plainBuilder.toString()

    // Apply search highlight ranges if search is active
    if (searchQuery.isNotBlank()) {
        val highlightStyle = SpanStyle(
            background = Color(0xFFFFD54F),
            color = Color(0xFF000000),
            fontWeight = FontWeight.Bold,
        )
        var searchIndex = 0
        while (searchIndex < fullText.length) {
            val match = fullText.indexOf(searchQuery, searchIndex, ignoreCase = true)
            if (match == -1) break
            spanStyles.add(
                AnnotatedString.Range(
                    item = highlightStyle,
                    start = match,
                    end = match + searchQuery.length,
                ),
            )
            searchIndex = match + searchQuery.length
        }
    }

    return AnnotatedString(text = fullText, spanStyles = spanStyles)
}

@Composable
private fun terminalMouseInput(
    snapshot: TerminalSnapshot,
    cell: TerminalCellMetrics,
    onMouse: (String) -> Unit,
): Modifier {
    val current = rememberUpdatedState(snapshot)
    val send = rememberUpdatedState(onMouse)
    return Modifier.pointerInput(cell) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val state = current.value.mouse
            val col = cell.columnAt(down.position.x)
                .coerceIn(0, (current.value.columns - 1).coerceAtLeast(0))
            val row = cell.rowAt(down.position.y)
                .coerceIn(0, (current.value.rows - 1).coerceAtLeast(0))

            var travelled = 0f
            var pendingScroll = 0f
            var scrolled = false
            var position = down.position

            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val delta = change.position - position
                position = change.position
                travelled += delta.getDistance()
                pendingScroll += delta.y
                while (pendingScroll >= cell.lineStep) {
                    pendingScroll -= cell.lineStep
                    scrolled = true
                    val wheelCode = TerminalMouseEncoder.wheel(up = true, col = col, row = row, state = state)
                    if (wheelCode != null) {
                        send.value(wheelCode)
                    } else {
                        send.value(TerminalKeyEncoder.encode(TerminalKey.Up, applicationCursorKeys = current.value.applicationCursorKeys))
                    }
                }
                while (pendingScroll <= -cell.lineStep) {
                    pendingScroll += cell.lineStep
                    scrolled = true
                    val wheelCode = TerminalMouseEncoder.wheel(up = false, col = col, row = row, state = state)
                    if (wheelCode != null) {
                        send.value(wheelCode)
                    } else {
                        send.value(TerminalKeyEncoder.encode(TerminalKey.Down, applicationCursorKeys = current.value.applicationCursorKeys))
                    }
                }
                if (!change.pressed) break
            }

            if (!scrolled && travelled <= viewConfiguration.touchSlop) {
                TerminalMouseEncoder.click(col = col, row = row, state = state)
                    ?.let { send.value(it) }
            }
        }
    }
}

@Composable
private fun TerminalGrid(
    snapshot: TerminalSnapshot,
    theme: TerminalTheme = TerminalTheme.DEFAULT,
    fontSize: TextUnit = DEFAULT_FONT_SIZE_SP.sp,
    lineHeight: TextUnit = (DEFAULT_FONT_SIZE_SP + 2f).sp,
    searchQuery: String = "",
    modifier: Modifier = Modifier,
) {
    val revision = snapshot.revision
    val annotated = remember(revision, snapshot.columns, snapshot.rows, theme, fontSize, searchQuery) {
        buildTerminalText(snapshot, theme, fontSize, searchQuery)
    }
    BasicText(
        text = annotated,
        modifier = modifier,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = theme.foreground,
        ),
        softWrap = false,
        maxLines = snapshot.rows.coerceAtLeast(1),
    )
}
