package com.aaron.chen.animeone.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aaron.chen.animeone.database.DbConst.NON_NULL_TEXT

@Entity(tableName = AnimeEntity.TABLE_NAME)
data class AnimeEntity(
    @PrimaryKey @ColumnInfo(name = ID)
    val id: String,
    @ColumnInfo(name = TITLE) val title: String,
    @ColumnInfo(name = STATUS) val status: String,
    @ColumnInfo(name = YEAR) val year: String,
    @ColumnInfo(name = SEASON) val season: String,
    @ColumnInfo(name = FANSUB) val fansub: String
) {
    companion object {
        const val TABLE_NAME = "AnimeTable"

        const val ID = "id"
        const val TITLE = "title"
        const val STATUS = "status"
        const val YEAR = "year"
        const val SEASON = "season"
        const val FANSUB = "fansub"

        val FIELD_MAP = hashMapOf(
            ID to NON_NULL_TEXT,
            TITLE to NON_NULL_TEXT,
            STATUS to NON_NULL_TEXT,
            YEAR to NON_NULL_TEXT,
            SEASON to NON_NULL_TEXT,
            FANSUB to NON_NULL_TEXT
        )

        val PRIMARY_KEYS = arrayOf(ID)
    }
}