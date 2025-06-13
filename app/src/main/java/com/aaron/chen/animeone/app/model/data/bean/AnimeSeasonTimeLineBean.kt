package com.aaron.chen.animeone.app.model.data.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeSeasonTimeLineBean(
    val seasonTitle: String,
    val timeLine: List<AnimeLiteBean>
) : Parcelable

@Parcelize
data class AnimeLiteBean(
    val id: String,
    val day: String,
    val title: String
) : Parcelable