package com.development.app.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.base.domain.model.Post
import com.development.base.domain.usecase.GetPostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val loading: Boolean = false,
    val post: Post? = null,
    val error: String? = null
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val getPost: GetPostUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: Int = checkNotNull(savedStateHandle[PostDetailDestination.PostIdArg]) {
        "缺少导航参数 ${PostDetailDestination.PostIdArg}"
    }

    private val _state = MutableStateFlow(PostDetailUiState(loading = true))
    val state: StateFlow<PostDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val post = getPost(postId)
            _state.value = PostDetailUiState(post = post, error = if (post == null) PostNotFound else null)
        }
    }

    private companion object {
        const val PostNotFound = "本地缓存中没有这条数据，请返回列表下拉刷新后重试"
    }
}
