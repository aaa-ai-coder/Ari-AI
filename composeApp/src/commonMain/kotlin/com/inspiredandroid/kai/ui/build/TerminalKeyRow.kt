package com.inspiredandroid.kai.ui.build

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.build.terminal.TerminalKey
import com.inspiredandroid.kai.build.terminal.TerminalModifiers
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.settings.monoStyle
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.kai_build_terminal_input_mode_content_description
import kai.composeapp.generated.resources.kai_build_terminal_key_down_content_description
import kai.composeapp.generated.resources.kai_build_terminal_key_enter_content_description
import kai.composeapp.generated.resources.kai_build_terminal_key_left_content_description
import kai.composeapp.generated.resources.kai_build_terminal_key_right_content_description
import kai.composeapp.generated.resources.kai_build_terminal_key_up_content_description
import org.jetbrains.compose.resources.stringResource

/** Navigation caps, in the order a keyboard lays them out. */
private val ArrowCaps = listOf(
    Triple(TerminalKey.Left, TerminalArrowLeft, Res.string.kai_build_terminal_key_left_content_description),
    Triple(TerminalKey.Up, TerminalArrowUp, Res.string.kai_build_terminal_key_up_content_description),
    Triple(TerminalKey.Down, TerminalArrowDown, Res.string.kai_build_terminal_key_down_content_description),
    Triple(TerminalKey.Right, TerminalArrowRight, Res.string.kai_build_terminal_key_right_content_description),
)

/** Navigation and common symbols on mobile */
private val NavCaps = listOf(
    "home" to TerminalKey.Home,
    "end" to TerminalKey.End,
    "pgup" to TerminalKey.PageUp,
    "pgdn" to TerminalKey.PageDown,
)

private val SymbolCaps = listOf(
    "|" to TerminalKey.Pipe,
    "~" to TerminalKey.Tilde,
    "/" to TerminalKey.Slash,
    "-" to TerminalKey.Dash,
    "_" to TerminalKey.Underscore,
    "$" to TerminalKey.Dollar,
    ">" to TerminalKey.GreaterThan,
    "&" to TerminalKey.Ampersand,
    "`" to TerminalKey.Backtick,
    "\\" to TerminalKey.Backslash,
)

private val KeyCapHeight = 34.dp
private val KeyCapMinWidth = 38.dp

/** Icon caps hold one glyph, so they can be squarer than the lettered ones. */
private val IconKeyCapMinWidth = 36.dp

/** Enter is the row's action key and gets the width to say so. */
private val EnterKeyCapMinWidth = 48.dp
private val KeyCapFontSize = 12.5.sp
private val KeyCapShape = RoundedCornerShape(8.dp)

/**
 * Mobile-optimized terminal key row providing instant access to modifiers,
 * signals (Ctrl+C, Ctrl+D, Ctrl+L), arrows, navigation, and critical symbols.
 */
@Composable
internal fun TerminalKeyRow(
    enabled: Boolean,
    latched: TerminalModifiers,
    onLatchChange: (TerminalModifiers) -> Unit,
    onKey: (TerminalKey) -> Unit,
    modifier: Modifier = Modifier,
    rawInput: Boolean = false,
    onToggleInputMode: (() -> Unit)? = null,
    onToggleKeyboard: (() -> Unit)? = null,
    onText: ((String) -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF151515))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Modifiers
            KeyCap(
                label = "ctrl",
                enabled = enabled,
                active = latched.ctrl,
                onClick = { onLatchChange(latched.copy(ctrl = !latched.ctrl)) },
            )
            KeyCap(label = "esc", enabled = enabled, onClick = { onKey(TerminalKey.Escape) })
            KeyCap(label = "tab", enabled = enabled, onClick = { onKey(TerminalKey.Tab) })

            KeyGroupSeparator()

            // Arrows
            ArrowCaps.forEach { (key, icon, description) ->
                IconKeyCap(
                    icon = icon,
                    contentDescription = stringResource(description),
                    enabled = enabled,
                    onClick = { onKey(key) },
                )
            }

            KeyGroupSeparator()

            // Navigation
            NavCaps.forEach { (label, key) ->
                KeyCap(label = label, enabled = enabled, onClick = { onKey(key) })
            }

            KeyGroupSeparator()

            // Essential symbols for phone typing
            SymbolCaps.forEach { (sym, key) ->
                KeyCap(
                    label = sym,
                    enabled = enabled,
                    minWidth = 32.dp,
                    onClick = { onKey(key) },
                )
            }

            KeyGroupSeparator()

            KeyCap(
                label = "alt",
                enabled = enabled,
                active = latched.alt,
                onClick = { onLatchChange(latched.copy(alt = !latched.alt)) },
            )
            KeyCap(
                label = "shift",
                enabled = enabled,
                active = latched.shift,
                onClick = { onLatchChange(latched.copy(shift = !latched.shift)) },
            )
        }

        onToggleKeyboard?.let { toggle ->
            IconKeyCap(
                icon = TerminalKeyboard,
                contentDescription = "Toggle Software Keyboard",
                enabled = true,
                onClick = toggle,
            )
        }

        onToggleInputMode?.let { toggle ->
            IconKeyCap(
                icon = if (rawInput) Icons.Default.Edit else Icons.Default.Terminal,
                contentDescription = stringResource(
                    Res.string.kai_build_terminal_input_mode_content_description,
                ),
                enabled = true,
                onClick = toggle,
            )
        }

        IconKeyCap(
            icon = TerminalEnter,
            contentDescription = stringResource(Res.string.kai_build_terminal_key_enter_content_description),
            enabled = enabled,
            accent = true,
            iconSize = TerminalEnterIconSize,
            minWidth = EnterKeyCapMinWidth,
            onClick = { onKey(TerminalKey.Enter) },
        )
    }
}

@Composable
private fun KeyGroupSeparator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(18.dp)
            .width(1.dp)
            .background(Color.White.copy(alpha = 0.12f)),
    )
}

@Composable
private fun KeyCap(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    accentColor: Color? = null,
    minWidth: Dp = KeyCapMinWidth,
) {
    KeyCapSurface(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        active = active,
        minWidth = minWidth,
    ) { tint ->
        Text(
            text = label,
            style = monoStyle(KeyCapFontSize, accentColor ?: tint),
        )
    }
}

@Composable
private fun IconKeyCap(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    iconSize: Dp = TerminalKeyIconSize,
    minWidth: Dp = IconKeyCapMinWidth,
) {
    KeyCapSurface(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        accent = accent,
        minWidth = minWidth,
    ) { tint ->
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** The cap itself: colors for the three states, and the tint its content draws with. */
@Composable
private fun KeyCapSurface(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    accent: Boolean = false,
    minWidth: Dp = KeyCapMinWidth,
    content: @Composable (tint: Color) -> Unit,
) {
    val tint = when {
        !enabled -> Color(0xFF767676)
        accent -> Color(0xFFFFFFFF)
        active -> Color(0xFF00E5FF)
        else -> Color(0xFFCCCCCC)
    }
    val container = when {
        accent && enabled -> Color(0xFF7C4DFF).copy(alpha = 0.35f)
        active -> Color(0xFF00E5FF).copy(alpha = 0.25f)
        else -> Color(0xFF262626)
    }

    Box(
        modifier = modifier
            .height(KeyCapHeight)
            .defaultMinSize(minWidth = minWidth)
            .clip(KeyCapShape)
            .background(container)
            .handCursor()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        content(tint)
    }
}
