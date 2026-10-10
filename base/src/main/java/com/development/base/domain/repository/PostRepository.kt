package com.development.base.domain.repository

import com.development.base.core.common.ApiResult
import com.development.base.domain.model.Post

interface PostRepository { suspend fun refreshPosts(): ApiResult<Unit>; suspend fun posts(): List<Post> }
