package com.development.demo.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** 距离列表末尾还剩几行时开始预加载下一页。 */
private const val LoadMoreThreshold = 3

@Composable
internal fun ListRoute(
    onBack: () -> Unit,
    viewModel: ListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ListScreen(state, viewModel::refresh, viewModel::loadMore, onBack)
}

/** 同一个纵向列表中混排单列、双列、三列行。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListScreen(
    state: DemoListUiState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分页列表 · 混合列布局") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text = "同一列表混排：单列、每行 2 项、每行 3 项；下拉刷新或滚动到底部加载更多。",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (state.initialLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    MixedItemList(state, onLoadMore)
                }
            }
        }
    }
}

private data class MixedRow(val items: List<DemoListItem>)

/** 按 1、2、3 项循环分行，保证同一 LazyColumn 内出现三种行布局。 */
private fun mixedRows(items: List<DemoListItem>): List<MixedRow> {
    val rows = mutableListOf<MixedRow>()
    var itemIndex = 0
    var rowIndex = 0
    while (itemIndex < items.size) {
        val count = rowIndex % 3 + 1
        val end = minOf(itemIndex + count, items.size)
        rows += MixedRow(items.subList(itemIndex, end))
        itemIndex = end
        rowIndex++
    }
    return rows
}

@Composable
private fun MixedItemList(state: DemoListUiState, onLoadMore: () -> Unit) {
    val rows = remember(state.items) { mixedRows(state.items) }
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            listState.layoutInfo.totalItemsCount > 0 && lastVisible >= listState.layoutInfo.totalItemsCount - LoadMoreThreshold
        }
    }
    LaunchedEffect(shouldLoadMore, state.loadingMore, state.hasMore) {
        if (shouldLoadMore && !state.loadingMore && state.hasMore) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(rows, key = { row -> row.items.first().id }) { row ->
            MixedItemRow(row.items)
        }
        item(key = "load-more-footer") { LoadMoreFooter(state) }
    }
}

@Composable
private fun MixedItemRow(items: List<DemoListItem>) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            DemoListItemCard(
                item = item,
                compact = items.size == 3,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DemoListItemCard(item: DemoListItem, compact: Boolean, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.fillMaxWidth()) {
            Thumbnail(item, Modifier.fillMaxWidth().aspectRatio(if (compact) 1f else 16f / 9f))
            ItemTexts(
                item = item,
                maxSummaryLines = if (compact) 2 else 3,
                modifier = Modifier.padding(if (compact) 8.dp else 12.dp),
            )
        }
    }
}

@Composable
private fun Thumbnail(item: DemoListItem, modifier: Modifier) {
    val containerColor = when (item.colorSeed) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    Box(
        modifier = modifier.clip(MaterialTheme.shapes.small).background(containerColor),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text = item.tag,
            modifier = Modifier.padding(6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ItemTexts(item: DemoListItem, maxSummaryLines: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            item.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = maxSummaryLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LoadMoreFooter(state: DemoListUiState) {
    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        when {
            state.loadingMore -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("正在加载更多…", style = MaterialTheme.typography.bodySmall)
            }
            !state.hasMore -> Text(
                "没有更多了",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> Text(
                "上拉加载更多",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
