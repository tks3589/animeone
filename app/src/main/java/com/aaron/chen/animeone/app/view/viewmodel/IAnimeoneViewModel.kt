package com.aaron.chen.animeone.app.view.viewmodel

import androidx.paging.PagingData
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IAnimeoneViewModel {
    val timeLineState: StateFlow<UiState<AnimeSeasonTimeLineBean>>
    val episodeState: StateFlow<UiState<List<AnimeEpisodeBean>>>
    val commentState: StateFlow<UiState<List<AnimeCommentBean>>>

    fun requestAnimeList(): Flow<PagingData<AnimeEntity>>
    fun requestAnimeSeasonTimeLine()
    fun requestAnimeEpisodes(animeId: String)
    fun requestAnimeVideo(dataRaw: String): Flow<AnimeVideoBean>
    fun requestAnimeComments(animeId: String, next: String? = null, initial: Boolean = false)
}