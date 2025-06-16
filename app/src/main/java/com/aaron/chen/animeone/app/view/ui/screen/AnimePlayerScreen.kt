package com.aaron.chen.animeone.app.view.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel

@Composable
fun AnimePlayerScreen(viewModel: IAnimeoneViewModel, animeId: String) {
    val uiState = remember { mutableStateOf<UiState<List<AnimeEpisodeBean>>>(UiState.Loading) }
    val context = LocalContext.current

    val selectedEpisode = remember { mutableStateOf<AnimeEpisodeBean?>(null) }
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.requestAnimeEpisodes(animeId)
            .collect {
                uiState.value = it
                if (it is UiState.Success && it.data.isNotEmpty()) {
                    selectedEpisode.value = it.data.first()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Player Section
        if (selectedEpisode.value != null) {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        this.player = player
                        useController = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
            )

            LaunchedEffect(selectedEpisode.value) {
                val episode = selectedEpisode.value!!
                // ⚠️ TODO: 這邊你要自己用 `episode.apiReq` 呼叫 API 拿到 mp4 連結
                // 這邊示範用假資料
//                val videoUrl = getMp4UrlFromApiReq(episode.apiReq)
//
//                player.setMediaItem(MediaItem.fromUri(videoUrl))
//                player.prepare()
            }
        }

        when (val state = uiState.value) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "載入失敗：${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is UiState.Success -> {
                val episodes = state.data

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    maxItemsInEachRow = 4
                ) {
                    episodes.forEach { episode ->
                        val isSelected = episode == selectedEpisode.value

                        Button(
                            onClick = { selectedEpisode.value = episode },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("第 ${episode.episode} 話")
                        }
                    }
                }
            }
        }
    }
}

