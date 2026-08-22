package com.inspiredandroid.kai.ui.build.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun PlatformPreviewWebView(
    url: String,
    refreshTrigger: Int,
    onConsoleMessage: ((String) -> Unit)?,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(Color(0xFF1E1E1E)).padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("iOS Preview: $url", color = Color.White)
    }
}
