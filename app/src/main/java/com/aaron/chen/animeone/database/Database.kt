package com.aaron.chen.animeone.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aaron.chen.animeone.database.dao.AnimeDao
import com.aaron.chen.animeone.database.entity.AnimeEntity
import com.aaron.chen.animeone.database.DaoModule.Companion.DB_VERSION

@Database(
    entities = [
        AnimeEntity::class
    ],
    version = DB_VERSION,
)
abstract class DataBase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}