package com.development.demo.ui.adaptive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.development.base.core.ui.AndroidBaseTheme
import dagger.hilt.android.AndroidEntryPoint

/** 折叠屏自适应示例的独立入口。 */
@AndroidEntryPoint
class AdaptiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidBaseTheme {
                Surface { AdaptiveDemoRoute() }
            }
        }
    }
}
