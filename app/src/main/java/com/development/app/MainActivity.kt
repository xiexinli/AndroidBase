package com.development.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.development.app.feature.main.MainRoute
import com.development.app.theme.AppTheme
import com.development.app.theme.AppThemeState
import com.development.app.theme.applyThemeWindowBackground
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appThemeState: AppThemeState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 先定窗口启动背景，避免深色模式下首帧之前闪一下浅色。
        applyThemeWindowBackground(appThemeState.themeMode)
        setContent {
            AppTheme { Surface { MainRoute() } }
        }
    }
}
