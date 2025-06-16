package com.aaron.chen.animeone.app.model.data.responsevo

import com.google.gson.annotations.SerializedName

class AnimeVideoRespVo {
    @SerializedName("s")
    val source: List<AnimeSrcVo>? = null
        get() = field ?: emptyList()

    class AnimeSrcVo {
        @SerializedName("src")
        var src: String? = null
            get() = field.orEmpty()

        @SerializedName("type")
        var type: String? = null
            get() = field.orEmpty()
    }
}