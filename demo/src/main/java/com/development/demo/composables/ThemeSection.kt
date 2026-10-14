package com.development.demo.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 主题与样式类：colorScheme / typography / shapes 与局部主题覆盖。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ThemeSection() {
    ExampleBlock(
        title = "colorScheme：颜色令牌",
        description = "所有颜色都从 MaterialTheme.colorScheme 取，才能在深色模式与换肤时自动适配；" +
            "业务层不要硬编码 Color(0xFF...)。",
    ) {
        val swatches = listOf(
            "primary" to MaterialTheme.colorScheme.primary,
            "secondary" to MaterialTheme.colorScheme.secondary,
            "tertiary" to MaterialTheme.colorScheme.tertiary,
            "error" to MaterialTheme.colorScheme.error,
            "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
            "secondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
            "surfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
            "outline" to MaterialTheme.colorScheme.outline,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            swatches.forEach { (name, color) ->
                Box(
                    modifier = Modifier.size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(color),
                )
            }
        }
        Text(
            text = swatches.joinToString(" / ") { it.first },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    ExampleBlock(
        title = "typography：文字层级",
        description = "用 MaterialTheme.typography.xxx 而不是硬编码 fontSize，保证全站层级一致、也便于统一换字体。",
    ) {
        listOf(
            "displaySmall" to MaterialTheme.typography.displaySmall,
            "headlineSmall" to MaterialTheme.typography.headlineSmall,
            "titleMedium" to MaterialTheme.typography.titleMedium,
            "bodyLarge" to MaterialTheme.typography.bodyLarge,
            "labelSmall" to MaterialTheme.typography.labelSmall,
        ).forEach { (name, style) ->
            Text(text = "$name：Compose 主题排版", style = style)
        }
    }

    ExampleBlock(
        title = "shapes：圆角档位",
        description = "MaterialTheme.shapes 提供 extraSmall / small / medium / large / extraLarge 五档，统一用它们而不是随手写 RoundedCornerShape(7.dp)。",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(
                "small" to MaterialTheme.shapes.small,
                "medium" to MaterialTheme.shapes.medium,
                "large" to MaterialTheme.shapes.large,
            ).forEach { (name, shape) ->
                Box(
                    modifier = Modifier.size(56.dp)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = name, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    ExampleBlock(
        title = "局部主题覆盖与深色模式",
        description = "可以在任意子树外再包一层 MaterialTheme 覆盖令牌；深色方案用 darkColorScheme()。" +
            "真实项目里通常在 Application/Activity 顶层按 isSystemInDarkTheme() 二选一。",
    ) {
        Text(
            text = "当前系统是否深色：${isSystemInDarkTheme()}",
            style = MaterialTheme.typography.bodySmall,
        )
        MaterialTheme(colorScheme = darkColorScheme()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
            ) {
                Text(
                    text = "这一段被局部包成了深色主题",
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    ExampleBlock(
        title = "易错点：硬编码颜色不随主题变化",
        description = "写死 Color(0xFF6750A4) 会绕开主题令牌：切到深色模式时既不适配，也不利于统一换肤，" +
            "在代码评审里属于应当拦下的问题。",
        isPitfall = true,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFF6750A4)),
            )
            Box(
                Modifier.size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        ResultText("左：硬编码 0xFF6750A4 ｜ 右：colorScheme.primary")
    }
}
