package com.androidbase.core.database

import android.content.Context
import androidx.room.Room
import com.androidbase.data.local.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "android_base.db").build()
    @Provides fun postDao(database: AppDatabase): PostDao = database.postDao()
}
