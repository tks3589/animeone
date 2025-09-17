package com.aaron.chen.animeone.app.view.ui.screen

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.constant.VideoConst
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@OptIn(UnstableApi::class)
@Composable
fun AnimePlayerScreen(viewModel: IAnimeoneViewModel, player: ExoPlayer, animeId: String, episode: Int) {
    val episodeLoadState = viewModel.episodeState.collectAsState(UiState.Idle)
    val commentsLoadState = viewModel.commentState.collectAsState(UiState.Idle)
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedEpisode = remember { mutableStateOf<AnimeEpisodeBean?>(null) }
    val isFullscreen = remember { mutableStateOf(false) }
    val isVideoBuffering = remember { mutableStateOf(true) }
    val imageDialogUrl = remember { mutableStateOf<String?>(null) }
    val uiMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK

    LaunchedEffect(uiMode) {
        activity?.window?.also {
            val isLight = uiMode != Configuration.UI_MODE_NIGHT_YES
            val controller = WindowCompat.getInsetsController(it, it.decorView)
            controller.isAppearanceLightStatusBars = isLight
            controller.isAppearanceLightNavigationBars = isLight
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isVideoBuffering.value = playbackState == Player.STATE_BUFFERING
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            player.release()
            if (isFullscreen.value && activity != null) {
                toggleFullscreen(activity, false)
            }
        }
    }

    LaunchedEffect(isFullscreen.value) {
        activity?.let {
            toggleFullscreen(it, isFullscreen.value)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.requestAnimeEpisodes(animeId)
    }

    LaunchedEffect(episodeLoadState.value) {
        val state = episodeLoadState.value
        if (state is UiState.Success && state.data.isNotEmpty()) {
            selectedEpisode.value = state.data.firstOrNull { ep -> ep.episode == episode } ?: state.data.first()
        }
    }

    LaunchedEffect(imageDialogUrl.value) {
        if (imageDialogUrl.value != null) {
            player.pause()
        }
    }

    // status bar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (isFullscreen.value) PaddingValues(0.dp) else WindowInsets.statusBars.asPaddingValues())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Player Section
        if (selectedEpisode.value != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isFullscreen.value) Modifier.weight(1f) else Modifier.aspectRatio(16 / 9f))
                    .background(Color.Black)
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
                    modifier = Modifier.fillMaxSize()
                )

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
                isVideoBuffering.value = true
                launch {
                    viewModel.requestAnimeVideo(episodeBean.dataApireq)
                        .catch {
                            Toast.makeText(context, "載入影片失敗：${it.message}", Toast.LENGTH_SHORT).show()
                            isVideoBuffering.value = false
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
                }
                launch {
                    //loading comments
                    viewModel.requestAnimeComments(episodeBean.id)
                }
            }

            if (!isFullscreen.value) {
                selectedEpisode.value?.let { episode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${episode.title} - 第 ${episode.episode} 話",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f) // 左邊文字占滿剩餘空間
                        )

                        IconButton(
                            onClick = {
                                val shareText = "${episode.title} - 第 ${episode.episode} 話\n${RetrofitModule.BASE_URL}${episode.id}"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "分享到"))
                            },
                            modifier = Modifier.size(20.dp)) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                ) {
                    item {
                        EpisodeSection((episodeLoadState.value as? UiState.Success), selectedEpisode)
                    }
                    item {
                        CommentSection(commentsLoadState.value, imageDialogUrl)
                    }
                }
            }
        } else if (episodeLoadState.value is UiState.Error || episodeLoadState.value is UiState.Empty) {
            ErrorText()
        } else {
           LoadingText()
        }
    }
}


@Composable
private fun EpisodeSection(state: UiState.Success<List<AnimeEpisodeBean>>?, selectedEpisode: MutableState<AnimeEpisodeBean?>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        maxItemsInEachRow = 4
    ) {
        state?.data?.forEach { episode ->
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

@Composable
private fun CommentSection(commentState: UiState<List<AnimeCommentBean>>, imageDialogUrl: MutableState<String?>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "留言板",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when (commentState) {
            is UiState.Idle,
            is UiState.Loading -> {
                Text(
                    text = stringResource(R.string.loading_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            is UiState.Empty -> {}
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
                        CommentItem(comment, imageDialogUrl)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingText() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.loading_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorText(message: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.error_text),
            color = MaterialTheme.colorScheme.error
        )
    }
}


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
fun CommentItem(comment: AnimeCommentBean, imageDialogUrl: MutableState<String?>) {
    val parts = splitMessageWithMedia(comment.message)
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
            parts.forEach { part ->
                when (part) {
                    is MessagePart.Text -> {
                        if (part.text.isNotBlank()) {
                            Text(
                                text = part.text.trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    is MessagePart.ImagePlaceholder -> {
                        val media = comment.media.getOrNull(part.mediaIndex)
                        if (media != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            var isImageLoaded = remember { mutableStateOf(false) }
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(media.url)
                                    .crossfade(true)
                                    .decoderFactory(GifDecoder.Factory())
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .heightIn(max = 300.dp)
                                    .background(Color.LightGray)
                                    .then(
                                        if (isImageLoaded.value) {
                                            Modifier.clickable {
                                                imageDialogUrl.value = media.url
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                loading = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                },
                                onSuccess = {
                                    isImageLoaded.value = true
                                },
                                error = {
                                    isImageLoaded.value = false
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("載入失敗", color = Color.White)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Text(
                text = comment.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
    if (imageDialogUrl.value != null) {
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Dialog(
            onDismissRequest = { imageDialogUrl.value = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(top = topPadding, bottom = bottomPadding)
            ) {
                // 圖片內容（置中）
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageDialogUrl.value)
                        .crossfade(true)
                        .decoderFactory(GifDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    // 關閉按鈕
                    IconButton(
                        onClick = {
                            imageDialogUrl.value = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "關閉",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun splitMessageWithMedia(message: String): List<MessagePart> {
    val imageUrlRegex = "(https?://[^\\s]+\\.(jpg|jpeg|png|gif))".toRegex(RegexOption.IGNORE_CASE)
    val parts = mutableListOf<MessagePart>()
    var lastIndex = 0
    imageUrlRegex.findAll(message).forEachIndexed { index, match ->
        val start = match.range.first
        if (start > lastIndex) {
            val text = message.substring(lastIndex, start)
            parts.add(MessagePart.Text(text))
        }
        parts.add(MessagePart.ImagePlaceholder(index))
        lastIndex = match.range.last + 1
    }
    if (lastIndex < message.length) {
        parts.add(MessagePart.Text(message.substring(lastIndex)))
    }
    return parts
}

sealed class MessagePart {
    data class Text(val text: String) : MessagePart()
    data class ImagePlaceholder(val mediaIndex: Int) : MessagePart()
}