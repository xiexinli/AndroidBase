package com.development.base.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.development.base.data.local.PostDao
import com.development.base.data.local.PostEntity

@Database(entities = [PostEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() { abstract fun postDao(): PostDao }
