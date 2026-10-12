package com.development.base.domain.repository

import com.development.base.core.common.ApiResult
import com.development.base.domain.model.Post

interface PostRepository {
    /**
     * 拉取第 [page] 页（从 1 开始）并写入本地缓存。
     *
     * 第 1 页会先清空旧缓存，保证刷新后的列表与远端一致。
     */
    suspend fun loadPage(page: Int, pageSize: Int): ApiResult<List<Post>>

    /** 本地缓存中的全部帖子，用于网络失败时回退展示。 */
    suspend fun cachedPosts(): List<Post>

    /** 按 id 读取本地缓存中的帖子，供详情页使用。 */
    suspend fun post(id: Int): Post?
}
