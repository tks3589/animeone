package com.aaron.chen.animeone.app.view.ui.screen

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.constant.VideoConst
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import kotlinx.coroutines.flow.catch
import kotlinx.datetime.Clock

@OptIn(UnstableApi::class)
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

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(WindowInsets.statusBars.asPaddingValues())) {
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
                val episodeBean = selectedEpisode.value!!
                viewModel.requestAnimeVideo(episodeBean.dataApireq)
                    .catch {
                        Toast.makeText(context, "載入影片失敗：${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    .collect { video ->
                        val videoSrc = video.src
                        val fixedUrl = if (videoSrc.startsWith("//")) "https:$videoSrc" else videoSrc
                        val mediaItem = MediaItem.fromUri(fixedUrl)
                        val headers = mapOf(
                            "Cookie" to video.cookie,
                            "Referer" to RetrofitModule.BASE_URL,
                            "User-Agent" to VideoConst.USER_AGENTS_LIST.random()
                        )
                        val dataSourceFactory = DefaultHttpDataSource.Factory()
                            .setDefaultRequestProperties(headers)
                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                        player.setMediaSource(mediaSource)
                        player.prepare()
                        player.play()

                        val session = Clock.System.now().toEpochMilliseconds()
                        val title = "${episodeBean.title} 第 ${episodeBean.episode} 話"
                        viewModel.addRecordAnime(AnimeRecordBean(id = episodeBean.id, title = title, episode = episodeBean.episode, session = session))
                    }
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
                selectedEpisode.value?.let { episode ->
                    Text(
                        text = "${episode.title} - 第 ${episode.episode} 話",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
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

