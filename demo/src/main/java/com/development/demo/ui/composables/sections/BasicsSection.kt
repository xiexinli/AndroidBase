package com.development.demo.ui.composables.sections

import com.development.demo.ui.composables.ExampleBlock
import com.development.demo.ui.composables.ExampleLabel
import com.development.demo.ui.composables.ResultText

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** 基础组件类：Text / Button 家族 / IconButton / Card / Divider。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BasicsSection() {
    ExampleBlock(
        title = "Text：样式令牌与溢出处理",
        description = "文字样式一律走 MaterialTheme.typography，不要在业务层硬编码 fontSize；" +
            "长文本用 maxLines + overflow 控制截断。",
    ) {
        Text(text = "headlineSmall 标题", style = MaterialTheme.typography.headlineSmall)
        Text(text = "bodyMedium 正文：Compose 默认按父级宽度自动换行。", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "这是一段很长很长的文本，用来演示 maxLines = 1 与 TextOverflow.Ellipsis 的效果。",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    ExampleBlock(
        title = "Button 家族：五种语义层级",
        description = "Button（主操作）/ FilledTonalButton（次主）/ OutlinedButton（次级）/" +
            "ElevatedButton（浮起）/ TextButton（最低强调）。按钮文案放在最后一个 lambda 里。",
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = {}) { Text("Button") }
            FilledTonalButton(onClick = {}) { Text("FilledTonal") }
            OutlinedButton(onClick = {}) { Text("Outlined") }
            ElevatedButton(onClick = {}) { Text("Elevated") }
            TextButton(onClick = {}) { Text("TextButton") }
        }
    }

    ExampleBlock(
        title = "IconButton 与 BadgedBox",
        description = "IconButton 是纯图标按钮，**必须**给 Icon 一个 contentDescription（无障碍朗读）；" +
            "BadgedBox 用来挂角标。",
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "收藏")
            }
            BadgedBox(badge = { Badge { Text("9+") } }) {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Filled.Notifications, contentDescription = "通知")
                }
            }
        }
    }

    ExampleBlock(
        title = "Card 与 Divider",
        description = "OutlinedCard / ElevatedCard 是同一语义的不同视觉；Divider 会自动处理 RTL 镜像。",
    ) {
        OutlinedCard(Modifier.fillMaxWidth()) {
            Text(text = "OutlinedCard", modifier = Modifier.padding(12.dp))
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Text(text = "ElevatedCard", modifier = Modifier.padding(12.dp))
        }
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "左", style = MaterialTheme.typography.bodySmall)
            VerticalDivider(Modifier.height(24.dp).padding(horizontal = 8.dp))
            Text(text = "右", style = MaterialTheme.typography.bodySmall)
        }
    }

    ExampleBlock(
        title = "易错点：Row 里的长文本会把兄弟元素挤出去",
        description = "短文本时看不出问题，一旦文案变长，没有 weight 的 Text 会占满一行并把右侧图标顶到屏幕外。" +
            "正确做法：给 Text 加 weight(1f) + maxLines。",
        isPitfall = true,
    ) {
        ExampleLabel("没有约束：右侧图标被挤出可见区域")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "这是一段没有设置 weight 与 maxLines 的长说明文本，它会把右边的图标挤出可见区域。",
                style = MaterialTheme.typography.bodySmall,
            )
            Icon(imageVector = Icons.Filled.Info, contentDescription = null)
        }
        Spacer(Modifier.height(8.dp))
        ExampleLabel("正确：weight(1f) + maxLines = 2，图标始终可见")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "这是一段很长的说明文本，weight(1f) 让它在剩余空间里换行，maxLines 再兜底截断。",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Icon(imageVector = Icons.Filled.Info, contentDescription = null)
        }
    }
}
