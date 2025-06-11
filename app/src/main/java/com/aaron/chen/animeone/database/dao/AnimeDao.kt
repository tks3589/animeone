package com.aaron.chen.animeone.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aaron.chen.animeone.database.entity.AnimeEntity

@Dao
interface AnimeDao {
    @Query("SELECT * FROM ${AnimeEntity.TABLE_NAME}")
    fun getAll(): PagingSource<Int, AnimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entity: List<AnimeEntity>)

    @Query("DELETE FROM ${AnimeEntity.TABLE_NAME}")
    suspend fun deleteAll(): Int
}