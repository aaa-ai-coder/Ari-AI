package com.inspiredandroid.kai.ui.build.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Multiplatform embedded WebView for live phone emulation and app previews.
 */
@Composable
expect fun PlatformPreviewWebView(
    url: String,
    refreshTrigger: Int,
    onConsoleMessage: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
)
