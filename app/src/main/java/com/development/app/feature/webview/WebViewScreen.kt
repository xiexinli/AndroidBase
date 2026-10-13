package com.development.app.feature.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** 进度条换算用的满值。 */
private const val ProgressMax = 100f
private const val DialogTopOffsetDivisor = 6
private val DialogShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

/** 交给 WebView 加载的协议；其余自定义 scheme 由页面拦下。 */
private val WebViewSchemes = setOf("http", "https")

/**
 * 工具栏「调用 JS」按钮使用的示例脚本：读取网页标题并返回，
 * 同时通过 JS 桥回传原生，一次演示「原生 → JS」与「H5 → 原生」两个方向。
 */
private const val PageProbeScript =
    "(function(){var t=document.title||'';" +
        "if(window.AndroidNative){window.AndroidNative.postMessage(JSON.stringify({type:'probe',title:t}));}" +
        "return t;})()"

/**
 * 可复用的二级 WebView 页面，加载 [WebViewDestination.UrlArg] 指定的地址。
 *
 * 封装了两个方向的调用，后续业务接入 H5 时按需扩展：
 * - H5 → 原生：注入 [WebViewJsBridge]（JS 侧 `window.AndroidNative.*`），新增能力改桥类即可；
 * - 原生 → JS：[WebViewViewModel.callJs] 发事件，页面在 WebView 上执行 [WebView.evaluateJavascript]。
 *
 * 底部导航栏对二级页面不显示（见 `feature/main/MainScreen.kt` 的选中判定）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewRoute(
    onBack: () -> Unit,
    displayMode: WebViewDisplayMode = WebViewDisplayMode.FULL_SCREEN,
    viewModel: WebViewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnBack by rememberUpdatedState(onBack)
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val contentTopPadding = if (displayMode == WebViewDisplayMode.DIALOG) {
        screenHeight / DialogTopOffsetDivisor
    } else {
        0.dp
    }
    val contentModifier = Modifier
        .fillMaxSize()
        .padding(top = contentTopPadding)
        .then(if (displayMode == WebViewDisplayMode.DIALOG) Modifier.clip(DialogShape) else Modifier)
    var webView by remember { mutableStateOf<WebView?>(null) }

    val jsBridge = remember(viewModel) {
        WebViewJsBridge(
            onMessage = viewModel::onWebMessage,
            onClose = viewModel::onWebClose,
            appInfo = viewModel::appInfoJson
        )
    }

    LaunchedEffect(webView, viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is WebViewEvent.Notify -> snackbarHostState.showSnackbar(event.message)
                WebViewEvent.ClosePage -> currentOnBack()
                is WebViewEvent.EvaluateJs -> webView?.evaluateJavascript(event.script) { result ->
                    viewModel.onJsResult(result)
                }
            }
        }
    }

    DisposableEffect(webView) {
        // 必须在这里取值：onDispose 里直接读状态拿到的是“当前值”，会在 WebView 刚创建时就被销毁。
        val view = webView
        onDispose {
            view?.let { web ->
                web.removeJavascriptInterface(WebViewJsBridge.InterfaceName)
                web.destroy()
            }
        }
    }

    // 优先在网页内部回退，退无可退再离开本页。
    BackHandler {
        val current = webView
        if (current?.canGoBack() == true) current.goBack() else currentOnBack()
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = contentModifier,
            topBar = {
                TopAppBar(
                    title = { Text(state.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = currentOnBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { webView?.reload() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                        }
                        IconButton(onClick = { viewModel.callJs(PageProbeScript) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "调用 JS")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Column(Modifier.fillMaxSize().padding(innerPadding)) {
                if (state.loading) {
                    LinearProgressIndicator(
                        progress = { state.progress / ProgressMax },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AndroidView(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    factory = { context ->
                        createWebView(context, state.url, jsBridge, viewModel).also { webView = it }
                    }
                )
            }
        }

        // 弹窗样式下，内容上方的透明区域就是“遮罩”：点击即关闭本页（无涟漪，保持透明观感）。
        if (displayMode == WebViewDisplayMode.DIALOG) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(contentTopPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { currentOnBack() }
                    )
            )
        }
    }
}

/** 创建并配置 WebView：开启 JS、注册 JS 桥与加载回调；销毁由调用方负责。 */
@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(
    context: Context,
    url: String,
    jsBridge: WebViewJsBridge,
    viewModel: WebViewViewModel
): WebView = WebView(context).apply {
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.cacheMode = WebSettings.LOAD_DEFAULT
    addJavascriptInterface(jsBridge, WebViewJsBridge.InterfaceName)

    webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, pageUrl: String?, favicon: Bitmap?) {
            viewModel.onPageStarted()
        }

        override fun onPageFinished(view: WebView, pageUrl: String?) {
            viewModel.onPageFinished()
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            if (request.isForMainFrame) viewModel.onLoadError(error.description?.toString().orEmpty())
        }

        /**
         * 只让 WebView 自己加载 http/https；其他自定义 scheme（`baiduboxapp://`、`intent://`
         * 等 App 唤起协议）一律拦下，否则 WebView 会落到 ERR_UNKNOWN_URL_SCHEME 错误页。
         * 需要真正唤起宿主 App 时，在这里改用系统 Intent 打开。
         */
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val scheme = request.url.scheme?.lowercase()
            if (scheme != null && scheme in WebViewSchemes) return false
            viewModel.onUnsupportedScheme(scheme)
            return true
        }
    }

    webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            viewModel.onProgressChanged(newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String?) {
            viewModel.onTitleChanged(title)
        }
    }

    loadUrl(url)
}
