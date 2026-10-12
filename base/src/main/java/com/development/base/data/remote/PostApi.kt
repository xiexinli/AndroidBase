package com.development.base.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface PostApi {
    /**
     * 分页拉取帖子。
     *
     * jsonplaceholder 支持 `_page` / `_limit` 分页，样例数据共 100 条。
     *
     * @param page 页码，从 1 开始。
     * @param limit 每页条数。
     */
    @GET("posts")
    suspend fun getPosts(@Query("_page") page: Int, @Query("_limit") limit: Int): List<PostDto>
}

@Serializable data class PostDto(val id: Int, val title: String, val body: String)
