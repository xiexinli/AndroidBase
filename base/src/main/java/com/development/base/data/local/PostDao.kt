package com.development.base.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id") suspend fun getAll(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1") suspend fun getById(id: Int): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(posts: List<PostEntity>)

    @Query("DELETE FROM posts") suspend fun clear()
}
