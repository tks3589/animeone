package com.aaron.chen.animeone.app.view.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aaron.chen.animeone.app.view.ui.screen.AnimePlayerScreen
import com.aaron.chen.animeone.app.view.ui.theme.AnimeoneTheme
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeoneViewModel
import com.aaron.chen.animeone.constant.DefaultConst
import org.koin.androidx.viewmodel.ext.android.viewModel

class AnimePlayerActivity : ComponentActivity() {
    val viewModel: IAnimeoneViewModel by viewModel<AnimeoneViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val animeId = intent.getStringExtra("animeId") ?: DefaultConst.EMPTY_STRING

        setContent {
            AnimeoneTheme {
                AnimePlayerScreen(viewModel = viewModel, animeId = animeId)
            }
        }
    }
}
