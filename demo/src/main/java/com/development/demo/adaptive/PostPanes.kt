package com.development.demo.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.development.base.core.ui.AppImage
import com.development.base.domain.model.Post

/** 列表与详情两个面板：单栏、双栏、桌面模式三种布局都复用它们。 */

/** 详情正文最大宽度，避免大屏展开后文字行过长（约 45–75 字符的阅读舒适区）。 */
private val ReadableWidth = 640.dp

@Composable
internal fun PostList(
    posts: List<Post>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(posts, key = { it.id }) { post ->
            PostRow(post, selected = post.id == selectedId, onClick = { onSelect(post.id) })
        }
    }
}

@Composable
private fun PostRow(post: Post, selected: Boolean, onClick: () -> Unit) {
    val container = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = container)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AppImage(post.imageUrl, post.title, Modifier.size(56.dp).clip(MaterialTheme.shapes.medium))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(post.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(post.body, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
        }
    }
}

/**
 * 详情面板。
 *
 * @param post 未选中任何条目时为 null，显示空态提示。
 * @param onBack 单栏布局下需要“返回列表”；双栏/桌面模式传 null。
 */
@Composable
internal fun PostDetail(
    post: Post?,
    onRefresh: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (post == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("请选择一条数据查看详情", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        onBack?.let { TextButton(onClick = it) { Text("返回列表") } }
        AppImage(
            model = post.imageUrl,
            description = post.title,
            modifier = Modifier.fillMaxWidth().height(180.dp).clip(MaterialTheme.shapes.medium),
        )
        Text(post.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            text = post.body,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = ReadableWidth),
        )
        Button(onClick = onRefresh) { Text("刷新网络数据") }
    }
}
