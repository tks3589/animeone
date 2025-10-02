package com.aaron.chen.animeone.app.model.data.bean

import android.graphics.Bitmap

data class AnimeDownloadBean(
    val name: String,
    val path: String,
    val preview: Bitmap?
)