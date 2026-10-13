package com.development.app.feature.webview

import android.net.Uri

/**
 * WebView 页面（二级页面）的导航契约。
 *
 * 目标地址通过查询参数 [UrlArg] 传入，调用方用 [route] 生成路由：
 * URL 会先做一次编码，避免其中的 `?`、`&`、`#` 破坏路由匹配；
 * Navigation 在解析 `StringType` 参数时会自行解码，页面拿到的仍是原始 URL。
 */
object WebViewDestination {

    /** 要加载的网页地址。 */
    const val UrlArg = "url"

    /** 路由模板：`webview?url={url}`。 */
    const val Route = "webview?$UrlArg={$UrlArg}"

    /** 由目标地址生成可导航的路由。 */
    fun route(url: String): String = "webview?$UrlArg=${Uri.encode(url)}"
}
