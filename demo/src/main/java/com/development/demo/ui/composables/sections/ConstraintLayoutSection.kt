package com.development.demo.ui.composables.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import com.development.demo.ui.composables.ExampleBlock
import com.development.demo.ui.composables.ExampleLabel
import com.development.demo.ui.composables.ResultText

/**
 * 约束布局类：Compose 版 [ConstraintLayout] 的主要用法。
 *
 * 与「布局 Layout」一页的区别在于：这里展示的是**用约束描述关系**而不是用嵌套容器堆叠。
 * 每个示例都刻意把位置写进 `Modifier.constrainAs { }`，并给出常见的失效场景。
 */
@Composable
internal fun ConstraintLayoutSection() {
    BasicConstraintBlock()
    DimensionBlock()
    BiasBlock()
    GuidelineBlock()
    BarrierBlock()
    ChainBlock()
    RatioBlock()
    VisibilityBlock()
    ConstraintSetBlock()
    PitfallBlock()
}

/** 基础约束：createRefs + constrainAs + linkTo。 */
@Composable
private fun BasicConstraintBlock() {
    ExampleBlock(
        title = "基础约束：createRefs + constrainAs + linkTo",
        description = "ConstraintLayout 的每个子项都要用 Modifier.constrainAs(ref) 说明「相对谁定位、边距多少、尺寸怎么算」。"
            + "createRefs() 一次拿到一组引用，正好用解构声明分别命名。",
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth().height(112.dp)) {
            val (avatar, name, summary, badge) = createRefs()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .constrainAs(avatar) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    },
            )
            Text(
                text = "约束布局",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.constrainAs(name) {
                    top.linkTo(avatar.top)
                    start.linkTo(avatar.end, margin = 12.dp)
                    end.linkTo(parent.end)
                    // start / end 都约束时默认按 bias = 0.5 居中，想贴着头像就把它压到 0。
                    horizontalBias = 0f
                },
            )
            Text(
                text = "标题、说明、标签都挂在头像的 end 上；只有说明用 fillToConstraints 撑满剩余宽度。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(summary) {
                    top.linkTo(name.bottom, margin = 4.dp)
                    start.linkTo(avatar.end, margin = 12.dp)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            )
            Text(
                text = "fillToConstraints",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .constrainAs(badge) {
                        top.linkTo(summary.bottom, margin = 8.dp)
                        start.linkTo(avatar.end, margin = 12.dp)
                    },
            )
        }
        ResultText("所有间距都来自 linkTo 的 margin；两端都约束时默认居中，这里用 bias = 0f 贴住头像。")
    }
}

/** Dimension 的几种宽度写法对照。 */
@Composable
private fun DimensionBlock() {
    ExampleBlock(
        title = "Dimension：四种宽度写法",
        description = "width / height 只能写 Dimension 常量。默认是 wrapContent —— 只写 start 和 end 并不会让元素铺满，"
            + "必须显式写 fillToConstraints。",
    ) {
        val wrapColor = MaterialTheme.colorScheme.surfaceTint
        val fillColor = MaterialTheme.colorScheme.primary
        val valueColor = MaterialTheme.colorScheme.secondary
        val percentColor = MaterialTheme.colorScheme.tertiary

        DimensionRow(label = "wrapContent", barColor = wrapColor, barText = "内容宽度") { label ->
            start.linkTo(label.end, margin = 8.dp)
            end.linkTo(parent.end)
            width = Dimension.wrapContent
        }
        DimensionRow(label = "fillToConstraints", barColor = fillColor) { label ->
            start.linkTo(label.end, margin = 8.dp)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
        DimensionRow(label = "value(96.dp)", barColor = valueColor) { label ->
            start.linkTo(label.end, margin = 8.dp)
            end.linkTo(parent.end)
            width = Dimension.value(96.dp)
        }
        DimensionRow(label = "percent(0.4f)", barColor = percentColor) { label ->
            start.linkTo(label.end, margin = 8.dp)
            width = Dimension.percent(0.4f)
        }
        ResultText("背景是这一行的容器：只有 fillToConstraints 会一直顶到右边。")
    }
}

/**
 * bias 需要两端都有约束。
 *
 * @param endLinked 为 true 时同时约束 start 与 end，bias 才有可计算的区间。
 */
@Composable
private fun BiasRow(endLinked: Boolean, barColor: Color) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val bar = createRef()
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(barColor)
                .constrainAs(bar) {
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    if (endLinked) {
                        end.linkTo(parent.end, margin = 8.dp)
                    }
                    horizontalBias = 0.5f
                },
        )
    }
}

