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
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeoneViewModel
import com.aaron.chen.animeone.constant.DefaultConst
import org.koin.androidx.viewmodel.ext.android.viewModel

class AnimePlayerActivity : ComponentActivity() {
    private val viewModel: IAnimeoneViewModel by viewModel<AnimeoneViewModel>()
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val animeId = intent.getStringExtra("animeId") ?: DefaultConst.EMPTY_STRING
        val episode = intent.getIntExtra("episode", 1)
        player = ExoPlayer.Builder(this).build().apply {
            playWhenReady = true
        }

        setContent {
            AnimeoneTheme {
                AnimePlayerScreen(viewModel = viewModel, player = player, animeId = animeId, episode = episode)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
