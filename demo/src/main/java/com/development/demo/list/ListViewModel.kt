package com.development.demo.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 列表项。
 *
 * 这里刻意只带一个 [colorSeed] 而不是网络图片：示例要能在离线下完整展示多布局差异，
 * 不引入 Coil / 网络依赖。
 */
data class DemoListItem(
    val id: Int,
    val title: String,
    val summary: String,
    val tag: String,
    val colorSeed: Int,
)

/** 列表页状态。 */
data class DemoListUiState(
    val items: List<DemoListItem> = emptyList(),
    /** 首次进入的加载态（此时列表还没有数据）。 */
    val initialLoading: Boolean = true,
    /** 下拉刷新中。 */
    val refreshing: Boolean = false,
    /** 上拉加载更多中。 */
    val loadingMore: Boolean = false,
    val hasMore: Boolean = true,
)

/**
 * 分页列表示例的 ViewModel。
 *
 * **数据来源说明**：这里的列表数据是**刻意伪造**的演示数据，只服务于 `:demo` 的 UI 示例，
 * 不接入 `:base` 的网络 / Room 缓存链路（真实业务应走 `domain/usecase`）。
 * 分页、刷新、加载更多的状态机是真实的，替换数据源即可变成真分页。
 */
@HiltViewModel
class ListViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DemoListUiState())
    val state = _state.asStateFlow()

    /** 刷新时轮换文案，让「下拉刷新」有肉眼可见的结果。 */
    private var refreshRound = 0

    init {
        viewModelScope.launch {
            delay(InitialLoadDelayMillis)
            _state.update {
                it.copy(
                    items = fakePage(page = 1),
                    initialLoading = false,
                    hasMore = TotalPages > 1,
                )
            }
        }
    }

    /** 下拉刷新：丢掉已加载的分页，回到第一页。 */
    fun refresh() {
        if (_state.value.refreshing) return
        _state.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            delay(RefreshDelayMillis)
            refreshRound++
            _state.update {
                it.copy(
                    items = fakePage(page = 1),
                    refreshing = false,
                    loadingMore = false,
                    hasMore = TotalPages > 1,
                )
            }
        }
    }

    /**
     * 上拉加载更多。
     *
     * 这里同步置位 [DemoListUiState.loadingMore] 再启动协程，避免列表快速滚动时并发触发多次加载。
     */
    fun loadMore() {
        val current = _state.value
        if (current.loadingMore || current.refreshing || current.initialLoading || !current.hasMore) return
        val page = current.items.size / PageSize + 1
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            delay(LoadMoreDelayMillis)
            _state.update {
                it.copy(
                    items = it.items + fakePage(page = page),
                    loadingMore = false,
                    hasMore = page < TotalPages,
                )
            }
        }
    }

    private fun fakePage(page: Int): List<DemoListItem> {
        val start = (page - 1) * PageSize
        return List(PageSize) { offset ->
            val id = start + offset
            DemoListItem(
                id = id,
                title = "第 $page 页 · 条目 ${id + 1}",
                summary = Summaries[(id + refreshRound) % Summaries.size],
                tag = Tags[id % Tags.size],
                colorSeed = id % 3,
            )
        }
    }

    private companion object {
        const val PageSize = 12
        const val TotalPages = 6
        const val InitialLoadDelayMillis = 700L
        const val RefreshDelayMillis = 900L
        const val LoadMoreDelayMillis = 800L

        val Summaries = listOf(
            "下拉刷新会重新从第一页开始加载，并把已加载的分页丢弃。",
            "上拉接近底部时自动加载下一页，直到没有更多数据为止。",
            "同一个纵向列表中按行混排：有单列行、双列行和三列行。",
            "每行的列数由该行包含的假数据条目数决定，不是整页切换列数。",
            "加载更多的并发保护放在 ViewModel，UI 只负责上报「快到底了」。",
        )

        val Tags = listOf("推荐", "热门", "置顶", "新")
    }
}
