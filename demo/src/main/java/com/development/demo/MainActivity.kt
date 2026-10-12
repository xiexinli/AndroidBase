package com.development.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.development.base.core.ui.AndroidBaseTheme
import com.development.demo.adaptive.AdaptiveDemoRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * demo 首页：在 `:base` 之上演示折叠屏自适应布局，验证「只依赖 :base 就能跑」。
 *
 * 需要看 `:base` 自带的网络样例页时，把内容换回 `HomeRoute()` 即可。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidBaseTheme { Surface { AdaptiveDemoRoute() } }
        }
    }
}
