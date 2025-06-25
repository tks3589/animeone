package com.aaron.chen.animeone.app.view.ui.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.constant.VideoConst
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import kotlinx.coroutines.flow.catch
import kotlinx.datetime.Clock

@OptIn(UnstableApi::class, ExperimentalLayoutApi::class)
@Composable
fun AnimePlayerScreen(viewModel: IAnimeoneViewModel, animeId: String, episode: Int) {
    val episodeLoadState = remember { mutableStateOf<UiState<List<AnimeEpisodeBean>>>(UiState.Loading) }
    val commentsLoadState = remember { mutableStateOf<UiState<List<AnimeCommentBean>>>(UiState.Loading) }
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val selectedEpisode = remember { mutableStateOf<AnimeEpisodeBean?>(null) }
    val isFullscreen = remember { mutableStateOf(false) }
    val wasPlayingBeforePause = remember { mutableStateOf(false) }
    val isVideoBuffering = remember { mutableStateOf(true) } // 新增：影片緩衝狀態

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            // 新增：監聽播放器狀態
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isVideoBuffering.value = playbackState == Player.STATE_BUFFERING
                }
            })
        }
    }

    // --- 新增的保持螢幕開啟邏輯 ---
    val currentActivity = context as? ComponentActivity
    DisposableEffect(currentActivity) { // 當 Activity 改變時重新執行
        currentActivity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            currentActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    // --- 保持螢幕開啟邏輯結束 ---

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (player.isPlaying) {
                        wasPlayingBeforePause.value = true
                        player.pause()
                    } else {
                        wasPlayingBeforePause.value = false
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
//                    if (wasPlayingBeforePause.value) {
//                        player.play()
//                    }
                }
                else -> { /* 其他生命週期事件無需處理 */ }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            player.release()
            if (isFullscreen.value && activity != null) {
                toggleFullscreen(activity, false)
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isFullscreen.value) {
        activity?.let {
            toggleFullscreen(it, isFullscreen.value)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.requestAnimeEpisodes(animeId)
            .collect {
                episodeLoadState.value = it
                if (it is UiState.Success && it.data.isNotEmpty()) {
                    selectedEpisode.value = it.data.firstOrNull { ep -> ep.episode == episode }
                        ?: it.data.first()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isFullscreen.value) PaddingValues(0.dp) else WindowInsets.statusBars.asPaddingValues())
    ) {
        // Player Section
        if (selectedEpisode.value != null) {
            Box( // 使用 Box 來疊加 PlayerView 和 Loading 指示器
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isFullscreen.value) Modifier.weight(1f) else Modifier.aspectRatio(16 / 9f))
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = true
                            setFullscreenButtonClickListener {
                                isFullscreen.value = !isFullscreen.value
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize() // 讓 PlayerView 填滿 Box
                )

                // 影片緩衝時顯示 Loading Progress
                if (isVideoBuffering.value) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }


            LaunchedEffect(selectedEpisode.value) {
                val episodeBean = selectedEpisode.value!!
                // 重置緩衝狀態，因為要載入新影片了
                isVideoBuffering.value = true
                viewModel.requestAnimeVideo(episodeBean.dataApireq)
                    .catch {
                        Toast.makeText(context, "載入影片失敗：${it.message}", Toast.LENGTH_SHORT).show()
                        isVideoBuffering.value = false // 載入失敗也停止緩衝指示
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
                        viewModel.addRecordAnime(AnimeRecordBean(id = animeId, title = title, episode = episodeBean.episode, session = session))
                    }

                //loading comments
                viewModel.requestAnimeComments(episodeBean.id)
                    .collect {
                        commentsLoadState.value = it
                    }
            }
        }

        if (!isFullscreen.value) {
            selectedEpisode.value?.let { episode ->
                Text(
                    text = "${episode.title} - 第 ${episode.episode} 話",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 集數按鈕區塊
                item {
                    when (val state = episodeLoadState.value) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.loading_text),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        is UiState.Error -> {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${stringResource(R.string.error_text)}：${state.message}",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        is UiState.Success -> {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                maxItemsInEachRow = 4
                            ) {
                                state.data.forEach { episode ->
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
                if (episodeLoadState.value is UiState.Success) {
                    // 留言區塊
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "留言板",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            when (val commentState = commentsLoadState.value) {
                                is UiState.Loading -> {
                                    Text(
                                        text = stringResource(R.string.loading_text),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                is UiState.Error -> {
                                    Text(
                                        text = "${stringResource(R.string.error_text)}：${commentState.message}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                is UiState.Success -> {
                                    if (commentState.data.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.no_comment_list),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        commentState.data.forEach { comment ->
                                            CommentItem(comment)
                                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 輔助函數：切換全螢幕模式
@OptIn(UnstableApi::class)
private fun toggleFullscreen(activity: Activity, isFullscreen: Boolean) {
    val window = activity.window
    val decorView = window.decorView
    val controller = WindowCompat.getInsetsController(window, decorView)

    if (isFullscreen) {
        // 進入全螢幕
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    } else {
        // 退出全螢幕
        controller.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}

@Composable
fun CommentItem(comment: AnimeCommentBean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = comment.user.avatar.url,
            contentDescription = "頭像",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.user.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = comment.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = comment.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

