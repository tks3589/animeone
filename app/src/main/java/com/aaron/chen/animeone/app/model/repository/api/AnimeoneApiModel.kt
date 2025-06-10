package com.aaron.chen.animeone.app.model.repository.api

import com.aaron.chen.animeone.app.model.repository.api.impl.IAnimeoneApiModel
import com.aaron.chen.animeone.app.model.data.bean.AnimeBean
import com.aaron.chen.animeone.app.model.data.responsevo.toAnimeList
import com.aaron.chen.animeone.module.retrofit.IEtRetrofitApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AnimeoneApiModel: IAnimeoneApiModel, KoinComponent {
    private val apiModel: IEtRetrofitApi by inject()
    private val requestTag = ""
    private val url = "https://d1zquzjgwo9yb.cloudfront.net/"

    override fun getAnimeList(): Flow<List<AnimeBean>> {
        return apiModel.getAnimeList(requestTag, url).map { vo ->
            vo.toAnimeList()
        }
    }
}