package com.development.demo.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** 列表类示例用的一行数据。 */
private data class DemoRow(val id: Int, val title: String)

/** 列表与网格类：LazyColumn / LazyRow / LazyVerticalGrid，以及 key 的正确用法。 */
@Composable
internal fun ListSection() {
    ExampleBlock(
        title = "LazyColumn：只组合可见区域",
        description = "长列表必须用 Lazy 系列；LazyColumn 内部**不要**再套 verticalScroll，" +
            "否则会命中「同方向嵌套滚动 / 高度无界」的问题。",
    ) {
        val rows = remember { (1..20).map { DemoRow(it, "列表项 $it") } }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(rows, key = { it.id }) { row ->
                Text(text = row.title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    ExampleBlock(
        title = "LazyRow：横向列表",
        description = "与 LazyColumn 完全对称，只是滚动方向不同；适合横向卡片流。",
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(10) { index ->
                Card { Text(text = "卡片 ${index + 1}", modifier = Modifier.padding(16.dp)) }
            }
        }
    }

    ExampleBlock(
        title = "LazyVerticalGrid：网格",
        description = "GridCells.Fixed(n) 固定列数；GridCells.Adaptive(minSize) 按可用宽度自适应列数，" +
            "是平板 / 折叠屏上更省心的选择。",
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            modifier = Modifier.fillMaxWidth().height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(9) { index ->
                Box(
                    modifier = Modifier.aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "${index + 1}", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    ExampleBlock(
        title = "易错点：用 index 当 key → 删除后状态错位",
        description = "两个列表内容完全相同，唯一区别是 key。先给某个条目的「+1」点几下，" +
            "再点「删除第一项」：左边（key = index）计数会留在原位置并整体上移，右边（key = id）计数跟着自己的条目走。",
        isPitfall = true,
    ) {
        var rows by remember { mutableStateOf((1..4).map { DemoRow(it, "条目 $it") }) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ExampleLabel("key = index")
                LazyColumn(Modifier.fillMaxWidth().height(150.dp)) {
                    itemsIndexed(rows, key = { index, _ -> index }) { _, row ->
                        CounterRow(row)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ExampleLabel("key = row.id")
                LazyColumn(Modifier.fillMaxWidth().height(150.dp)) {
                    items(rows, key = { it.id }) { row ->
                        CounterRow(row)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { rows = rows.drop(1) },
                enabled = rows.isNotEmpty(),
            ) { Text("删除第一项") }
            TextButton(onClick = { rows = (1..4).map { DemoRow(it, "条目 $it") } }) { Text("重置列表") }
        }
    }
}

/** 演示列表的一项：内部用 remember 记住自己的点击次数，用来暴露 key 用错时的状态错位。 */
@Composable
private fun CounterRow(row: DemoRow) {
    var count by remember { mutableIntStateOf(0) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.title,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(text = count.toString(), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.width(4.dp))
        TextButton(onClick = { count++ }) { Text("+1") }
    }
}
