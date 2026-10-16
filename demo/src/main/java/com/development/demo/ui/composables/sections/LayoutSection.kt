package com.development.demo.ui.composables.sections

import com.development.demo.ui.composables.ExampleBlock
import com.development.demo.ui.composables.ExampleLabel
import com.development.demo.ui.composables.ResultText

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** 布局类：Column / Row / Box / Spacer / FlowRow 与尺寸修饰符。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LayoutSection() {
    ExampleBlock(
        title = "Column / Row：主轴分布与权重",
        description = "Row 里用 weight(f) 按比例分配剩余宽度；Column 里的 weight 则分配高度。" +
            "spacedBy 统一子项间距，比逐个加 padding 更好维护。",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Box(
                Modifier.weight(2f).fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.secondary),
            )
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.tertiary),
            )
        }
        ResultText("weight 1 : 2 : 1")
    }

    ExampleBlock(
        title = "Box：叠放与对齐",
        description = "Box 的子项默认叠放在左上角；Modifier.align 只对 Box 的**直接子项**生效（BoxScope）。",
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Text(
                text = "TopStart",
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "Center",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "BottomEnd",
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    ExampleBlock(
        title = "Spacer 与尺寸修饰符",
        description = "size 同时设置宽高，width / height 只设一个，" +
            "fillMaxWidth / fillMaxHeight 撑满父级已给出的约束。",
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small))
            Spacer(Modifier.width(12.dp))
            Text(text = "Spacer(Modifier.width(12.dp))", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxWidth().height(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.secondary),
        )
        ResultText("Box.fillMaxWidth() 撑满整行")
    }

    ExampleBlock(
        title = "FlowRow：自动换行",
        description = "一行放不下时自动折行，适合标签墙；属于 ExperimentalLayoutApi，需要显式 OptIn。",
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(8) { index ->
                AssistChip(onClick = {}, label = { Text("标签 ${index + 1}") })
            }
        }
    }

    ExampleBlock(
        title = "易错点：size 会被父级约束压缩，requiredSize 不会",
        description = "父容器只有 48dp 时：size(80.dp) 被压到 48dp；requiredSize(80.dp) 直接忽略父级约束而溢出。" +
            "需要「无论如何都是这个尺寸」时才用 requiredSize，否则会挤破布局。",
        isPitfall = true,
    ) {
        ExampleLabel("Modifier.size(80.dp) —— 被压缩到父级高度")
        Box(
            Modifier.fillMaxWidth().height(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(Modifier.size(80.dp).background(MaterialTheme.colorScheme.primary))
        }
        ExampleLabel("Modifier.requiredSize(80.dp) —— 溢出父级")
        Box(
            Modifier.fillMaxWidth().height(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(Modifier.requiredSize(80.dp).background(MaterialTheme.colorScheme.error))
        }
    }
}
