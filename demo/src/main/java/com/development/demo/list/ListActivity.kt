package com.development.demo.list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.development.base.core.ui.AndroidBaseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 分页列表示例的入口 Activity。
 *
 * 入口在「Compose 用法示例」页（[com.development.demo.composables.ComposablesActivity]）的目录里，
 * 不单独注册 LAUNCHER。
 *
 * 数据是刻意伪造的演示数据，由 [ListViewModel] 生成，不接入 `:base` 的网络与缓存。
 */
@AndroidEntryPoint
class ListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidBaseTheme {
                Surface {
                    ListRoute(onBack = { finish() })
                }
            }
        }
    }
}
