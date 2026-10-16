package com.development.demo.ui.composables.sections

import com.development.demo.ui.composables.ExampleBlock
import com.development.demo.ui.composables.ExampleLabel
import com.development.demo.ui.composables.ResultText

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** 动画类：animate*AsState / AnimatedVisibility / Crossfade / Animatable。 */
@Composable
internal fun AnimationSection() {
    ExampleBlock(
        title = "animate*AsState：目标值驱动的补间",
        description = "只声明「目标值」，Compose 自动在旧值和新值之间补间。" +
            "适合开关、进度、颜色这类简单动画。",
    ) {
        var expanded by remember { mutableStateOf(false) }
        val width by animateDpAsState(targetValue = if (expanded) 220.dp else 80.dp, label = "width")
        val color by animateColorAsState(
            targetValue = if (expanded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
            label = "color",
        )
        Button(onClick = { expanded = !expanded }) { Text("切换") }
        Box(
            Modifier.width(width).height(36.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color),
        )
        ResultText("当前宽度 ${width.value.toInt()}dp")
    }

    ExampleBlock(
        title = "AnimatedVisibility：显隐过渡",
        description = "可以分别指定 enter / exit 转场；默认是淡入淡出叠加尺寸变化。",
    ) {
        var visible by remember { mutableStateOf(true) }
        Switch(checked = visible, onCheckedChange = { visible = it })
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Box(
                Modifier.fillMaxWidth().height(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            )
        }
    }

    ExampleBlock(
        title = "Crossfade：内容切换的交叉淡入淡出",
        description = "key 变化时旧内容淡出、新内容淡入，常用于页面或状态切换。",
    ) {
        var page by remember { mutableIntStateOf(0) }
        Button(onClick = { page = (page + 1) % 3 }) { Text("切换内容（当前 $page）") }
        Crossfade(targetState = page, label = "page") { value ->
            Box(
                modifier = Modifier.fillMaxWidth().height(60.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (value % 2 == 0) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "第 $value 屏内容")
            }
        }
    }

    ExampleBlock(
        title = "Animatable：需要手动控制时",
        description = "要暂停、取消或按顺序播放时用 Animatable，配合 rememberCoroutineScope 驱动。" +
            "不要忘记 snapTo 先把值置回起点。",
    ) {
        val scope = rememberCoroutineScope()
        val progress = remember { Animatable(0f) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        progress.snapTo(0f)
                        progress.animateTo(1f, tween(durationMillis = 1200))
                    }
                },
            ) { Text("播放") }
            Button(onClick = { scope.launch { progress.animateTo(0f, tween(durationMillis = 400)) } }) {
                Text("重置")
            }
        }
        Box(
            Modifier.fillMaxWidth().height(24.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                Modifier.fillMaxWidth(progress.value).fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        ResultText("进度 ${(progress.value * 100).toInt()}%")
    }
}
