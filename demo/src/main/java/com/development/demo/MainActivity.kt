package com.development.demo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.development.base.core.ui.AndroidBaseTheme
import com.development.demo.adaptive.AdaptiveDemoRoute
import com.development.demo.composables.ComposablesActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * demo 首页：在 `:base` 之上演示折叠屏自适应布局，并作为示例页面的统一入口。
 *
 * 右下角的按钮跳转到 [ComposablesActivity]（Compose 主要 Composable 用法示例）。
 * 需要看 `:base` 自带的网络样例页时，把内容换回 `HomeRoute()` 即可。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidBaseTheme {
                Surface {
                    Box(Modifier.fillMaxSize()) {
                        AdaptiveDemoRoute()
                        // 入口：跳转到「Compose 主要 Composable 用法示例」。
                        ExtendedFloatingActionButton(
                            onClick = { openComposablesShowcase() },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                            text = { Text("Compose") },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        )
                    }
                }
            }
        }
    }

    private fun openComposablesShowcase() {
        startActivity(Intent(this, ComposablesActivity::class.java))
    }
}
