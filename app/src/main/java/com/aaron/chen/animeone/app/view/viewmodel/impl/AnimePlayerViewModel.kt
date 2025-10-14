package com.aaron.chen.animeone.app.view.viewmodel.impl

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.view.controller.PlayerController
import com.aaron.chen.animeone.app.view.viewmodel.IAnimePlayerViewModel
import com.aaron.chen.animeone.utils.MediaUtils.getMediaSource
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent

@KoinViewModel
class AnimePlayerViewModel(context: Context): ViewModel(), IAnimePlayerViewModel, KoinComponent {
    private val player = ExoPlayer.Builder(context).build().apply {
        playWhenReady = true
    }
    private var episodeList: List<AnimeEpisodeBean> = emptyList()
    override val selectedEpisode = MutableStateFlow<AnimeEpisodeBean?>(null)
    override val currentVideo = MutableStateFlow<AnimeVideoBean?>(null)
    override val isVideoBuffering = MutableStateFlow(true)
    private val playerController = PlayerController(
        player = player,
        onBufferingChanged = { isVideoBuffering.value = it },
        onEpisodeEnd = { nextEpisode() }
    )

    private fun nextEpisode() {
        val current = selectedEpisode.value
        val idx = episodeList.indexOfFirst { it.id == current?.id }
        selectedEpisode.value = if (idx != -1 && idx < episodeList.lastIndex) episodeList[idx + 1] else episodeList.first()
    }

    override fun play(video: AnimeVideoBean) {
        currentVideo.value = video
        playerController.setMediaSource(getMediaSource(video))
        playerController.prepare()
        playerController.play()
    }

    override fun getPlayer(): ExoPlayer {
        return player
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }

    override fun pause() {
        playerController.pause()
    }

    override fun updateEpisodeData(data: List<AnimeEpisodeBean>, episode: Int, playLast: Boolean) {
        episodeList = data
        val targetEpisode = if (playLast) {
            data.lastOrNull() ?: data.last()
        } else {
            data.firstOrNull { it.episode == episode } ?: data.first()
        }
        selectedEpisode.value = targetEpisode
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}