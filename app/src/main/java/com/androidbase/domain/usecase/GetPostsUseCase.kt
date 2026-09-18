package com.androidbase.domain.usecase

import com.androidbase.domain.repository.PostRepository
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(private val repository: PostRepository) {
    suspend operator fun invoke() = repository.posts()
    suspend fun refresh() = repository.refreshPosts()
}
