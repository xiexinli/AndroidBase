package com.development.demo.ui.hilt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.development.demo.ui.composables.ExampleBlock
import com.development.demo.ui.composables.ExampleLabel
import com.development.demo.ui.composables.ResultText

/**
 * Hilt 用法示例页。
 *
 * @param activityEvidence 由 [HiltActivity] 通过字段注入 / Lazy / Provider / EntryPoint 实测出的结果。
 * @param onBack 返回上一页（关闭本页）。
 * @param viewModel 页面 ViewModel，用 `hiltViewModel()` 演示 Compose 中的 Hilt 注入。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HiltShowcaseScreen(
    activityEvidence: List<HiltEvidence>,
    onBack: () -> Unit,
    viewModel: HiltDemoViewModel = hiltViewModel(),
) {
    val viewModelEvidence by viewModel.evidence.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hilt 用法示例") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { IntroBlock() }
            item { EvidenceBlock(activityEvidence = activityEvidence, viewModelEvidence = viewModelEvidence) }
            items(HiltScenarios, key = { it.title }) { scenario -> ScenarioBlock(scenario) }
        }
    }
}

/** 页面开头的总体说明。 */
@Composable
private fun IntroBlock() {
    ExampleBlock(
        title = "这个页面演示什么",
        description = "逐条列出 Hilt 的使用场景，并给出注意事项与补充信息。",
    ) {
        BulletText("每个场景都配了示例代码；标注「本页实测」的结论来自本页真实注入的对象，不是文案。")
        BulletText("本工程用 KSP 做注解处理，Hilt 版本见 gradle/libs.versions.toml。")
        BulletText(":base 提供绑定与容器入口，:app / :demo 各自 @HiltAndroidApp；:base 不反向依赖宿主。")
    }
}

/** 顶部「本页实测结果」：把两条来源的证据合并展示。 */
@Composable
private fun EvidenceBlock(
    activityEvidence: List<HiltEvidence>,
    viewModelEvidence: List<HiltEvidence>,
) {
    val rows = activityEvidence + viewModelEvidence
    ExampleBlock(
        title = "本页实测结果",
        description = "HiltActivity 字段注入 / Lazy / Provider / EntryPoint 与 HiltDemoViewModel 构造注入的真实输出。",
    ) {
        if (rows.isEmpty()) {
            ResultText("等待注入结果…")
        } else {
            rows.forEach { row ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ExampleLabel(row.label)
                    ResultText(row.value)
                }
            }
        }
    }
}

/** 单个场景：摘要 + 示例代码 + 注意事项 + 补充信息。 */
@Composable
private fun ScenarioBlock(scenario: HiltScenario) {
    ExampleBlock(title = scenario.title, description = scenario.summary) {
        ExampleLabel("示例代码")
        CodeText(scenario.code)
        ExampleLabel("注意事项")
        scenario.notes.forEach { BulletText(it) }
        ExampleLabel("补充信息")
        scenario.extras.forEach { BulletText(it) }
    }
}

/** 等宽字体的代码块，与说明文字区分开。 */
@Composable
private fun CodeText(code: String) {
    Text(
        text = code,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
    )
}

/** 要点行，用圆点前缀，换行时与文字左对齐。 */
@Composable
private fun BulletText(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
