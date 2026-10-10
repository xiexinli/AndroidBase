package com.development.base.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.AsyncImage

/**
 * 应用统一的图片加载组件。
 *
 * 业务层不应直接使用 Coil 的 [AsyncImage]，应通过此组件加载远程或本地图片。
 *
 * @param model 图片地址、资源标识或其他 Coil 支持的模型。
 * @param description 图片的无障碍描述。
 * @param modifier 应用于图片的修饰符。
 * @param contentScale 图片在布局中的缩放方式。
 * @param placeholder 图片加载期间显示的占位图。
 * @param error 图片加载失败时显示的图片。
 * @param fallback 图片模型为空时显示的图片。
 */
@Composable
fun AppImage(
    model: Any?,
    description: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = null,
) {
    AsyncImage(
        model = model,
        contentDescription = description,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
    )
}
