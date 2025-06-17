package com.aaron.chen.animeone.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aaron.chen.animeone.database.dao.AnimeListDao
import com.aaron.chen.animeone.database.entity.AnimeEntity
import com.aaron.chen.animeone.database.DaoModule.Companion.DB_VERSION
import com.aaron.chen.animeone.database.dao.AnimeRecordDao
import com.aaron.chen.animeone.database.entity.AnimeRecordEntity

@Database(
    entities = [
        AnimeEntity::class,
        AnimeRecordEntity::class
    ],
    version = DB_VERSION,
)
abstract class DataBase : RoomDatabase() {
    abstract fun animeListDao(): AnimeListDao
    abstract fun animeRecordDao(): AnimeRecordDao
}