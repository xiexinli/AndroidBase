package com.development.base.data.repository

import com.development.base.core.common.ApiResult
import com.development.base.core.common.IoDispatcher
import com.development.base.core.common.apiCall
import com.development.base.data.local.PostDao
import com.development.base.data.local.PostEntity
import com.development.base.data.remote.PostApi
import com.development.base.data.remote.PostDto
import com.development.base.domain.model.Post
import com.development.base.domain.repository.PostRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val FirstPage = 1

class PostRepositoryImpl @Inject constructor(
    private val api: PostApi, private val dao: PostDao, @IoDispatcher private val io: CoroutineDispatcher
) : PostRepository {
    override suspend fun loadPage(page: Int, pageSize: Int): ApiResult<List<Post>> = withContext(io) {
        apiCall {
            val dtos = api.getPosts(page, pageSize)
            if (page == FirstPage) dao.clear()
            dao.insertAll(dtos.map { PostEntity(it.id, it.title, it.body) })
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun cachedPosts(): List<Post> = withContext(io) { dao.getAll().map { it.toDomain() } }

    override suspend fun post(id: Int): Post? = withContext(io) { dao.getById(id)?.toDomain() }

    private fun PostEntity.toDomain() = Post(id, title, body, imageUrlOf(id))

    private fun PostDto.toDomain() = Post(id, title, body, imageUrlOf(id))

    private fun imageUrlOf(id: Int) = "https://picsum.photos/seed/androidbase$id/160/120"
}
