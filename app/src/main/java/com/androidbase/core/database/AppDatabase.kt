package com.androidbase.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidbase.data.local.PostDao
import com.androidbase.data.local.PostEntity

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() { abstract fun postDao(): PostDao }
