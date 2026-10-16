package com.development.demo.ui.adaptive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.base.core.common.ApiResult
import com.development.base.core.common.DefaultPageSize
import com.development.base.domain.model.Post
import com.development.base.domain.usecase.GetPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** demo 自己持有示例页状态；数据能力仍由 :base 的 domain/use case 提供。 */
data class DemoPostsUiState(
    val loading: Boolean = true,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class DemoPostsViewModel @Inject constructor(
    private val getPosts: GetPostsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DemoPostsUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val result = getPosts.page(page = 1, pageSize = DefaultPageSize)) {
                is ApiResult.Success -> _state.value = DemoPostsUiState(loading = false, posts = result.data)
                is ApiResult.Error -> _state.value = DemoPostsUiState(
                    loading = false,
                    posts = getPosts(),
                    error = result.message,
                )
            }
        }
    }
}
