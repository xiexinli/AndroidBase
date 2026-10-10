package com.development.base.data.repository

import com.development.base.core.common.ApiResult
import com.development.base.core.common.IoDispatcher
import com.development.base.core.common.apiCall
import com.development.base.data.local.PostDao
import com.development.base.data.local.PostEntity
import com.development.base.data.remote.PostApi
import com.development.base.domain.model.Post
import com.development.base.domain.repository.PostRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val api: PostApi, private val dao: PostDao, @IoDispatcher private val io: CoroutineDispatcher
) : PostRepository {
    override suspend fun refreshPosts(): ApiResult<Unit> = withContext(io) {
        apiCall { dao.insertAll(api.getPosts().map { PostEntity(it.id, it.title, it.body) }) }
    }
    override suspend fun posts(): List<Post> = withContext(io) { dao.getAll().map { it.toDomain() } }
    private fun PostEntity.toDomain() = Post(id, title, body, "https://picsum.photos/seed/androidbase$id/160/120")
}
