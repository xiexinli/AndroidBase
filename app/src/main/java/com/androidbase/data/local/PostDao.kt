package com.androidbase.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id") suspend fun getAll(): List<PostEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(posts: List<PostEntity>)
}
