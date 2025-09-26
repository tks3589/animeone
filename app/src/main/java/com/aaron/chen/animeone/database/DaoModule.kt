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
        const val DB_VERSION = 2
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
            DbMigration.MIGRATION_1_2
        )
    }

    @Factory fun animeDao(etDataBase: DataBase) = etDataBase.animeListDao()
    @Factory fun animeRecordDao(etDataBase: DataBase) = etDataBase.animeRecordDao()
    @Factory fun animeFavoriteDao(etDataBase: DataBase) = etDataBase.animeFavoriteDao()
}