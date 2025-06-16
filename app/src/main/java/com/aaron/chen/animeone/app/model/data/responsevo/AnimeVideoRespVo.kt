package com.aaron.chen.animeone.app.model.data.responsevo

import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.constant.DefaultConst
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

fun AnimeVideoRespVo.toVideo(cookie: String): AnimeVideoBean {
    return AnimeVideoBean(
        src = source?.firstOrNull()?.src ?: DefaultConst.EMPTY_STRING,
        cookie = cookie,
        type = source?.firstOrNull()?.type ?: DefaultConst.EMPTY_STRING
    )
}