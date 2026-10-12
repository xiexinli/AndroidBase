package com.development.demo.adaptive

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo

/**
 * 折叠设备当前姿态，由 [FoldingFeature] 归一化而来。
 *
 * 非折叠设备、或折叠区域不分割窗口（例如只有折痕）时统一视为 [FLAT]。
 */
enum class FoldPosture {
    /** 平铺：直板手机，或已完全展开的平板形态，单/多栏由窗口宽度决定。 */
    FLAT,

    /** 桌面模式（tabletop）：水平折叠，适合内容在上、操作在下。 */
    TABLETOP,

    /** 书本模式（book）：垂直折叠，适合左右分栏，并让内容避开铰链区域。 */
    BOOK,
}

/**
 * 读取当前窗口的尺寸等级。
 *
 * 这里取 `LocalConfiguration` 的窗口尺寸而不是物理屏幕尺寸：分屏、自由窗口、折叠展开
 * 都会改变它，这正是「按可用空间而不是按设备型号」判断布局的依据。
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(
            widthDp = configuration.screenWidthDp,
            heightDp = configuration.screenHeightDp,
        )
    }
}

/**
 * 订阅窗口布局信息并取出折叠特征；设备没有折叠区域时返回 null。
 *
 * 需要 [androidx.activity.ComponentActivity]：`WindowInfoTracker` 是 Activity 级的，
 * 在 Compose 里通过 `LocalActivity` 拿到的就是当前宿主 Activity。
 */
@Composable
fun rememberFoldingFeature(): FoldingFeature? {
    val activity = LocalActivity.current ?: return null
    val layoutInfo by remember(activity) {
        WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity)
    }.collectAsStateWithLifecycle(initialValue = WindowLayoutInfo(emptyList()))
    return remember(layoutInfo) {
        layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
    }
}

/** 把折叠特征映射成布局可直接使用的姿态。 */
fun FoldingFeature?.toFoldPosture(): FoldPosture = when {
    this == null || !isSeparating -> FoldPosture.FLAT
    orientation == FoldingFeature.Orientation.HORIZONTAL -> FoldPosture.TABLETOP
    else -> FoldPosture.BOOK
}
