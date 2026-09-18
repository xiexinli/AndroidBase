package com.androidbase.domain.repository

import com.androidbase.core.common.ApiResult
import com.androidbase.domain.model.Post

interface PostRepository { suspend fun refreshPosts(): ApiResult<Unit>; suspend fun posts(): List<Post> }
