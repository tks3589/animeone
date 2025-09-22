package com.aaron.chen.animeone.app.model.data.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeVideoBean(
    val src: String,
    val cookie: String,
    val type: String
) : Parcelable