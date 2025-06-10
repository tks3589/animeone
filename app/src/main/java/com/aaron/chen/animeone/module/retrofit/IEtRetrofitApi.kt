package com.aaron.chen.animeone.module.retrofit

import com.aaron.chen.animeone.constant.ApiConst.HEADER_VALUE_REQUEST_TAG
import com.aaron.chen.animeone.app.model.data.responsevo.AnimeListRespVo
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface IEtRetrofitApi {
    @GET
    fun getAnimeList(@Header(HEADER_VALUE_REQUEST_TAG) requestTag: String, @Url url: String): Flow<AnimeListRespVo>
}