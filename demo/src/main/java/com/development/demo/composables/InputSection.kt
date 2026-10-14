package com.development.demo.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/** 输入与选择类：OutlinedTextField / Checkbox / Switch / RadioButton / Slider。 */
@Composable
internal fun InputSection() {
    var email by remember { mutableStateOf("") }

    ExampleBlock(
        title = "OutlinedTextField：受控输入",
        description = "value + onValueChange 是受控组件；label 是浮动标签，supportingText 放校验提示，" +
            "isError 会把边框与提示切成错误色。",
    ) {
        val isError = email.isNotEmpty() && !email.contains('@')
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("邮箱") },
            placeholder = { Text("name@example.com") },
            supportingText = { Text(if (isError) "邮箱格式不正确" else "我们会用它发送通知") },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Text("@", style = MaterialTheme.typography.bodyMedium) },
            trailingIcon = {
                if (email.isNotEmpty()) {
                    IconButton(onClick = { email = "" }) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "清空")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        ResultText("当前值：\"$email\"")
    }

    ExampleBlock(
        title = "Checkbox 与 Switch：布尔选择",
        description = "Checkbox 用于「确认/多选」，Switch 用于「立即生效的开关」；" +
            "两者都应把整行做成可点区域，而不是只点小方块。",
    ) {
        var agreed by remember { mutableStateOf(false) }
        var wifiEnabled by remember { mutableStateOf(true) }
        var notifications by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth().clickable { agreed = !agreed },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = agreed, onCheckedChange = { agreed = it })
            Text(text = "我已阅读并同意用户协议", style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            modifier = Modifier.fillMaxWidth().clickable { wifiEnabled = !wifiEnabled },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Switch(checked = wifiEnabled, onCheckedChange = { wifiEnabled = it })
            Text(text = "仅 Wi-Fi 下下载", style = MaterialTheme.typography.bodyMedium)
        }
        Row(
            modifier = Modifier.fillMaxWidth().clickable { notifications = !notifications },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Switch(checked = notifications, onCheckedChange = { notifications = it })
            Text(text = "接收推送通知", style = MaterialTheme.typography.bodyMedium)
        }
        ResultText("agreed = $agreed ｜ wifiOnly = $wifiEnabled ｜ push = $notifications")
    }

    ExampleBlock(
        title = "RadioButton：互斥单选",
        description = "同组 RadioButton 共享同一个 selected 状态；点击整行切换（selectable/clickable 都要一并设置）。",
    ) {
        var plan by remember { mutableIntStateOf(0) }
        val plans = listOf("免费版", "专业版", "企业版")
        plans.forEachIndexed { index, name ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { plan = index },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = plan == index, onClick = { plan = index })
                Text(text = name, style = MaterialTheme.typography.bodyMedium)
            }
        }
        ResultText("已选：${plans[plan]}")
    }

    ExampleBlock(
        title = "Slider：连续取值",
        description = "valueRange 定义区间，steps 定义中间刻度数（0 表示连续）；" +
            "变化频繁时可只保留一个节流后的状态。",
    ) {
        var progress by remember { mutableFloatStateOf(0.3f) }
        var rating by remember { mutableFloatStateOf(3f) }
        Text(text = "进度：${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        Slider(value = progress, onValueChange = { progress = it })
        Text(text = "评分：${rating.toInt()} 分（1–5，4 个刻度）", style = MaterialTheme.typography.bodySmall)
        Slider(value = rating, onValueChange = { rating = it }, valueRange = 1f..5f, steps = 3)
    }

    ExampleBlock(
        title = "易错点：TextField 的 value 没有 remember → 输入没有反应",
        description = "缺了 remember，每次重组都会新建一个 State，写入的值立刻丢失。" +
            "表现就是「键盘敲了，输入框纹丝不动」——这是最常见的 Compose 新手问题。",
        isPitfall = true,
    ) {
        var broken by mutableStateOf("")
        OutlinedTextField(
            value = broken,
            onValueChange = { broken = it },
            label = { Text("缺 remember：输入无反应") },
            modifier = Modifier.fillMaxWidth(),
        )
        var working by remember { mutableStateOf("") }
        OutlinedTextField(
            value = working,
            onValueChange = { working = it },
            label = { Text("加了 remember：正常输入") },
            modifier = Modifier.fillMaxWidth(),
        )
        ResultText("对照两行输入框的响应即可复现")
    }
}
