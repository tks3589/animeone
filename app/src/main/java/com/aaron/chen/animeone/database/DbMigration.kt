package com.aaron.chen.animeone.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aaron.chen.animeone.database.entity.AnimeFavoriteEntity

object DbMigration {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                createTableSql(
                    AnimeFavoriteEntity.TABLE_NAME,
                    AnimeFavoriteEntity.FIELD_MAP,
                    AnimeFavoriteEntity.PRIMARY_KEYS
                )
            )
        }
    }

    private fun createTableSql(tableName: String, fieldMap: Map<String, String>, primaryKeys: Array<String>): String {
        val fields = fieldMap.entries.joinToString(",\n") { (name, type) -> "`$name` $type" }
        val primaryKey = primaryKeys.joinToString(", ") { "`$it`" }
        return """
        CREATE TABLE IF NOT EXISTS `$tableName` (
            $fields,
            PRIMARY KEY($primaryKey)
        )
        """.trimIndent()
    }
}