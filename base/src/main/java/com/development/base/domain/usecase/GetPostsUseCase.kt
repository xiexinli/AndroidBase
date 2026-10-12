package com.development.base.domain.usecase

import com.development.base.domain.repository.PostRepository
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(private val repository: PostRepository) {
    /** 读取本地缓存的帖子列表（无网时的回退数据源）。 */
    suspend operator fun invoke() = repository.cachedPosts()

    /** 拉取第 [page] 页（从 1 开始）。 */
    suspend fun page(page: Int, pageSize: Int) = repository.loadPage(page, pageSize)
}
