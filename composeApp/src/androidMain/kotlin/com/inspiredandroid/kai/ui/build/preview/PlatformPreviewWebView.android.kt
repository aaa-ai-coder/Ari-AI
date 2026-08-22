package com.inspiredandroid.kai.ui.build.preview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformPreviewWebView(
    url: String,
    refreshTrigger: Int,
    onConsoleMessage: ((String) -> Unit)?,
    modifier: Modifier,
) {
    var webViewInstance: WebView? = null

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            val level = it.messageLevel()?.name ?: "LOG"
                            val msg = "[$level] ${it.message()} (${it.sourceId()}:${it.lineNumber()})"
                            onConsoleMessage?.invoke(msg)
                        }
                        return super.onConsoleMessage(consoleMessage)
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, pageUrl: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, pageUrl, favicon)
                        onConsoleMessage?.invoke("🌐 Loading: $pageUrl")
                    }

                    override fun onPageFinished(view: WebView?, pageUrl: String?) {
                        super.onPageFinished(view, pageUrl)
                        onConsoleMessage?.invoke("✅ Loaded: $pageUrl")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?,
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            onConsoleMessage?.invoke("❌ Error: ${error?.description} (${request.url})")
                        }
                    }
                }
                webViewInstance = this
                loadUrl(url)
            }
        },
        update = { webView ->
            webViewInstance = webView
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
    )

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            webViewInstance?.reload()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.stopLoading()
            webViewInstance?.destroy()
        }
    }
}
