package com.aaron.chen.animeone.app.model.data.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeBean(
    val id: String,
    val title: String,
    val status: String,
    val year: String,
    val season: String,
    val fansub: String
) : Parcelable