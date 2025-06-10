package com.aaron.chen.animeone.app.model.repository.api.impl

import com.aaron.chen.animeone.app.model.data.bean.AnimeBean
import kotlinx.coroutines.flow.Flow

interface IAnimeoneApiModel {
    fun getAnimeList(): Flow<List<AnimeBean>>
}