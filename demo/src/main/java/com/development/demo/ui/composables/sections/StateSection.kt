package com.development.demo.ui.composables.sections

import com.development.demo.ui.composables.ExampleBlock
import com.development.demo.ui.composables.ExampleLabel
import com.development.demo.ui.composables.ResultText

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 状态类：remember / rememberSaveable / 状态提升 / derivedStateOf。 */
@Composable
internal fun StateSection() {
    ExampleBlock(
        title = "remember 与 rememberSaveable",
        description = "remember 只在「重组」之间保留值，Activity 重建（旋转、切深色、进程恢复）会丢失；" +
            "rememberSaveable 会把它写进 savedInstanceState 并在重建后恢复。",
    ) {
        var remembered by remember { mutableIntStateOf(0) }
        var saved by rememberSaveable { mutableIntStateOf(0) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { remembered++ }) { Text("remember +1") }
            Button(onClick = { saved++ }) { Text("rememberSaveable +1") }
        }
        ResultText("remember = $remembered ｜ rememberSaveable = $saved")
        Text(
            text = "旋转屏幕后：左边归零，右边保留。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    ExampleBlock(
        title = "状态提升：单一数据源",
        description = "子组件只接收 count 与回调、自己不持有状态。两个子组件共享同一份状态，" +
            "任意一个 +1 另一个同步变化，也更容易做单测。",
    ) {
        var shared by remember { mutableIntStateOf(0) }
        StatelessCounter(count = shared, onIncrement = { shared++ })
        StatelessCounter(count = shared, onIncrement = { shared++ })
        ResultText("两个子组件共用同一个 count")
    }

    ExampleBlock(
        title = "derivedStateOf：从状态派生状态",
        description = "过滤结果只依赖列表与关键词。用 derivedStateOf 包一层后，只有依赖真正变化时才重算，" +
            "而且结果引用稳定，能避免下游被无谓重组。",
    ) {
        val options = remember {
            listOf("Compose", "Coroutines", "Kotlin", "Flow", "Room", "Hilt", "Coil", "WorkManager")
        }
        var query by remember { mutableStateOf("") }
        val filtered by remember { derivedStateOf { options.filter { it.contains(query, ignoreCase = true) } } }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("过滤关键词") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = filtered.joinToString(" / ").ifEmpty { "（无匹配）" },
            style = MaterialTheme.typography.bodySmall,
        )
        ResultText("匹配 ${filtered.size} 项")
    }

    ExampleBlock(
        title = "易错点：remember 的 key 用错 → 状态莫名清零",
        description = "remember(key) 会在 key 变化时丢弃旧值并重新初始化。把「经常变化的输入」当成 key，" +
            "状态就会被反复重置；只有确实需要「随某个条件重新开始」时才传 key。",
        isPitfall = true,
    ) {
        var text by remember { mutableStateOf("") }
        var keyedByText by remember(text) { mutableIntStateOf(0) }
        var stable by remember { mutableIntStateOf(0) }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("随便输入点什么，观察左边的计数") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { keyedByText++ }) { Text("key = text：$keyedByText") }
            Button(onClick = { stable++ }) { Text("无 key：$stable") }
        }
        ResultText("输入框每变一个字符，左边的计数就归零。")
    }
}

/**
 * 无状态计数器：只负责展示与上报事件，状态由父级持有（状态提升）。
 */
@Composable
internal fun StatelessCounter(count: Int, onIncrement: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "count = $count", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Button(onClick = onIncrement) { Text("+1") }
    }
}
