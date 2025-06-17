package com.aaron.chen.animeone.app.model.data.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeEpisodeBean(
    val id: String,
    val title: String,
    val episode: Int,
    val dataApireq: String
) : Parcelable