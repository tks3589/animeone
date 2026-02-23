package com.aaron.chen.animeone.app.model.repository.api.impl

import com.aaron.chen.animeone.app.model.data.bean.AnimeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import kotlinx.coroutines.flow.Flow

interface IAnimeoneApiModel {
    fun getAnimeList(): Flow<List<AnimeBean>>
    fun getAnimeSeasonTimeLine(): Flow<AnimeSeasonTimeLineBean>
    fun getAnimeEpisodes(animeId: String): Flow<List<AnimeEpisodeBean>>
    fun requestAnimeVideo(dataRaw: String): Flow<AnimeVideoBean>
    fun requestComments(animeId: String): Flow<List<AnimeCommentBean>>
}