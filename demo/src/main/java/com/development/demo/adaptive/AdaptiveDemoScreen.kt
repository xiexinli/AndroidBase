package com.development.demo.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import androidx.window.layout.FoldingFeature
import com.development.base.feature.home.HomeUiState
import com.development.base.feature.home.HomeViewModel

/** 「未选中任何条目」的哨兵值；用 -1 而不是可空状态，便于 `rememberSaveable` 保存。 */
private const val NoSelection = -1

/**
 * 折叠屏自适应示例入口。
 *
 * 数据来自 `:base` 的样例页 ViewModel（真实网络数据 + Room 缓存回退），
 * 这里只演示布局如何随窗口尺寸与折叠姿态变化。
 */
@Composable
fun AdaptiveDemoRoute(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AdaptiveDemoScreen(
        state = state,
        windowSizeClass = rememberWindowSizeClass(),
        foldingFeature = rememberFoldingFeature(),
        onRefresh = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdaptiveDemoScreen(
    state: HomeUiState,
    windowSizeClass: WindowSizeClass,
    foldingFeature: FoldingFeature?,
    onRefresh: () -> Unit,
) {
    val posture = foldingFeature.toFoldPosture()
    val hingeSpacing = rememberHingeSpacing(foldingFeature)
    val twoPane = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    // rememberSaveable：折叠/展开、旋转、分屏都会重建 Activity，选中项需要保留。
    var selectedId by rememberSaveable { mutableStateOf(NoSelection) }

    Scaffold(topBar = { TopAppBar(title = { Text("折叠屏自适应示例") }) }) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            AdaptiveStatusBanner(windowSizeClass, posture, foldingFeature, hingeSpacing)
            DataStatusLine(state)
            Box(Modifier.fillMaxSize()) {
                when {
                    posture == FoldPosture.TABLETOP -> TabletopLayout(
                        state = state,
                        selectedId = selectedId,
                        hingeSpacing = hingeSpacing,
                        onSelect = { selectedId = it },
                        onRefresh = onRefresh,
                    )

                    twoPane -> TwoPaneLayout(
                        state = state,
                        selectedId = selectedId,
                        hingeSpacing = hingeSpacing,
                        onSelect = { selectedId = it },
                        onRefresh = onRefresh,
                    )

                    else -> CompactLayout(
                        state = state,
                        selectedId = selectedId,
                        onSelect = { selectedId = it },
                        onBack = { selectedId = NoSelection },
                        onRefresh = onRefresh,
                    )
                }
            }
        }
    }
}

/** 单栏：列表与详情分开导航，适合紧凑窗口。 */
@Composable
private fun CompactLayout(
    state: HomeUiState,
    selectedId: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    val selected = state.posts.firstOrNull { it.id == selectedId }
    if (selected == null) {
        PostList(state.posts, selectedId, onSelect, Modifier.fillMaxSize())
    } else {
        PostDetail(selected, onRefresh, onBack, Modifier.fillMaxSize())
    }
}

/** 双栏：中等及以上窗口把列表与详情并排；书本模式下在中间留出铰链宽度。 */
@Composable
private fun TwoPaneLayout(
    state: HomeUiState,
    selectedId: Int,
    hingeSpacing: Dp,
    onSelect: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    val selected = state.posts.firstOrNull { it.id == selectedId }
    Row(Modifier.fillMaxSize()) {
        PostList(state.posts, selectedId, onSelect, Modifier.weight(1f))
        if (hingeSpacing > 0.dp) Spacer(Modifier.width(hingeSpacing)) else VerticalDivider()
        PostDetail(selected, onRefresh, onBack = null, modifier = Modifier.weight(1f))
    }
}

/** 桌面模式：内容放在上半屏，列表与操作放到下半屏，避开水平方向的折痕。 */
@Composable
private fun TabletopLayout(
    state: HomeUiState,
    selectedId: Int,
    hingeSpacing: Dp,
    onSelect: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    val selected = state.posts.firstOrNull { it.id == selectedId }
    Column(Modifier.fillMaxSize()) {
        PostDetail(selected, onRefresh, onBack = null, modifier = Modifier.weight(1f))
        if (hingeSpacing > 0.dp) Spacer(Modifier.height(hingeSpacing)) else HorizontalDivider()
        PostList(state.posts, selectedId, onSelect, Modifier.weight(1f))
    }
}

/**
 * 折叠区域在 dp 下的尺寸，用来把内容从铰链上让开。
 *
 * 这里只按尺寸预留间距（示例做法）；真实产品若要精确对齐，可用 `FoldingFeature.bounds`
 * 在窗口坐标系中的位置来分配两侧面板宽度。
 */
@Composable
private fun rememberHingeSpacing(feature: FoldingFeature?): Dp {
    if (feature == null || !feature.isSeparating) return 0.dp
    val density = LocalDensity.current
    return remember(feature, density) {
        with(density) {
            val hinge = feature.bounds
            if (feature.orientation == FoldingFeature.Orientation.HORIZONTAL) {
                hinge.height().toDp()
            } else {
                hinge.width().toDp()
            }
        }
    }
}

/** 把当前的窗口尺寸、折叠姿态与铰链信息显示出来，方便在模拟器上肉眼验证检测结果。 */
@Composable
private fun AdaptiveStatusBanner(
    windowSizeClass: WindowSizeClass,
    posture: FoldPosture,
    foldingFeature: FoldingFeature?,
    hingeSpacing: Dp,
) {
    val configuration = LocalConfiguration.current
    val summary = "窗口 ${configuration.screenWidthDp}×${configuration.screenHeightDp}dp" +
        " · ${windowSizeClass.widthLabel()}" +
        " · ${posture.label()}" +
        if (hingeSpacing > 0.dp) " · 铰链 ${hingeSpacing.value.toInt()}dp" else ""
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(summary, style = MaterialTheme.typography.labelLarge)
        foldingFeature?.let {
            val separating = if (it.isSeparating) "分割窗口" else "仅有折痕"
            Text(
                text = "折叠区域：${it.orientation} · $separating · 遮挡 ${it.occlusionType}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 与 `:base` 样例页一致的加载 / 异常提示。 */
@Composable
private fun DataStatusLine(state: HomeUiState) {
    val error = state.error
    when {
        error != null -> Text(
            text = "网络异常：$error（已展示本地缓存）",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        state.loading -> Text(
            text = "正在加载示例数据…",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

private fun WindowSizeClass.widthLabel(): String = when {
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> "扩展 expanded（≥840dp）"
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> "中等 medium（600–840dp）"
    else -> "紧凑 compact（<600dp）"
}

private fun FoldPosture.label(): String = when (this) {
    FoldPosture.FLAT -> "平铺"
    FoldPosture.TABLETOP -> "桌面模式（上下分区）"
    FoldPosture.BOOK -> "书本模式（左右分栏）"
}
