package com.development.demo.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.launch

/** 弹窗与浮层类：AlertDialog / ModalBottomSheet / Snackbar / DropdownMenu / Popup。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OverlaySection() {
    ExampleBlock(
        title = "AlertDialog：模态确认框",
        description = "对话框写在组合里、由布尔状态驱动；onDismissRequest 必须把状态置回 false，" +
            "否则点遮罩或返回键关不掉。",
    ) {
        var show by remember { mutableStateOf(false) }
        var choice by remember { mutableStateOf("（尚未选择）") }
        Button(onClick = { show = true }) { Text("打开 AlertDialog") }
        ResultText("最近选择：$choice")
        if (show) {
            AlertDialog(
                onDismissRequest = { show = false },
                title = { Text("删除确认") },
                text = { Text("删除后不可恢复，确定继续吗？") },
                confirmButton = {
                    TextButton(onClick = { choice = "已确认"; show = false }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { choice = "已取消"; show = false }) { Text("取消") }
                },
            )
        }
    }

    ExampleBlock(
        title = "ModalBottomSheet：底部半模态",
        description = "rememberModalBottomSheetState 控制展开状态；" +
            "「先 hide() 再关闭」才能播完收起动画，直接置 false 会显得很跳。",
    ) {
        var show by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        Button(onClick = { show = true }) { Text("打开底部弹窗") }
        if (show) {
            ModalBottomSheet(onDismissRequest = { show = false }, sheetState = sheetState) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = "底部弹窗内容", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "可以下拉手柄或点击遮罩关闭。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) show = false
                            }
                        },
                    ) { Text("收起") }
                }
            }
        }
    }

    ExampleBlock(
        title = "Snackbar：轻量提示",
        description = "SnackbarHostState.showSnackbar 是挂起函数，必须在协程作用域里调用；" +
            "页面里通常把它挂在 Scaffold 的 snackbarHost 上。",
    ) {
        val hostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        Box(
            modifier = Modifier.fillMaxWidth().height(160.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Button(
                onClick = { scope.launch { hostState.showSnackbar("这是一条 Snackbar 提示") } },
                modifier = Modifier.align(Alignment.Center),
            ) { Text("显示 Snackbar") }
            SnackbarHost(hostState, Modifier.align(Alignment.BottomCenter))
        }
    }

    ExampleBlock(
        title = "DropdownMenu：锚定菜单",
        description = "菜单锚定在父容器（这里是 Box）的坐标系上，展开状态由外部控制；" +
            "记得在选择后把 expanded 置回 false。",
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selected by remember { mutableStateOf("未选择") }
        Box {
            Button(onClick = { expanded = true }) { Text("选择：$selected") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("选项 A", "选项 B", "选项 C").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selected = option
                            expanded = false
                        },
                    )
                }
            }
        }
    }

    ExampleBlock(
        title = "Popup：更自由的浮层",
        description = "Popup 会把内容渲染到独立窗口，可指定对齐方式与是否可交互，适合自绘气泡、提示等场景。",
    ) {
        var show by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Button(onClick = { show = true }) { Text("打开 Popup") }
            if (show) {
                Popup(alignment = Alignment.Center, onDismissRequest = { show = false }) {
                    Card {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(text = "我是 Popup 里的内容", style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { show = false }) { Text("关闭") }
                        }
                    }
                }
            }
        }
    }
}
