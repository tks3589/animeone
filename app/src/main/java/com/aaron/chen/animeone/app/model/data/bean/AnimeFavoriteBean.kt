package com.aaron.chen.animeone.app.model.data.bean

import android.os.Parcelable
import com.aaron.chen.animeone.database.entity.AnimeFavoriteEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeFavoriteBean(
    val id: String,
    val title: String,
    val episode: Int,
    val session: Long
) : Parcelable

fun AnimeFavoriteBean.toEntity(): AnimeFavoriteEntity {
    return AnimeFavoriteEntity(
        id = id,
        title = title,
        episode = episode,
        session = session
    )
}