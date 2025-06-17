package com.aaron.chen.animeone.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.database.DbConst.NON_NULL_INTEGER
import com.aaron.chen.animeone.database.DbConst.NON_NULL_TEXT

@Entity(tableName = AnimeRecordEntity.TABLE_NAME)
data class AnimeRecordEntity(
    @PrimaryKey @ColumnInfo(name = ID)
    val id: String,
    @ColumnInfo(name = TITLE) val title: String,
    @ColumnInfo(name = EPISODE) val episode: Int,
    @ColumnInfo(name = SESSION) val session: Long
) {
    companion object {
        const val TABLE_NAME = "AnimeRecordTable"

        const val ID = "id"
        const val TITLE = "title"
        const val EPISODE = "episode"
        const val SESSION = "session"

        val FIELD_MAP = hashMapOf(
            ID to NON_NULL_TEXT,
            TITLE to NON_NULL_TEXT,
            EPISODE to NON_NULL_INTEGER,
            SESSION to NON_NULL_INTEGER
        )

        val PRIMARY_KEYS = arrayOf(ID)
    }
}

fun List<AnimeRecordEntity>.toBean() = map {
    AnimeRecordBean(
        id = it.id,
        title = it.title,
        episode = it.episode,
        session = it.session
    )
}