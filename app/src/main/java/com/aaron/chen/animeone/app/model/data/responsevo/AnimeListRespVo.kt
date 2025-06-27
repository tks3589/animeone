package com.aaron.chen.animeone.app.model.data.responsevo

import com.aaron.chen.animeone.app.model.data.bean.AnimeBean
import com.google.gson.annotations.SerializedName

class AnimeListRespVo {
    @SerializedName("animes")
    var animes: List<AnimeRespVo>? = null
        get() = field ?: emptyList()

    class AnimeRespVo {

        @SerializedName("id")
        var id: String? = null
            get() = field.orEmpty()

        @SerializedName("title")
        var title: String? = null
            get() = field.orEmpty()

        @SerializedName("status")
        var status: String? = null
            get() = field.orEmpty()

        @SerializedName("year")
        var year: String? = null
            get() = field.orEmpty()

        @SerializedName("season")
        var season: String? = null
            get() = field.orEmpty()

        @SerializedName("fansub")
        var fansub: String? = null
            get() = field.orEmpty()
    }
}

fun AnimeListRespVo.toAnimeList(): List<AnimeBean> {
    return animes!!.map { animeRespVo ->
        AnimeBean(
            id = animeRespVo.id!!,
            title = animeRespVo.title!!,
            status = animeRespVo.status!!,
            year = animeRespVo.year!!,
            season = animeRespVo.season!!,
            fansub = animeRespVo.fansub!!
        )
    }
}