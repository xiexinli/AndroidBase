package com.androidbase.data.repository

import com.androidbase.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule { @Binds abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository }
