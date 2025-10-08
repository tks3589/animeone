package com.aaron.chen.animeone.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aaron.chen.animeone.database.entity.AnimeFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeFavoriteDao {
    @Query("SELECT * FROM ${AnimeFavoriteEntity.TABLE_NAME} ORDER BY ${AnimeFavoriteEntity.SESSION} DESC")
    fun getAll(): Flow<List<AnimeFavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AnimeFavoriteEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM ${AnimeFavoriteEntity.TABLE_NAME} WHERE ${AnimeFavoriteEntity.ID} = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Query("DELETE FROM ${AnimeFavoriteEntity.TABLE_NAME} WHERE ${AnimeFavoriteEntity.ID} = :id")
    suspend fun deleteFromId(id: String): Int

    @Query("DELETE FROM ${AnimeFavoriteEntity.TABLE_NAME}")
    suspend fun deleteAll(): Int

}