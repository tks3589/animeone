package com.aaron.chen.animeone.module.retrofit

import com.aaron.chen.animeone.app.model.data.responsevo.AnimeListRespVo
import com.aaron.chen.animeone.app.model.data.responsevo.AnimeVideoRespVo
import com.aaron.chen.animeone.constant.ApiConst.HEADER_VALUE_REQUEST_TAG
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface IRetrofitApi {
    @GET
    fun getAnimeList(@Header(HEADER_VALUE_REQUEST_TAG) requestTag: String, @Url url: String): Flow<AnimeListRespVo>

    @GET
    fun getAnimeSeasonTimeLine(@Header(HEADER_VALUE_REQUEST_TAG) requestTag: String, @Url url: String): Flow<String>

    @GET
    fun getAnimeEpisodes(@Header(HEADER_VALUE_REQUEST_TAG) requestTag: String, @Url url: String): Flow<String>

    @POST
    fun requestAnimeVideo(@Header(HEADER_VALUE_REQUEST_TAG) requestTag: String, dataRaw: String): RetrofitFlow<AnimeVideoRespVo>
}