package com.aaron.chen.animeone.app.view.ui.screen

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.data.bean.MediaBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextL
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextM
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextS
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextXS
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.constant.DefaultConst
import com.aaron.chen.animeone.constant.VideoConst
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@OptIn(UnstableApi::class)
@Composable
fun AnimePlayerScreen(viewModel: IAnimeoneViewModel, player: ExoPlayer, animeId: String, episode: Int, playLast: Boolean) {
    val episodeLoadState = viewModel.episodeState.collectAsState(UiState.Idle)
    val commentsLoadState = viewModel.commentState.collectAsState(UiState.Idle)
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedEpisode = remember { mutableStateOf<AnimeEpisodeBean?>(null) }
    val isFullscreen = remember { mutableStateOf(false) }
    val isVideoBuffering = remember { mutableStateOf(true) }
    val imageDialogUrl = remember { mutableStateOf<String?>(null) }
    val configuration = LocalConfiguration.current
    val uiMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val showControls = remember { mutableStateOf(false) }

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
                if (playbackState == Player.STATE_ENDED) {
                    val episodes = (episodeLoadState.value as? UiState.Success)?.data.orEmpty()
                    val current = selectedEpisode.value
                    val currentIndex = episodes.indexOfFirst { it.id == current?.id }

                    if (currentIndex != -1 && currentIndex < episodes.lastIndex) {
                        selectedEpisode.value = episodes[currentIndex + 1]
                    } else {
                        selectedEpisode.value = episodes.first()
                    }
                }
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
            val targetEpisode = if (playLast) {
                state.data.lastOrNull() ?: state.data.last()
            } else {
                state.data.firstOrNull { it.episode == episode } ?: state.data.first()
            }
            selectedEpisode.value = targetEpisode
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
                            setControllerVisibilityListener(
                                PlayerView.ControllerVisibilityListener { visibility ->
                                    showControls.value = visibility == View.VISIBLE
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (showControls.value) {
                        IconButton(
                            onClick = {
                                if (!isFullscreen.value) {
                                    activity?.finish()
                                } else {
                                    isFullscreen.value = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "返回",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedEpisode.value?.let { episode ->
                                    val shareText = "${episode.title} - ${context.resources.getString(R.string.episode_title, episode.episode)}\n${RetrofitModule.BASE_URL}${episode.id}"
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_to)))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "分享",
                                tint = Color.White,
                                modifier = Modifier.size(CommonMargin.m4)
                            )
                        }
                    }
                }

                if (isVideoBuffering.value) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            LaunchedEffect(selectedEpisode.value) {
                val episodeBean = selectedEpisode.value!!
                isVideoBuffering.value = true
                launch {
                    viewModel.requestAnimeVideo(episodeBean.dataApireq)
                        .catch {
                            Toast.makeText(context, "${context.resources.getString(R.string.error_text)}：${it.message}", Toast.LENGTH_SHORT).show()
                            isVideoBuffering.value = false
                        }
                        .collect { video ->
                            player.setMediaSource(getMediaSource(video))
                            player.prepare()
                            player.play()

                            val session = Clock.System.now().toEpochMilliseconds()
                            val title = "${episodeBean.title} ${context.resources.getString(R.string.episode_title, episodeBean.episode)}"
                            viewModel.addRecordAnime(AnimeRecordBean(id = animeId, title = title, episode = episodeBean.episode, session = session))
                        }
                }
                launch {
                    //loading comments
                    viewModel.requestAnimeComments(episodeBean.id)
                }
            }

            if (!isFullscreen.value) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                ) {
                    item {
                        selectedEpisode.value?.let { episode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = CommonMargin.m4, vertical = CommonMargin.m4),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CommonTextL(
                                    text = "${episode.title} - ${stringResource(R.string.episode_title, episode.episode)}",
                                    textAlign = TextAlign.Start,
                                    bold = true,
                                    modifier = Modifier.weight(1f) // 左邊文字占滿剩餘空間
                                )
                                Spacer(modifier = Modifier.width(CommonMargin.m4))
                                IconButton(
                                    onClick = {
                                    },
                                    modifier = Modifier.size(CommonMargin.m5)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = "收藏",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(CommonMargin.m4))
                                IconButton(
                                    onClick = {
                                    },
                                    modifier = Modifier.size(CommonMargin.m5)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.download),
                                        contentDescription = "緩存",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    item {
                        selectedEpisode.value?.let { episode ->
                            CommonTextXS(text = stringResource(R.string.update_time, episode.updateTime), modifier = Modifier.padding(start = CommonMargin.m4))
                        }
                    }
                    item {
                        EpisodeSection((episodeLoadState.value as? UiState.Success), selectedEpisode)
                    }
                    // Comment 區塊標題
                    item {
                        CommonTextM(
                            text = stringResource(R.string.comment_board),
                            modifier = Modifier.padding(
                                horizontal = CommonMargin.m4,
                                vertical = CommonMargin.m2
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Comment 狀態處理
                    when (val state = commentsLoadState.value) {
                        is UiState.Idle, is UiState.Loading -> {
                            item {
                                CommonTextS(
                                    text = stringResource(R.string.loading_text),
                                    modifier = Modifier.padding(CommonMargin.m4),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        is UiState.Empty -> {
                            item {
                                CommonTextS(
                                    text = stringResource(R.string.no_comment_list),
                                    modifier = Modifier.padding(CommonMargin.m4),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        is UiState.Error -> {
                            item {
                                CommonTextS(
                                    text = "${stringResource(R.string.error_text)}：${state.message}",
                                    modifier = Modifier.padding(CommonMargin.m4),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        is UiState.Success -> {
                            if (state.data.isEmpty()) {
                                item {
                                    CommonTextS(
                                        text = stringResource(R.string.no_comment_list),
                                        modifier = Modifier.padding(CommonMargin.m4),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // 每個留言獨立成 LazyColumn item
                                items(state.data, key = { it.id }) { comment ->
                                    Column(modifier = Modifier.padding(horizontal = CommonMargin.m4)) {
                                        CommentItem(comment, imageDialogUrl)
                                        Divider(modifier = Modifier.padding(vertical = CommonMargin.m2))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (episodeLoadState.value is UiState.Error || episodeLoadState.value is UiState.Empty) {
            ErrorText()
        } else {
           LoadingText()
        }
    }

    // comment image dialog
    imageDialogUrl.value?.let { url ->
        ImageDialog(imageDialogUrl)
    }
}

@OptIn(UnstableApi::class)
private fun getMediaSource(video: AnimeVideoBean): MediaSource {
    val videoSrc = video.src
    val fixedUrl = if (videoSrc.startsWith("//")) "https:$videoSrc" else videoSrc
    val mediaItem = MediaItem.fromUri(fixedUrl)
    val headers = mapOf(
        "Cookie" to video.cookie,
        "Referer" to RetrofitModule.BASE_URL,
        "User-Agent" to VideoConst.USER_AGENTS_LIST.random()
    )
    val dataSourceFactory = DefaultHttpDataSource.Factory().setDefaultRequestProperties(headers)
    return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
}

@Composable
private fun EpisodeSection(state: UiState.Success<List<AnimeEpisodeBean>>?, selectedEpisode: MutableState<AnimeEpisodeBean?>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CommonMargin.m2)
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
                CommonTextS(
                    text = stringResource(R.string.episode_title, episode.episode),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadingText() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(CommonMargin.m4),
        contentAlignment = Alignment.Center
    ) {
        CommonTextS(
            text = stringResource(R.string.loading_text),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorText(message: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(CommonMargin.m4),
        contentAlignment = Alignment.Center
    ) {
        CommonTextS(
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
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

@Composable
fun CommentItem(comment: AnimeCommentBean, imageDialogUrl: MutableState<String?>) {
    val parts = splitMessageWithMedia(comment.message)
    Row(modifier = Modifier.fillMaxWidth()) {
        Avatar(url = comment.user.avatar.url)
        Spacer(modifier = Modifier.width(CommonMargin.m2))
        Column(modifier = Modifier.weight(1f)) {
            CommonTextS(
                text = comment.user.name,
                color = MaterialTheme.colorScheme.primary
            )
            parts.forEach { part ->
                when (part) {
                    is MessagePart.Text -> {
                        if (part.text.isNotBlank()) {
                            CommonTextS(
                                text = part.text.trim(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(top = CommonMargin.m1)
                            )
                        }
                    }

                    is MessagePart.ImagePlaceholder -> {
                        comment.media.getOrNull(part.mediaIndex)?.let {
                            CommentImageResources(it, imageDialogUrl)
                        }
                    }
                }
            }
            CommonTextXS(
                text = comment.createdAt,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun getImageRequest(url: String): ImageRequest {
    return ImageRequest.Builder(LocalContext.current)
        .data(url)
        .crossfade(true)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .apply {
            if (url.endsWith(".gif")) {
                decoderFactory(GifDecoder.Factory())
            }
        }.build()
}

@Composable
private fun CommentImageResources(media: MediaBean, imageDialogUrl: MutableState<String?>) {
    Spacer(modifier = Modifier.height(CommonMargin.m2))
    val isImageLoaded = remember { mutableStateOf(false) }
    SubcomposeAsyncImage(
        model = getImageRequest(media.url),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .heightIn(max = 200.dp)
            .clip(RoundedCornerShape(CommonMargin.m2))
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
                    .fillMaxWidth(0.5f)
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(CommonMargin.m4), color = Color.White)
            }
        },
        onSuccess = {
            isImageLoaded.value = true
        },
        error = {
            isImageLoaded.value = false
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CommonTextXS(
                    text = stringResource(R.string.error_text),
                    color = Color.White
                )
            }
        }
    )
}

@Composable
private fun Avatar(url: String) {
    val isAvatarLoading = remember { mutableStateOf(false) }
    AsyncImage(
        model = url,
        contentDescription = "頭像",
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .placeholder(
                visible = isAvatarLoading.value,
                color = Color.LightGray,
                shape = CircleShape
            ),
        contentScale = ContentScale.Crop,
        onState = { state ->
            isAvatarLoading.value = state is AsyncImagePainter.State.Loading
        }
    )
}

@Composable
private fun ImageDialog(imageDialogUrl: MutableState<String?>) {
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
                model = getImageRequest(imageDialogUrl.value ?: DefaultConst.EMPTY_STRING),
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