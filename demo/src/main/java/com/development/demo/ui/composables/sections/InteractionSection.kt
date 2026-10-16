package com.development.demo.ui.composables.sections

import com.development.demo.ui.composables.ExampleBlock
import com.development.demo.ui.composables.ExampleLabel
import com.development.demo.ui.composables.ResultText

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** 交互与手势类：clickable / combinedClickable / pointerInput，以及 Modifier 顺序。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun InteractionSection() {
    var clickCount by remember { mutableIntStateOf(0) }

    ExampleBlock(
        title = "Modifier.clickable：点击区域由链上的位置决定",
        description = "修饰符从左到右「包裹」：把 clickable 放在 padding **之前**，" +
            "整个内边距都算点击区域；放在之后则只有文字本身可点。" +
            "onClickLabel 与 role 用于无障碍语义。",
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClickLabel = "增加计数", role = Role.Button) { clickCount++ }
                .padding(16.dp),
        ) {
            Text(text = "点击整块区域（含内边距）", style = MaterialTheme.typography.bodyMedium)
        }
        ResultText("累计点击 $clickCount 次")
        Text(
            text = "若写成 .padding(16.dp).clickable { }，只有文字可以点。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    ExampleBlock(
        title = "combinedClickable：单击 + 长按",
        description = "长按常用于「更多操作」菜单，属于 ExperimentalFoundationApi。",
    ) {
        var hint by remember { mutableStateOf("点我试试") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .combinedClickable(
                    onClick = { hint = "单击" },
                    onLongClick = { hint = "长按" },
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "单击或长按我", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(text = hint, style = MaterialTheme.typography.bodyMedium)
        }
    }

    ExampleBlock(
        title = "pointerInput：自由手势（拖拽）",
        description = "detectDragGestures 提供底层拖拽识别。拖动过程中要立即生效，必须用 Modifier.offset { } " +
            "这个 lambda 版本（避免每帧都触发重组）。",
    ) {
        var offset by remember { mutableStateOf(Offset.Zero) }
        Box(
            modifier = Modifier.fillMaxWidth().height(140.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offset += dragAmount
                        }
                    },
            )
        }
        TextButton(onClick = { offset = Offset.Zero }) { Text("复位") }
    }

    ExampleBlock(
        title = "易错点：Modifier 顺序不同，绘制结果完全不同",
        description = "修饰符是按顺序**一层层包裹**的：先 padding 再 background，背景只包住内容区；" +
            "先 background 再 padding，背景会把内边距一起涂上。这是新手最常见、也最难自查的一类问题。",
        isPitfall = true,
    ) {
        ExampleLabel("padding(16.dp).background(primary) —— 背景仅 48×48")
        Box(
            Modifier.padding(16.dp)
                .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                .size(48.dp),
        )
        Spacer(Modifier.height(8.dp))
        ExampleLabel("background(primary).padding(16.dp) —— 背景 80×80（含内边距）")
        Box(
            Modifier.background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                .padding(16.dp)
                .size(48.dp),
        )
    }
}
