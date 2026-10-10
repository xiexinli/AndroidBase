package com.development.base.domain.usecase

import com.development.base.domain.repository.PostRepository
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(private val repository: PostRepository) {
    suspend operator fun invoke() = repository.posts()
    suspend fun refresh() = repository.refreshPosts()
}
