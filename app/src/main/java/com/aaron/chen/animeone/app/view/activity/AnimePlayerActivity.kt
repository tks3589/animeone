package com.aaron.chen.animeone.app.view.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aaron.chen.animeone.app.view.ui.screen.AnimePlayerScreen
import com.aaron.chen.animeone.app.view.ui.theme.AnimeoneTheme

class AnimePlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val animeId = intent.getStringExtra("animeId") ?: ""

        setContent {
            AnimeoneTheme {
                AnimePlayerScreen(animeId = animeId)
            }
        }
    }
}