/** bias 对照：只约束单边 vs 两端都约束。 */
@Composable
private fun BiasBlock() {
    ExampleBlock(
        title = "bias：只有两端都约束了才生效",
        description = "horizontalBias / verticalBias 决定元素落在两端之间的哪个位置。"
            + "只写一边的约束就没有区间可算，此时 bias 写了也不起作用。",
        isPitfall = true,
    ) {
        ExampleLabel("错误：只约束 start，写 bias = 0.5f 也推不到中间")
        BiasRow(endLinked = false, barColor = MaterialTheme.colorScheme.error)
        ExampleLabel("正确：start 与 end 都约束，bias = 0.5f 才把它推到中间")
        BiasRow(endLinked = true, barColor = MaterialTheme.colorScheme.primary)
        ResultText("bias 取值 0f..1f：0 = 贴 start，0.5 = 居中，1 = 贴 end。")
    }
}

/** Guideline 参考线。 */
@Composable
private fun GuidelineBlock() {
    ExampleBlock(
        title = "Guideline：按百分比或固定间距切分",
        description = "createGuidelineFromStart(0.4f) 是父宽 40% 的垂直参考线，createGuidelineFromTop(56.dp) 是距顶部 56dp 的水平参考线，"
            + "两者都能像普通锚点一样被 linkTo 引用。",
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            val leftColumn = createRef()
            val rightColumn = createRef()
            val caption = createRef()
            val verticalGuide = createGuidelineFromStart(0.4f)
            val horizontalGuide = createGuidelineFromTop(56.dp)

            Text(
                text = "垂直参考线在父宽的 40% 处，水平参考线在距顶 56dp 处。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.constrainAs(caption) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .constrainAs(leftColumn) {
                        top.linkTo(horizontalGuide)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(verticalGuide, margin = 4.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    },
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .constrainAs(rightColumn) {
                        top.linkTo(horizontalGuide)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(verticalGuide, margin = 4.dp)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    },
            )
        }
    }
}

/** Barrier 障碍：按最宽的一方对齐。 */
@Composable
private fun BarrierBlock() {
    ExampleBlock(
        title = "Barrier：始终贴在最宽的那一行右侧",
        description = "createEndBarrier(short, long) 会跟着两者中最宽的一个走，比手写固定宽度更稳；"
            + "margin 参数让 Barrier 额外让开一点距离。",
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth().height(104.dp)) {
            val shortText = createRef()
            val longText = createRef()
            val marker = createRef()
            val follow = createRef()

            Text(
                text = "短标签",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.constrainAs(shortText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                },
            )
            Text(
                text = "第二行文本明显更长一些",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.constrainAs(longText) {
                    top.linkTo(shortText.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                },
            )
            val barrier = createEndBarrier(shortText, longText, margin = 12.dp)
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .constrainAs(marker) {
                        start.linkTo(barrier)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.value(2.dp)
                        height = Dimension.fillToConstraints
                    },
            )
            Text(
                text = "对齐线随内容宽度自动移动",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.constrainAs(follow) {
                    start.linkTo(barrier, margin = 8.dp)
                    top.linkTo(parent.top)
                },
            )
        }
    }
}

