package com.aaron.chen.animeone.app.model.data.responsevo

import com.aaron.chen.animeone.app.model.data.bean.AnimeLiteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.google.gson.annotations.SerializedName

class AnimeSeasonTimeLineRespVo {
    @SerializedName("title")
    var seasonTitle: String? = null
        get() = field.orEmpty()

    @SerializedName("timeline")
    var timeline: List<AnimeLiteRespVo>? = null
        get() = field ?: emptyList()

    class AnimeLiteRespVo {

        @SerializedName("id")
        var id: String? = null
            get() = field.orEmpty()

        @SerializedName("day")
        var day: String? = null
            get() = field.orEmpty()

        @SerializedName("title")
        var title: String? = null
            get() = field.orEmpty()
    }
}

fun AnimeSeasonTimeLineRespVo.toTimeLine(): AnimeSeasonTimeLineBean {
    return AnimeSeasonTimeLineBean(
        seasonTitle = seasonTitle!!,
        timeLine = timeline!!.map { animeLiteRespVo ->
            AnimeLiteBean(
                id = animeLiteRespVo.id!!,
                day = animeLiteRespVo.day!!,
                title = animeLiteRespVo.title!!
            )
        }
    )
}