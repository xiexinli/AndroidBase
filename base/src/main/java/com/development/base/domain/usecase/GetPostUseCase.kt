package com.development.base.domain.usecase

import com.development.base.domain.repository.PostRepository
import javax.inject.Inject

/** 按 id 读取单条帖子，供详情页使用。 */
class GetPostUseCase @Inject constructor(private val repository: PostRepository) {
    suspend operator fun invoke(id: Int) = repository.post(id)
}
