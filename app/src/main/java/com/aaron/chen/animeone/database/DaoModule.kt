package com.aaron.chen.animeone.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DaoModule {
    companion object {
        const val DB_VERSION = 1
        private const val DB_NAME = "animeone.db"
    }

    @Single
    fun getInstance(applicationContext: Context): DataBase {
        return Room.databaseBuilder(applicationContext, DataBase::class.java, DB_NAME)
            .addMigrations(*getMigrations())
            .build()
    }

    private fun getMigrations(): Array<Migration> {
        return arrayOf(
        )
    }

    @Factory fun animeDao(etDataBase: DataBase) = etDataBase.animeDao()
}