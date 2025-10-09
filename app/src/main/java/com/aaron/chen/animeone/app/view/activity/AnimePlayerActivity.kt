package com.aaron.chen.animeone.app.view.activity

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.media3.exoplayer.ExoPlayer
import com.aaron.chen.animeone.app.view.ui.screen.AnimePlayerScreen
import com.aaron.chen.animeone.app.view.ui.theme.AnimeoneTheme
import com.aaron.chen.animeone.constant.DefaultConst
import com.aaron.chen.animeone.constant.ExtraConst

class AnimePlayerActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val animeId = intent.getStringExtra(ExtraConst.ANIME_ID) ?: DefaultConst.EMPTY_STRING
        val episode = intent.getIntExtra(ExtraConst.EPISODE, 1)
        val playLast = intent.getBooleanExtra(ExtraConst.PLAY_LAST, false)
        player = ExoPlayer.Builder(this).build().apply {
            playWhenReady = true
        }

        setContent {
            AnimeoneTheme {
                AnimePlayerScreen(player = player, animeId = animeId, episode = episode, playLast = playLast)
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (player.isPlaying) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onStop() {
        super.onStop()

        player.pause()
    }
}
