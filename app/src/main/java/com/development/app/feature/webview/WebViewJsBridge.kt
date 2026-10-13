package com.development.app.feature.webview

import android.webkit.JavascriptInterface

/**
 * 暴露给 H5 的原生接口（H5 → 原生）。
 *
 * 页面用 [WebView.addJavascriptInterface] 以 [InterfaceName] 注入，JS 侧通过
 * `window.AndroidNative.<方法名>(...)` 调用。后续新增原生能力时，只需在这里追加
 * 一个 `@JavascriptInterface` 方法并转发给回调，不改动页面其余部分。
 *
 * 注意：被 JS 调用的方法运行在 WebView 的 JS 线程，不要在其中直接操作 UI。
 *
 * @param onMessage H5 主动上报的消息，业务侧约定为 JSON 并按 `type` 分发。
 * @param onClose H5 请求关闭当前页面。
 * @param appInfo 同步返回给 H5 的宿主 App 信息（JSON 字符串）。
 */
class WebViewJsBridge(
    private val onMessage: (String) -> Unit,
    private val onClose: () -> Unit,
    private val appInfo: () -> String
) {

    /** H5 上报消息：`window.AndroidNative.postMessage('{"type":"pay"}')`。 */
    @JavascriptInterface
    fun postMessage(message: String) = onMessage(message)

    /** H5 请求关闭当前页面：`window.AndroidNative.closePage()`。 */
    @JavascriptInterface
    fun closePage() = onClose()

    /** H5 同步读取宿主 App 信息：`var info = window.AndroidNative.getAppInfo()`。 */
    @JavascriptInterface
    fun getAppInfo(): String = appInfo()

    companion object {
        /** 注入到 JS 全局对象的名称。 */
        const val InterfaceName = "AndroidNative"
    }
}
