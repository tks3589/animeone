package com.aaron.chen.animeone.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aaron.chen.animeone.database.entity.AnimeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeRecordDao {
    @Query("SELECT * FROM ${AnimeRecordEntity.TABLE_NAME} ORDER BY ${AnimeRecordEntity.SESSION} DESC LIMIT 20")
    fun getAll(): Flow<List<AnimeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AnimeRecordEntity)

    @Query("DELETE FROM ${AnimeRecordEntity.TABLE_NAME}")
    suspend fun deleteAll(): Int
}