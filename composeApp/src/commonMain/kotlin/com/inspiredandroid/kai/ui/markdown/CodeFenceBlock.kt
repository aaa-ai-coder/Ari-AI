package com.inspiredandroid.kai.ui.markdown

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.inspiredandroid.kai.ui.rememberCopyToClipboard
import kai.composeapp.generated.resources.Res
import kai.composeapp.generated.resources.bot_message_copy_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CodeFenceBlock(
    language: String?,
    code: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val highlightColors = remember(colorScheme) { codeHighlightColors(colorScheme) }
    val highlighted = remember(code, language, highlightColors) {
        highlightCode(code, language, highlightColors)
    }
    val copyToClipboard = rememberCopyToClipboard()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colorScheme.surfaceVariant,
        contentColor = colorScheme.onSurfaceVariant,
    ) {
        Column {
            var justCopied by remember { mutableStateOf(false) }
            LaunchedEffect(justCopied) {
                if (justCopied) {
                    kotlinx.coroutines.delay(1800)
                    justCopied = false
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val lang = language?.takeIf { it.isNotBlank() } ?: "code"
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (lang.lowercase()) {
                            "python", "py" -> Color(0xFF3572A5).copy(alpha = 0.25f)
                            "kotlin", "kt" -> Color(0xFFA97BFF).copy(alpha = 0.25f)
                            "bash", "sh", "shell", "zsh" -> Color(0xFF00E5FF).copy(alpha = 0.25f)
                            "json", "yaml", "yml" -> Color(0xFF4CAF50).copy(alpha = 0.25f)
                            "javascript", "js", "ts", "typescript" -> Color(0xFFFFD54F).copy(alpha = 0.25f)
                            "rust", "rs" -> Color(0xFFFF5722).copy(alpha = 0.25f)
                            else -> colorScheme.surfaceVariant
                        },
                    ) {
                        Text(
                            text = lang.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = when (lang.lowercase()) {
                                "python", "py" -> Color(0xFF64B5F6)
                                "kotlin", "kt" -> Color(0xFFCE93D8)
                                "bash", "sh", "shell", "zsh" -> Color(0xFF00E5FF)
                                "json", "yaml", "yml" -> Color(0xFF81C784)
                                "javascript", "js", "ts", "typescript" -> Color(0xFFFFE082)
                                "rust", "rs" -> Color(0xFFFF8A65)
                                else -> colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (justCopied) {
                        Text(
                            text = "Copied!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                    IconButton(
                        onClick = {
                            copyToClipboard(code)
                            justCopied = true
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(Res.string.bot_message_copy_content_description),
                            tint = if (justCopied) Color(0xFF00E5FF) else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.2f))
            val scroll = rememberScrollState()
            Box(Modifier.horizontalScroll(scroll).padding(12.dp)) {
                Text(
                    text = highlighted,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
