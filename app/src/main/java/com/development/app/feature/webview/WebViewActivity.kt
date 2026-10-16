package com.development.app.feature.webview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.development.app.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/** WebView Activity 的展示模式。 */
enum class WebViewDisplayMode {
    /** WebView 页面覆盖整个 Activity。 */
    FULL_SCREEN,

    /** 页面以带遮罩的上层弹窗样式展示，内容顶部留出约六分之一屏高。 */
    DIALOG
}

/**
 * 可独立启动的 WebView 页面 Activity。
 *
 * 其他页面通过 [createIntent] 传入 URL 与展示模式；URL 仅允许 http/https。
 */
@AndroidEntryPoint
class WebViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(ExtraUrl)?.takeIf(::isSupportedUrl)
        if (url == null) {
            finish()
            return
        }

        val displayMode = intent.getStringExtra(ExtraDisplayMode)
            ?.let { value -> WebViewDisplayMode.entries.firstOrNull { it.name == value } }
            ?: WebViewDisplayMode.FULL_SCREEN

        configureWindow(displayMode)
        setContent {
            AppTheme(transparentWindow = true) {
                WebViewActivityContent(
                    url = url,
                    displayMode = displayMode,
                    onBack = ::finish
                )
            }
        }
    }

    /** 配置透明 Activity 窗口；仅弹窗样式给下层页面加遮罩。 */
    private fun configureWindow(displayMode: WebViewDisplayMode) {
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            if (displayMode == WebViewDisplayMode.DIALOG) {
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                attributes = attributes.apply { dimAmount = DialogDimAmount }
            } else {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
    }

    companion object {
        private const val ExtraUrl = "com.development.app.extra.WEBVIEW_URL"
        private const val ExtraDisplayMode = "com.development.app.extra.WEBVIEW_DISPLAY_MODE"
        private const val DialogDimAmount = 0.32f

        /** 创建启动 Intent。默认全屏；弹窗样式需显式传入 [WebViewDisplayMode.DIALOG]。 */
        fun createIntent(
            context: Context,
            url: String,
            displayMode: WebViewDisplayMode = WebViewDisplayMode.FULL_SCREEN
        ): Intent {
            require(isSupportedUrl(url)) { "WebView 仅支持 http/https URL" }
            val intent = Intent(context, WebViewActivity::class.java)
                .putExtra(ExtraUrl, url)
                .putExtra(ExtraDisplayMode, displayMode.name)
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }

        private fun isSupportedUrl(url: String): Boolean {
            val uri = Uri.parse(url)
            return uri.scheme?.lowercase() in SupportedSchemes && !uri.host.isNullOrBlank()
        }

        private val SupportedSchemes = setOf("http", "https")
    }
}

/** Activity 内部只承载一个 WebView 导航项，以复用现有的 Hilt SavedStateHandle 参数注入。 */
@Composable
private fun WebViewActivityContent(
    url: String,
    displayMode: WebViewDisplayMode,
    onBack: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = WebViewDestination.route(url)) {
        composable(
            route = WebViewDestination.Route,
            arguments = listOf(navArgument(WebViewDestination.UrlArg) { type = NavType.StringType })
        ) {
            WebViewRoute(
                onBack = onBack,
                displayMode = displayMode
            )
        }
    }
}
