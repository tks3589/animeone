package com.aaron.chen.animeone.app.model.data.bean

import android.os.Parcelable
import com.aaron.chen.animeone.database.entity.AnimeRecordEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeRecordBean(
    val id: String,
    val title: String,
    val episode: Int,
    val session: Long
) : Parcelable

fun AnimeRecordBean.toEntity(): AnimeRecordEntity {
    return AnimeRecordEntity(
        id = id,
        title = title,
        episode = episode,
        session = session
    )
}
