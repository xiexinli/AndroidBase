package com.androidbase.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidbase.core.common.ApiResult
import com.androidbase.domain.model.Post
import com.androidbase.domain.usecase.GetPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(val loading: Boolean = false, val posts: List<Post> = emptyList(), val error: String? = null)
@HiltViewModel class HomeViewModel @Inject constructor(private val getPosts: GetPostsUseCase) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState(loading = true)); val state: StateFlow<HomeUiState> = _state.asStateFlow()
    init { load() }
    fun load() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)
        when (val result = getPosts.refresh()) {
            is ApiResult.Success -> _state.value = HomeUiState(posts = getPosts())
            is ApiResult.Error -> _state.value = HomeUiState(posts = getPosts(), error = result.message)
        }
    }
}
