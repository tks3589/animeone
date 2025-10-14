package com.aaron.chen.animeone.app.view.viewmodel

import androidx.media3.exoplayer.ExoPlayer
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface IAnimePlayerViewModel {
    val selectedEpisode: MutableStateFlow<AnimeEpisodeBean?>
    val currentVideo: StateFlow<AnimeVideoBean?>
    val isVideoBuffering: MutableStateFlow<Boolean>
    fun play(video: AnimeVideoBean)
    fun getPlayer(): ExoPlayer
    fun isPlaying(): Boolean
    fun pause()
    fun updateEpisodeData(data: List<AnimeEpisodeBean>, episode: Int, playLast: Boolean)
}