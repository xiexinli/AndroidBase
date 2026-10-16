package com.development.demo.ui.composables

import androidx.compose.runtime.Composable
import com.development.demo.ui.composables.sections.AnimationSection
import com.development.demo.ui.composables.sections.BasicsSection
import com.development.demo.ui.composables.sections.ConstraintLayoutSection
import com.development.demo.ui.composables.sections.EffectSection
import com.development.demo.ui.composables.sections.InputSection
import com.development.demo.ui.composables.sections.InteractionSection
import com.development.demo.ui.composables.sections.LayoutSection
import com.development.demo.ui.composables.sections.ListSection
import com.development.demo.ui.composables.sections.OverlaySection
import com.development.demo.ui.composables.sections.PitfallSection
import com.development.demo.ui.composables.sections.StateSection
import com.development.demo.ui.composables.sections.ThemeSection

/**
 * 「一类 Composable」的示例定义，对应目录页里的一条二级页面。
 *
 * @param id 路由参数，在 [ComposablesCategories] 内唯一。
 * @param title 目录页与二级页面标题栏使用的名称。
 * @param summary 一句话说明这一类 Composable 解决什么问题。
 * @param content 该类别的示例内容，由 [ComposablesCategoryScreen] 的脚手架承载。
 */
internal data class ComposablesCategory(
    val id: String,
    val title: String,
    val summary: String,
    val content: @Composable () -> Unit,
)

/** 目录清单：[ComposablesCatalogScreen] 展示它，二级页面按 [ComposablesCategory.id] 查找。 */
internal val ComposablesCategories: List<ComposablesCategory> = listOf(
    ComposablesCategory(
        id = "layout",
        title = "布局 Layout",
        summary = "Column / Row / Box / Spacer / weight / FlowRow 与尺寸修饰符",
    ) { LayoutSection() },
    ComposablesCategory(
        id = "constraint",
        title = "约束布局 ConstraintLayout",
        summary = "constrainAs / Guideline / Barrier / Chain / ConstraintSet",
    ) { ConstraintLayoutSection() },
    ComposablesCategory(
        id = "basic",
        title = "基础组件 Basics",
        summary = "Text / Button 家族 / IconButton / Card / Divider",
    ) { BasicsSection() },
    ComposablesCategory(
        id = "input",
        title = "输入与选择 Input",
        summary = "OutlinedTextField / Checkbox / Switch / RadioButton / Slider",
    ) { InputSection() },
    ComposablesCategory(
        id = "list",
        title = "列表与网格 Lists",
        summary = "LazyColumn / LazyRow / LazyVerticalGrid，以及 key 的正确用法",
    ) { ListSection() },
    ComposablesCategory(
        id = "state",
        title = "状态 State",
        summary = "remember / rememberSaveable / 状态提升 / derivedStateOf",
    ) { StateSection() },
    ComposablesCategory(
        id = "effect",
        title = "副作用 Effects",
        summary = "LaunchedEffect / DisposableEffect / produceState / rememberUpdatedState",
    ) { EffectSection() },
    ComposablesCategory(
        id = "interaction",
        title = "交互与手势 Interaction",
        summary = "clickable / combinedClickable / pointerInput，以及 Modifier 顺序",
    ) { InteractionSection() },
    ComposablesCategory(
        id = "animation",
        title = "动画 Animation",
        summary = "animate*AsState / AnimatedVisibility / Crossfade / Animatable",
    ) { AnimationSection() },
    ComposablesCategory(
        id = "overlay",
        title = "弹窗与浮层 Overlay",
        summary = "AlertDialog / ModalBottomSheet / Snackbar / DropdownMenu / Popup",
    ) { OverlaySection() },
    ComposablesCategory(
        id = "theme",
        title = "主题与样式 Theme",
        summary = "colorScheme / typography / shapes 与局部主题覆盖",
    ) { ThemeSection() },
    ComposablesCategory(
        id = "pitfall",
        title = "易错点与性能 Pitfalls",
        summary = "重组、副作用、状态提升、列表 key 等最容易踩的坑",
    ) { PitfallSection() },
)
