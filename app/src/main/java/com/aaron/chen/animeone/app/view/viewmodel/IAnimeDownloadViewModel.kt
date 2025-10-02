package com.aaron.chen.animeone.app.view.viewmodel

import com.aaron.chen.animeone.app.model.data.bean.AnimeDownloadBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.state.UiState
import kotlinx.coroutines.flow.StateFlow

interface IAnimeDownloadViewModel {
    val loadVideoState: StateFlow<UiState<List<AnimeDownloadBean>>>

    fun download(url: String, headers: HashMap<String, String> = hashMapOf(), episodeBean: AnimeEpisodeBean? = null)
    fun loadDownloadedVideos()
}