package com.peihua.touchmonitor.ui.screen.function.video

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import androidx.webkit.WebViewClientCompat
import com.kevinnzou.web.AccompanistWebChromeClient
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.function.video.m3u8.M3u8DownloaderByCode


@Composable
fun M3u8Downloader(modifier: Modifier = Modifier, type: Int = 0) {
    Toolbar(modifier = modifier, title = "m3u8 下载", navigateUp = { popBackStack() }) {
        when (type) {
            0 -> M3u8DownloaderByWebView(modifier = Modifier.fillMaxSize())
            else -> M3u8DownloaderByCode(modifier = Modifier.fillMaxSize())
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun M3u8DownloaderByWebView(modifier: Modifier) {
    val context = LocalContext.current
    val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", AssetsPathHandler(context))
        .addPathHandler("/res/", ResourcesPathHandler(context))
        .build()
    val navigator = rememberWebViewNavigator()
    val state = rememberWebViewState(url = "file:///android_asset/m3u8/downloader/index.html")
    WebView(
        modifier = modifier, state = state,
        navigator = navigator,
        chromeClient = WarpChromeClient(ChromeClient()),
        client = WarpAccompanistWebViewClient(WebViewClient(assetLoader)),
        onCreated = {
            it.settings.javaScriptEnabled = true
        })
}

private class WebViewClient(private val assetLoader: WebViewAssetLoader) : WebViewClientCompat() {
    @RequiresApi(21)
    override fun shouldInterceptRequest(
        view: android.webkit.WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }

    // To support API < 21.
    override fun shouldInterceptRequest(
        view: android.webkit.WebView,
        url: String,
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(Uri.parse(url))
    }
}

private class ChromeClient : WebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {}

    override fun onReceivedTitle(view: WebView?, title: String?) {}

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {}

    override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {

    }


}

open class WarpChromeClient(private val client: WebChromeClient) : AccompanistWebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        client.onProgressChanged(view, newProgress)
    }

    override fun onReceivedTitle(view: WebView, title: String?) {
        client.onReceivedTitle(view, title)
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        client.onReceivedIcon(view, icon)
    }

    override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
        client.onReceivedTouchIconUrl(view, url, precomposed)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        client.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        client.onHideCustomView()
    }

    override fun onCreateWindow(
        view: WebView?, isDialog: Boolean,
        isUserGesture: Boolean, resultMsg: Message?,
    ): Boolean {
        return client.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }

    override fun onRequestFocus(view: WebView?) {
        client.onRequestFocus(view)
    }

    override fun onCloseWindow(window: WebView?) {
        client.onCloseWindow(window)
    }

    override fun onJsAlert(
        view: WebView?, url: String?, message: String?,
        result: JsResult?,
    ): Boolean {
        return client.onJsAlert(view, url, message, result)
    }

    override fun onJsConfirm(
        view: WebView?, url: String?, message: String?,
        result: JsResult?,
    ): Boolean {
        return client.onJsConfirm(view, url, message, result)
    }

    override fun onJsPrompt(
        view: WebView?, url: String?, message: String?,
        defaultValue: String?, result: JsPromptResult?,
    ): Boolean {
        return client.onJsPrompt(view, url, message, defaultValue, result)
    }

    override fun onJsBeforeUnload(
        view: WebView?, url: String?, message: String?,
        result: JsResult?,
    ): Boolean {
        return client.onJsBeforeUnload(view, url, message, result)
    }
}

open class WarpAccompanistWebViewClient(private val client: WebViewClient) : AccompanistWebViewClient() {
    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        client.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        client.onPageFinished(view, url)
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        client.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        client.onReceivedError(view, request, error)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        client.onLoadResource(view, url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        return client.shouldOverrideUrlLoading(view, request)
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        client.onPageCommitVisible(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        url: String?,
    ): WebResourceResponse? {
        return client.shouldInterceptRequest(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        return client.shouldInterceptRequest(view, request)
    }
}