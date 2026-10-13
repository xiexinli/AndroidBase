package com.development.app.feature.webview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.development.app.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

/** 加载进度满值。 */
private const val MaxProgress = 100

/** `evaluateJavascript` 对 JS `null` / `undefined` 的返回值。 */
private const val JsNull = "null"

/** 页面标题取不到时的占位文案。 */
private const val Untitled = "网页"

/**
 * WebView 页面状态。
 *
 * @param url 要加载的地址，来自导航参数 [WebViewDestination.UrlArg]。
 * @param title 网页标题，取不到时为 [Untitled]。
 * @param progress 加载进度 0..100。
 * @param loading 是否正在加载。
 */
data class WebViewUiState(
    val url: String,
    val title: String = Untitled,
    val progress: Int = 0,
    val loading: Boolean = true
)

/** WebView 页面的一次性事件（由页面收集，不放进 [WebViewUiState]）。 */
sealed interface WebViewEvent {

    /** 需要提示给用户的信息：H5 上报、JS 执行结果、加载失败等。 */
    data class Notify(val message: String) : WebViewEvent

    /** 原生调用 JS：请页面内的 WebView 执行 [script]。 */
    data class EvaluateJs(val script: String) : WebViewEvent

    /** H5 请求关闭当前页面。 */
    data object ClosePage : WebViewEvent
}

/**
 * WebView 页面的状态持有者。
 *
 * 页面自身不感知 WebView 实现，只把加载回调交给这里，并把 [events] 中的
 * [WebViewEvent.EvaluateJs] 交给 WebView 执行，从而实现原生 → JS 的调用。
 */
@HiltViewModel
class WebViewViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _state = MutableStateFlow(
        WebViewUiState(
            url = checkNotNull(savedStateHandle.get<String>(WebViewDestination.UrlArg)) {
                "缺少导航参数 ${WebViewDestination.UrlArg}"
            }
        )
    )
    val state: StateFlow<WebViewUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<WebViewEvent>(
        extraBufferCapacity = EventBufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<WebViewEvent> = _events.asSharedFlow()

    // ---- 原生 → JS ----

    /** 原生调用 JS：交给页面执行，结果通过 [onJsResult] 回传。 */
    fun callJs(script: String) {
        _events.tryEmit(WebViewEvent.EvaluateJs(script))
    }

    /** JS 执行结果回传，去掉 `evaluateJavascript` 的 JSON 引号后提示。 */
    fun onJsResult(result: String?) {
        val text = result?.trim('"')?.takeIf { it.isNotBlank() && it != JsNull }
        notify("JS 返回：${text ?: "（空）"}")
    }

    // ---- H5 → 原生 ----

    /** H5 通过 [WebViewJsBridge] 上报消息。 */
    fun onWebMessage(message: String) {
        Timber.tag(LogTag).d("H5 -> Native: %s", message)
        notify("H5 消息：$message")
    }

    /** H5 请求关闭当前页面。 */
    fun onWebClose() {
        _events.tryEmit(WebViewEvent.ClosePage)
    }

    /** 提供给 H5 的宿主 App 信息，由 [WebViewJsBridge.getAppInfo] 同步返回给 JS。 */
    fun appInfoJson(): String =
        """{"appId":"${BuildConfig.APPLICATION_ID}","versionName":"${BuildConfig.VERSION_NAME}","platform":"android"}"""

    // ---- 加载状态回调 ----

    /** 主文档开始加载。 */
    fun onPageStarted() {
        _state.value = _state.value.copy(loading = true, progress = 0)
    }

    /** 主文档加载结束。 */
    fun onPageFinished() {
        _state.value = _state.value.copy(loading = false, progress = MaxProgress)
    }

    /** 加载进度变化。 */
    fun onProgressChanged(progress: Int) {
        val percent = progress.coerceIn(0, MaxProgress)
        _state.value = _state.value.copy(progress = percent, loading = percent < MaxProgress)
    }

    /** 网页标题变化，空标题忽略。 */
    fun onTitleChanged(title: String?) {
        val text = title?.takeIf { it.isNotBlank() } ?: return
        _state.value = _state.value.copy(title = text)
    }

    /** 主文档加载失败。 */
    fun onLoadError(description: String) {
        _state.value = _state.value.copy(loading = false)
        notify("网页加载失败：${description.ifBlank { "未知错误" }}")
    }

    /** 拦下了 WebView 不支持的自定义 scheme（如 App 唤起协议）。 */
    fun onUnsupportedScheme(scheme: String?) {
        Timber.tag(LogTag).d("已拦截自定义 scheme: %s", scheme)
    }

    private fun notify(message: String) {
        _events.tryEmit(WebViewEvent.Notify(message))
    }

    private companion object {
        /** 事件缓冲区大小；页面暂停时丢弃最旧的提示，避免阻塞 JS 线程。 */
        const val EventBufferSize = 8
        const val LogTag = "WebView"
    }
}