/** 链式排列：Spread / SpreadInside / Packed。 */
@Composable
private fun ChainBlock() {
    ExampleBlock(
        title = "Chain：把多个元素串成一条链",
        description = "createHorizontalChain(a, b, c) 之后的三个元素会自动互相约束，链的排布方式由 chainStyle 决定；"
            + "链两端仍然需要自己约束到父容器。",
    ) {
        ChainRow(label = "Spread：均匀分布", chainStyle = ChainStyle.Spread)
        ChainRow(label = "SpreadInside：首尾贴边，其余均分", chainStyle = ChainStyle.SpreadInside)
        ChainRow(label = "Packed：打包在一起居中", chainStyle = ChainStyle.Packed)
    }
}

@Composable
private fun ChainRow(label: String, chainStyle: ChainStyle) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    )
    ExampleLabel(label)
    ConstraintLayout(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        val (first, second, third) = createRefs()
        createHorizontalChain(first, second, third, chainStyle = chainStyle)
        listOf(first, second, third).forEachIndexed { index, ref ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors[index])
                    .constrainAs(ref) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
            )
        }
    }
}

/** dimensionRatio：由一边按比例算出另一边。 */
@Composable
private fun RatioBlock() {
    ExampleBlock(
        title = "dimensionRatio：由一边算出另一边",
        description = "把 height 写成 Dimension.ratio(\"16:9\")，高度就由宽度按比例算出，宽度变化时高度自动跟随，"
            + "不用手写 aspectRatio 修饰符。",
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth().height(112.dp)) {
            val banner = createRef()
            val square = createRef()
            val caption = createRef()

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .constrainAs(banner) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        width = Dimension.value(160.dp)
                        height = Dimension.ratio("16:9")
                    },
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .constrainAs(square) {
                        top.linkTo(parent.top)
                        start.linkTo(banner.end, margin = 12.dp)
                        width = Dimension.value(88.dp)
                        height = Dimension.ratio("1:1")
                    },
            )
            Text(
                text = "左：160dp × 90dp（16:9）　右：88dp × 88dp（1:1）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.constrainAs(caption) {
                    top.linkTo(banner.bottom, margin = 8.dp)
                    start.linkTo(parent.start)
                },
            )
        }
    }
}

