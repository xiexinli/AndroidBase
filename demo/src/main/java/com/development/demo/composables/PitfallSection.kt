package com.development.demo.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 易错点与性能类：把前面几页零散提到的坑集中成可直接复现的对比实例。
 *
 * 这里的每个例子都刻意同时给出「错误 / 正确」两半，方便肉眼对照。
 */
@Composable
internal fun PitfallSection() {
    ExampleBlock(
        title = "把副作用直接写在组合里 → 每次重组都会执行",
        description = "组合函数会被反复调用。「写在函数体里的日志、统计、网络请求」都会跟着重复执行。" +
            "需要「只执行一次」就放 LaunchedEffect(Unit)；需要「每次重组后同步外部状态」才用 SideEffect。",
        isPitfall = true,
    ) {
        var trigger by remember { mutableIntStateOf(0) }
        val compositionRuns = remember { intArrayOf(0) }
        var effectRuns by remember { mutableIntStateOf(0) }

        // 错误示范：直接写在组合函数体里，每次重组都会执行一次。
        compositionRuns[0]++
        // 正确示范：只在进入组合时执行一次。
        LaunchedEffect(Unit) { effectRuns++ }

        val label = "触发一次重组（$trigger）"
        Button(onClick = { trigger++ }) { Text(label) }
        Text(
            text = "写在组合里：已执行 ${compositionRuns[0]} 次",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "放在 LaunchedEffect 里：已执行 $effectRuns 次",
            style = MaterialTheme.typography.bodySmall,
        )
        ResultText("每点一次按钮，上面那行都会变大；下面那行始终是 1。")
    }

    ExampleBlock(
        title = "两个组件各自 remember → 数据不同步",
        description = "同一个业务值如果被两个组件各自 remember，就会出现「一处改了、另一处没变」。" +
            "正确做法是把状态提升到共同父级，形成单一数据源。",
        isPitfall = true,
    ) {
        ExampleLabel("错误：各自持有状态，互不影响")
        LocalCounter()
        LocalCounter()
        ExampleLabel("正确：状态提升到父级，两个组件共享")
        var shared by remember { mutableIntStateOf(0) }
        StatelessCounter(count = shared, onIncrement = { shared++ })
        StatelessCounter(count = shared, onIncrement = { shared++ })
    }

    ExampleBlock(
        title = "在组合中写「本作用域读取的状态」→ 无限重组",
        description = "如果写成 count++（count 正是本函数读取的状态），就会陷入" +
            "「重组 → 写状态 → 触发重组 → 再写」的死循环，界面直接卡死。" +
            "这类问题没法在这里演示——它会把 App 冻住，所以只做文字说明。",
        isPitfall = true,
    ) {
        Text(
            text = "反例（不要照抄）：\nvar count by remember { mutableIntStateOf(0) }\ncount++  // ← 每次重组都改自己读的状态",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "正解：把写入放进事件回调（onClick）或副作用（LaunchedEffect / SideEffect），不要在组合期间改状态。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    ExampleBlock(
        title = "列表没有 key → 增删后状态错位",
        description = "Lazy 系列默认用「位置」标识每一项。没有 key 时，插入/删除会让每一项的 remember 状态" +
            "留在原位置而不是跟着数据走。具体复现见「列表与网格 Lists」页的 index vs id 对照。",
        isPitfall = true,
    ) {
        Text(
            text = "正解：items(list, key = { it.id }) { }  —— 用业务上稳定的唯一标识作 key，" +
                "不要用 index，也不要用会变化的字段。",
            style = MaterialTheme.typography.bodySmall,
        )
    }

    ExampleBlock(
        title = "收集 StateFlow 时忘记跟随生命周期",
        description = "collectAsState() 在页面退到后台后仍可能继续收集；" +
            "本项目统一使用 collectAsStateWithLifecycle()，在 STOPPED 时自动取消收集。",
        isPitfall = true,
    ) {
        Text(
            text = "正解：val state by viewModel.state.collectAsStateWithLifecycle()",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "（本页是纯 UI 示例，没有 ViewModel；参考 demo 的折叠屏示例页与 :base 样例页。）",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    ExampleBlock(
        title = "长耗时计算写在组合里 → 每次重组都重算",
        description = "排序、过滤、格式化这类操作放在组合函数里，会跟着每次重组重复执行。" +
            "用 remember 缓存、或在 ViewModel 里算好再用 derivedStateOf 派生。",
        isPitfall = true,
    ) {
        Text(
            text = "正解：val sorted = remember(items) { items.sortedBy { it.rank } }",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "只有当结果确实随多个状态变化、且希望「变没变」决定下游重组时，才用 remember { derivedStateOf { ... } }。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 自持状态的计数器：与 [StatelessCounter] 对照，用来演示「状态没有提升」导致的各说各话。 */
@Composable
private fun LocalCounter() {
    var count by remember { mutableIntStateOf(0) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "count = $count", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Button(onClick = { count++ }) { Text("+1") }
    }
}
