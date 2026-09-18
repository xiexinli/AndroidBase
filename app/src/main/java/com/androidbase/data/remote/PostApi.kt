package com.androidbase.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface PostApi { @GET("posts") suspend fun getPosts(): List<PostDto> }
@Serializable data class PostDto(val id: Int, val title: String, val body: String)
