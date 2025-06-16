package com.aaron.chen.animeone.app.view.viewmodel

import androidx.paging.PagingData
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow

interface IAnimeoneViewModel {
    fun requestAnimes(): Flow<PagingData<AnimeEntity>>
    fun requestAnimeSeasonTimeLine(): Flow<UiState<AnimeSeasonTimeLineBean>>
    fun requestAnimeEpisodes(animeId: String): Flow<UiState<List<AnimeEpisodeBean>>>
    fun requestAnimeVideo(dataRaw: String): Flow<AnimeVideoBean>
}