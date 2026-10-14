package com.development.demo.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 副作用类：LaunchedEffect / DisposableEffect / produceState / rememberUpdatedState / rememberCoroutineScope。 */
@Composable
internal fun EffectSection() {
    ExampleBlock(
        title = "LaunchedEffect：key 就是「重启条件」",
        description = "key 不变时协程只启动一次；key 变化会先取消旧协程再重新启动。" +
            "要「只在进入页面时执行一次」就传 Unit。",
    ) {
        var key by remember { mutableIntStateOf(0) }
        var runs by remember { mutableIntStateOf(0) }
        LaunchedEffect(key) { runs++ }
        Button(onClick = { key++ }) { Text("改变 key（当前 $key）") }
        ResultText("LaunchedEffect 已执行 $runs 次")
    }

    ExampleBlock(
        title = "DisposableEffect：进入时注册、离开时释放",
        description = "onDispose 保证在「离开组合」或「key 变化」时执行一次。" +
            "监听器、广播接收器、回调注册都放在这里，避免泄漏。",
    ) {
        var attached by remember { mutableStateOf(true) }
        val log = remember { mutableStateListOf<String>() }
        Switch(checked = attached, onCheckedChange = { attached = it })
        if (attached) {
            DisposableEffect(Unit) {
                log.add("已注册监听")
                onDispose { log.add("已释放监听") }
            }
        }
        Text(
            text = log.joinToString(" → ").ifEmpty { "（暂无记录）" },
            style = MaterialTheme.typography.bodySmall,
        )
        ResultText("关掉开关再打开，能看到「注册 / 释放」成对出现")
    }

    ExampleBlock(
        title = "produceState：把非 Compose 数据源变成 State",
        description = "等价于 remember + LaunchedEffect + mutableStateOf 的组合；" +
            "生产者协程会随组合一起取消，不需要手动清理。",
    ) {
        val tick by produceState(initialValue = 0) {
            while (true) {
                delay(1000)
                value++
            }
        }
        ResultText("每秒自增：$tick")
    }

    ExampleBlock(
        title = "rememberUpdatedState：长期副作用要读最新值",
        description = "先点「开始 3 秒任务」，再在倒计时内点「修改内容」：" +
            "任务里的普通参数捕获的是启动那一刻的旧值，rememberUpdatedState 才能读到最新值。",
    ) {
        var content by remember { mutableStateOf("内容 A") }
        var runId by remember { mutableIntStateOf(0) }
        var capturedText by remember { mutableStateOf("（未开始）") }
        var latestText by remember { mutableStateOf("（未开始）") }
        val latestContent by rememberUpdatedState(content)

        LaunchedEffect(runId) {
            if (runId == 0) return@LaunchedEffect
            capturedText = "⏳ 3 秒后输出…"
            latestText = "⏳ 3 秒后输出…"
            delay(3000)
            capturedText = "旧值（闭包捕获）：$content"
            latestText = "最新值（rememberUpdatedState）：$latestContent"
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { runId++ }) { Text("开始 3 秒任务") }
            Button(onClick = { content = if (content == "内容 A") "内容 B" else "内容 A" }) {
                Text("修改内容")
            }
        }
        ResultText("当前 content = $content ｜ $capturedText ｜ $latestText")
    }

    ExampleBlock(
        title = "rememberCoroutineScope：在点击回调里启动协程",
        description = "onClick 不是挂起函数，需要自己拿作用域。该作用域跟随当前组合，" +
            "离开页面时其中的协程会被取消。",
    ) {
        val scope = rememberCoroutineScope()
        var status by remember { mutableStateOf("点一下按钮") }
        Button(
            onClick = {
                scope.launch {
                    status = "协程开始…"
                    delay(800)
                    status = "协程结束（作用域随组合回收）"
                }
            },
        ) { Text("启动协程") }
        ResultText(status)
    }
}
