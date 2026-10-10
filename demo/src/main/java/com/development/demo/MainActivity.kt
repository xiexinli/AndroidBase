package com.development.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.development.base.core.ui.AndroidBaseTheme
import com.development.base.feature.home.HomeRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * demo 首页：直接复用 `:base` 提供的主题与样例页面，验证「只依赖 :base 就能跑」。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidBaseTheme { Surface { HomeRoute() } }
        }
    }
}
