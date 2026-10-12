package com.development.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.base.core.common.ApiResult
import com.development.base.core.common.DefaultPageSize
import com.development.base.domain.model.Post
import com.development.base.domain.usecase.GetPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FirstPage = 1

/**
 * 首页列表状态。
 *
 * @param loading 首屏加载中（列表为空时展示整屏 loading）。
 * @param refreshing 下拉刷新中。
 * @param loadingMore 上拉加载更多中。
 * @param posts 已加载的帖子，按页累积。
 * @param endReached 已经取完全部数据。
 * @param error 最近一次失败的提示，正常时为 null。
 */
data class HomeUiState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val posts: List<Post> = emptyList(),
    val endReached: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(private val getPosts: GetPostsUseCase) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState(loading = true));
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    /** 下一页页码。 */
    private var nextPage = FirstPage
    private var loadJob: Job? = null

    init {
        load()
    }

    /** 首屏加载（也用于失败重试）：拉取第 1 页，失败时回退本地缓存。 */
    fun load() = loadFirstPage(isRefresh = false)

    /** 下拉刷新：重新拉取第 1 页，失败时保留当前列表并提示错误。 */
    fun refresh() = loadFirstPage(isRefresh = true)

    /** 上拉加载更多：追加下一页；返回条数不足一页即认为已到底。 */
    fun loadMore() {
        if (loadJob?.isActive == true || _state.value.endReached) return
        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(loadingMore = true, error = null)
            val page = nextPage
            when (val result = getPosts.page(page, DefaultPageSize)) {
                is ApiResult.Success -> {
                    nextPage = page + 1
                    _state.value = _state.value.copy(
                        loadingMore = false,
                        posts = _state.value.posts + result.data,
                        endReached = result.data.size < DefaultPageSize
                    )
                }

                is ApiResult.Error -> _state.value =
                    _state.value.copy(loadingMore = false, error = result.message)
            }
        }
    }

    private fun loadFirstPage(isRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(loading = !isRefresh, refreshing = isRefresh, error = null)
            when (val result = getPosts.page(FirstPage, DefaultPageSize)) {
                is ApiResult.Success -> {
                    nextPage = FirstPage + 1
                    _state.value = HomeUiState(posts = result.data, endReached = result.data.size < DefaultPageSize)
                }

                is ApiResult.Error -> _state.value = if (isRefresh) {
                    _state.value.copy(loading = false, refreshing = false, error = result.message)
                } else {
                    HomeUiState(posts = getPosts(), endReached = true, error = result.message)
                }
            }
        }
    }
}
