package com.development.base.data.repository

import com.development.base.domain.repository.PostRepository
import com.development.base.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
    @Binds abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository
}
