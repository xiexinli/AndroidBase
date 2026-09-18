package com.androidbase.data.repository

import com.androidbase.core.common.ApiResult
import com.androidbase.core.common.IoDispatcher
import com.androidbase.core.common.apiCall
import com.androidbase.data.local.PostDao
import com.androidbase.data.local.PostEntity
import com.androidbase.data.remote.PostApi
import com.androidbase.domain.model.Post
import com.androidbase.domain.repository.PostRepository
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