/** Visibility.Gone：隐藏后约束重新分配。 */
@Composable
private fun VisibilityBlock() {
    var hideMiddle by remember { mutableStateOf(true) }
    ExampleBlock(
        title = "Visibility.Gone：隐藏的元素不再占位",
        description = "把 visibility 设为 Gone 后，该元素尺寸视为 0，链接在它下面的元素会自动上移；"
            + "用 Invisible 则只是看不见，位置仍然保留。",
    ) {
        Button(onClick = { hideMiddle = !hideMiddle }) {
            Text(text = if (hideMiddle) "显示中间项（Visible）" else "隐藏中间项（Gone）")
        }
        ConstraintLayout(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            val topBar = createRef()
            val middleBar = createRef()
            val bottomBar = createRef()

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .height(32.dp)
                    .constrainAs(topBar) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .height(32.dp)
                    .constrainAs(middleBar) {
                        top.linkTo(topBar.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        visibility = if (hideMiddle) Visibility.Gone else Visibility.Visible
                    },
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .height(32.dp)
                    .constrainAs(bottomBar) {
                        top.linkTo(middleBar.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
            )
        }
        ResultText("第三块永远贴在第二块下面 —— 第二块 Gone 之后它就往上顶。")
    }
}

/** ConstraintSet：约束集中声明，整体切换。 */
@Composable
private fun ConstraintSetBlock() {
    var expanded by remember { mutableStateOf(false) }
    val constraintSet = remember(expanded) {
        ConstraintSet {
            val avatar = createRefFor(AvatarId)
            val title = createRefFor(TitleId)
            val description = createRefFor(DescriptionId)
            constrain(avatar) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
            constrain(title) {
                top.linkTo(if (expanded) avatar.bottom else avatar.top, margin = if (expanded) 8.dp else 0.dp)
                start.linkTo(if (expanded) parent.start else avatar.end, margin = if (expanded) 0.dp else 12.dp)
                end.linkTo(parent.end)
                horizontalBias = 0f
            }
            constrain(description) {
                top.linkTo(title.bottom, margin = 4.dp)
                start.linkTo(if (expanded) parent.start else avatar.end, margin = if (expanded) 0.dp else 12.dp)
                end.linkTo(parent.end)
                horizontalBias = 0f
            }
        }
    }
    ExampleBlock(
        title = "ConstraintSet：把约束集中声明",
        description = "约束也可以不写在布局里，而是先用 createRefFor(id) 建好一份 ConstraintSet，"
            + "再由 ConstraintLayout(constraintSet) 应用；布局内容只用 Modifier.layoutId 认领位置，切换整套约束非常方便。",
    ) {
        Button(onClick = { expanded = !expanded }) {
            Text(text = if (expanded) "切回卡片布局" else "切成上下布局")
        }
        ConstraintLayout(constraintSet = constraintSet, modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .layoutId(AvatarId),
            )
            Text(
                text = "声明式约束",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.layoutId(TitleId),
            )
            Text(
                text = "同一份布局内容，两套约束：一份挂着头像排，一份整体上下排列。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.layoutId(DescriptionId),
            )
        }
    }
}

/** 易错点：漏写 constrainAs 的子项。 */
@Composable
private fun PitfallBlock() {
    ExampleBlock(
        title = "易错点：漏写 constrainAs 的子项",
        description = "没有 constrainAs 的子项不参与任何约束关系，不会跟着别的元素排布，"
            + "位置只由它自己的修饰符决定 —— 在 ConstraintLayout 里通常表现为压在左上角、与其它元素重叠。",
        isPitfall = true,
    ) {
        ExampleLabel("错误：第二个方块忘了 constrainAs")
        ConstraintLayout(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            val first = createRef()
            val second = createRef()
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .constrainAs(first) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    },
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.error),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .constrainAs(second) {
                        top.linkTo(parent.top)
                        start.linkTo(first.end, margin = 64.dp)
                    },
            )
        }
        ExampleLabel("正确：每个子项都写明相对谁定位")
        ConstraintLayout(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            val first = createRef()
            val second = createRef()
            val third = createRef()
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .constrainAs(first) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    },
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .constrainAs(second) {
                        top.linkTo(parent.top)
                        start.linkTo(first.end, margin = 8.dp)
                    },
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .constrainAs(third) {
                        top.linkTo(parent.top)
                        start.linkTo(second.end, margin = 8.dp)
                    },
            )
        }
    }
}

/**
 * 一行 Dimension 对照：左边固定宽度的名称，右边是按给定约束摆放的色块。
 *
 * @param barText 色块内的文字；为空时色块只按 [barConstraints] 的 width / height 显示。
 * @param barConstraints 在 [ConstrainScope] 上补充该行的约束，参数是左侧名称的引用。
 */
@Composable
private fun DimensionRow(
    label: String,
    barColor: Color,
    barText: String? = null,
    barConstraints: ConstrainScope.(label: ConstrainedLayoutReference) -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val (labelRef, barRef) = createRefs()
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            modifier = Modifier.constrainAs(labelRef) {
                start.linkTo(parent.start, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                width = Dimension.value(112.dp)
            },
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(barColor)
                .constrainAs(barRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.value(14.dp)
                    barConstraints(labelRef)
                },
        ) {
            if (barText != null) {
                Text(
                    text = barText,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }
        }
    }
}

/** ConstraintSet 示例里布局内容与约束共用的 id。 */
private const val AvatarId = "avatar"
private const val TitleId = "title"
private const val DescriptionId = "description"
