package com.development.demo.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.development.base.core.ui.AndroidBaseTheme
import com.development.demo.ui.adaptive.AdaptiveActivity
import com.development.demo.ui.composables.ComposablesActivity
import dagger.hilt.android.AndroidEntryPoint

/** demo 示例导航首页。 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidBaseTheme {
                Surface {
                    DemoCatalogScreen(
                        onOpenAdaptive = { startActivity(Intent(this, AdaptiveActivity::class.java)) },
                        onOpenComposables = { startActivity(Intent(this, ComposablesActivity::class.java)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoCatalogScreen(onOpenAdaptive: () -> Unit, onOpenComposables: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Demo 示例") }) }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DemoEntry(
                    title = "折叠屏自适应示例",
                    description = "窗口尺寸、折叠姿态与双栏布局",
                    onClick = onOpenAdaptive,
                )
            }
            item {
                DemoEntry(
                    title = "Compose 用法示例",
                    description = "浏览常用 Composable 分类与运行示例",
                    onClick = onOpenComposables,
                )
            }
        }
    }
}

@Composable
private fun DemoEntry(title: String, description: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
