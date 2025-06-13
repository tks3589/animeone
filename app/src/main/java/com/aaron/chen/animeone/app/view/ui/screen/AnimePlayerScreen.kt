package com.aaron.chen.animeone.app.view.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel

@Composable
fun AnimePlayerScreen(viewModel: IAnimeoneViewModel, animeId: String) {
    val uiState = remember { mutableStateOf<UiState<List<AnimeEpisodeBean>>>(UiState.Loading) }

    LaunchedEffect(Unit) {
        viewModel.requestAnimeEpisodes(animeId)
            .collect { uiState.value = it }
    }

    when (val state = uiState.value) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "載入失敗：${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            val episodes = state.data
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "共 ${episodes.size} 集", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
