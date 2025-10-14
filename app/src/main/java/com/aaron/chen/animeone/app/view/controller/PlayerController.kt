package com.aaron.chen.animeone.app.view.controller

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource

class PlayerController(
    private val player: ExoPlayer,
    private val onBufferingChanged: (Boolean) -> Unit,
    private val onEpisodeEnd: () -> Unit
): Player.Listener {
    init {
        player.addListener(this)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        onBufferingChanged(playbackState == Player.STATE_BUFFERING)
        if (playbackState == Player.STATE_ENDED) onEpisodeEnd()
    }
    fun prepare() = player.prepare()
    fun play() = player.play()

    @OptIn(UnstableApi::class)
    fun setMediaSource(source: MediaSource) = player.setMediaSource(source)
    fun pause() = player.pause()
    fun release() = player.removeListener(this)
}