package com.inspiredandroid.kai.ui.build.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.kai.ui.handCursor
import com.inspiredandroid.kai.ui.settings.monoStyle

// Google AI Studio Dark Palette
private val StudioBackground = Color(0xFF131314)
private val StudioSurface = Color(0xFF1E1F20)
private val StudioSurfaceVariant = Color(0xFF282A2C)
private val StudioBorder = Color(0xFF333538)
private val StudioBlue = Color(0xFF8AB4F8)
private val StudioTextPrimary = Color(0xFFE3E3E3)
private val StudioTextSecondary = Color(0xFFC4C7C5)
private val StudioSparkleBrush = Brush.horizontalGradient(
    listOf(Color(0xFF4285F4), Color(0xFF9B72CB), Color(0xFFD96570), Color(0xFFF4B400)),
)

private val CommonDevPorts = listOf(
    "3000" to "React / Next.js / Node",
    "5173" to "Vite / Vue / Svelte",
    "8000" to "FastAPI / Django",
    "8080" to "Python HTTP Server",
    "5000" to "Flask",
    "4321" to "Astro",
    "80" to "Standard HTTP",
)

/**
 * Google AI Studio in-app interactive Phone Emulator & App Prototype screen.
 */
@Composable
fun BuildPreviewContent(
    project: String,
    onSendCommand: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var selectedModel by remember { mutableStateOf(PhoneDeviceModel.PIXEL_9_PRO) }
    var isLandscape by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf("http://localhost:3000") }
    var inputUrl by remember { mutableStateOf("http://localhost:3000") }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showModelMenu by remember { mutableStateOf(false) }
    var showPortsMenu by remember { mutableStateOf(false) }
    var showConsole by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }

    val consoleLogs = remember { mutableStateListOf<String>() }
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    val submitAiPrompt = {
        val prompt = aiPrompt.trim()
        if (prompt.isNotEmpty()) {
            onSendCommand?.invoke("$prompt\r")
            consoleLogs.add("✨ Prompt sent to AI: $prompt")
            aiPrompt = ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .imePadding(),
    ) {
        // --- Google AI Studio Top Control Bar ---
        Surface(
            color = StudioSurface,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 1.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Google AI Studio Device Frame Pill Selector
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                            modifier = Modifier
                                .handCursor()
                                .clickable { showModelMenu = true },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Icon(
                                    imageVector = when (selectedModel) {
                                        PhoneDeviceModel.IPHONE_16_PRO -> Icons.Default.PhoneIphone
                                        PhoneDeviceModel.TABLET_IPAD -> Icons.Default.Tablet
                                        else -> Icons.Default.PhoneAndroid
                                    },
                                    contentDescription = null,
                                    tint = StudioBlue,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = selectedModel.displayName,
                                    style = monoStyle(11.5.sp, StudioTextPrimary),
                                    fontWeight = FontWeight.Medium,
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = StudioTextSecondary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false },
                            modifier = Modifier.background(StudioSurface),
                        ) {
                            PhoneDeviceModel.entries.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model.displayName, color = StudioTextPrimary) },
                                    onClick = {
                                        selectedModel = model
                                        showModelMenu = false
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                            }
                        }
                    }

                    // Rotate Orientation
                    IconButton(
                        onClick = { isLandscape = !isLandscape },
                        modifier = Modifier.size(34.dp).handCursor(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Rotate",
                            tint = if (isLandscape) StudioBlue else StudioTextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // Quick Port Dropdown
                    Box {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                            modifier = Modifier
                                .handCursor()
                                .clickable { showPortsMenu = true },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Text(
                                    text = "Port",
                                    style = monoStyle(11.5.sp, Color(0xFF69F0AE)),
                                    fontWeight = FontWeight.Bold,
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = StudioTextSecondary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showPortsMenu,
                            onDismissRequest = { showPortsMenu = false },
                            modifier = Modifier.background(StudioSurface),
                        ) {
                            DropdownMenuItem(
                                text = { Text("⚡ Dev Server Ports", fontWeight = FontWeight.Bold, color = StudioBlue) },
                                onClick = {},
                                enabled = false,
                            )
                            CommonDevPorts.forEach { (port, desc) ->
                                DropdownMenuItem(
                                    text = { Text(":$port ($desc)", color = StudioTextPrimary) },
                                    onClick = {
                                        val newUrl = "http://localhost:$port"
                                        inputUrl = newUrl
                                        currentUrl = newUrl
                                        refreshTrigger++
                                        showPortsMenu = false
                                    },
                                    modifier = Modifier.handCursor(),
                                )
                            }
                            HorizontalDivider(color = StudioBorder, modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text("📄 Local index.html", color = StudioTextPrimary) },
                                onClick = {
                                    val newUrl = "file:///root/projects/$project/index.html"
                                    inputUrl = newUrl
                                    currentUrl = newUrl
                                    refreshTrigger++
                                    showPortsMenu = false
                                },
                                modifier = Modifier.handCursor(),
                            )
                        }
                    }

                    // Studio Address Bar
                    TextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        textStyle = monoStyle(11.5.sp, StudioTextPrimary),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = StudioSurfaceVariant,
                            unfocusedContainerColor = StudioSurfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = StudioBlue,
                        ),
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                var formatted = inputUrl.trim()
                                if (!formatted.startsWith("http://") && !formatted.startsWith("https://") && !formatted.startsWith("file://")) {
                                    formatted = "http://$formatted"
                                }
                                inputUrl = formatted
                                currentUrl = formatted
                                refreshTrigger++
                            },
                        ),
                    )

                    // Reload
                    IconButton(
                        onClick = { refreshTrigger++ },
                        modifier = Modifier.size(34.dp).handCursor(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    // Open in Browser
                    IconButton(
                        onClick = { runCatching { uriHandler.openUri(currentUrl) } },
                        modifier = Modifier.size(34.dp).handCursor(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in Chrome",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Quick Port Shortcuts Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Active Ports:",
                        style = monoStyle(10.5.sp, StudioTextSecondary),
                        modifier = Modifier.padding(end = 2.dp),
                    )
                    listOf("3000", "5173", "8000", "8080", "5000").forEach { port ->
                        val isCurrent = currentUrl == "http://localhost:$port"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) StudioBlue.copy(alpha = 0.25f) else StudioSurfaceVariant,
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, StudioBlue) else null,
                            modifier = Modifier
                                .handCursor()
                                .clickable {
                                    val newUrl = "http://localhost:$port"
                                    inputUrl = newUrl
                                    currentUrl = newUrl
                                    refreshTrigger++
                                },
                        ) {
                            Text(
                                text = ":$port",
                                style = monoStyle(10.5.sp, if (isCurrent) StudioBlue else StudioTextSecondary),
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = StudioBorder)

        // --- Center Google AI Studio Phone Emulator Frame ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            PhoneEmulatorFrame(
                model = selectedModel,
                isLandscape = isLandscape,
                modifier = Modifier.fillMaxSize(),
            ) {
                PlatformPreviewWebView(
                    url = currentUrl,
                    refreshTrigger = refreshTrigger,
                    onConsoleMessage = { log ->
                        consoleLogs.add(log)
                        if (consoleLogs.size > 200) consoleLogs.removeAt(0)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        HorizontalDivider(color = StudioBorder)

        // --- Google AI Studio "Ask AI to Modify" Prompt Bar ---
        Surface(
            color = StudioSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StudioSparkleBrush),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    TextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        placeholder = {
                            Text(
                                text = "Ask AI to modify this app (e.g. 'add dark mode', 'fix layout')...",
                                style = monoStyle(11.5.sp, StudioTextSecondary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        textStyle = monoStyle(12.sp, StudioTextPrimary),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = StudioSurfaceVariant,
                            unfocusedContainerColor = StudioSurfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = StudioBlue,
                        ),
                        shape = RoundedCornerShape(22.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitAiPrompt() }),
                    )

                    IconButton(
                        onClick = submitAiPrompt,
                        enabled = aiPrompt.isNotBlank(),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (aiPrompt.isNotBlank()) StudioBlue else StudioSurfaceVariant)
                            .handCursor(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send to AI",
                            tint = if (aiPrompt.isNotBlank()) StudioBackground else StudioTextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = StudioBorder)

        // --- Bottom DevTools Console Drawer ---
        Surface(
            color = StudioSurface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .handCursor()
                        .clickable { showConsole = !showConsole }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = StudioBlue,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Console Logs (${consoleLogs.size})",
                            style = monoStyle(11.5.sp, StudioTextPrimary),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (consoleLogs.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val text = consoleLogs.joinToString("\n")
                                    clipboardManager.setText(AnnotatedString(text))
                                },
                                modifier = Modifier.size(24.dp).handCursor(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Logs",
                                    tint = StudioTextSecondary,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                            IconButton(
                                onClick = { consoleLogs.clear() },
                                modifier = Modifier.size(24.dp).handCursor(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ClearAll,
                                    contentDescription = "Clear Logs",
                                    tint = StudioTextSecondary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Icon(
                            imageVector = if (showConsole) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                AnimatedVisibility(visible = showConsole) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(StudioBackground)
                            .padding(8.dp),
                    ) {
                        if (consoleLogs.isEmpty()) {
                            Text(
                                text = "No console output. JavaScript logs and errors will appear here.",
                                style = monoStyle(11.sp, StudioTextSecondary),
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(consoleLogs) { log ->
                                    val color = when {
                                        log.contains("[ERROR]", ignoreCase = true) || log.contains("❌") -> Color(0xFFFF5252)
                                        log.contains("[WARN]", ignoreCase = true) -> Color(0xFFFFD740)
                                        log.contains("✅") -> Color(0xFF69F0AE)
                                        log.contains("✨") -> Color(0xFFB388FF)
                                        else -> StudioTextPrimary
                                    }
                                    Text(
                                        text = log,
                                        style = monoStyle(10.5.sp, color),
                                        modifier = Modifier.padding(vertical = 1.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
