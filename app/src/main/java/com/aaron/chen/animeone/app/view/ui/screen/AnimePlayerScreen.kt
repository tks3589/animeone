package com.aaron.chen.animeone.app.view.ui.screen

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeFavoriteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.data.bean.MediaBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextL
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextM
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextS
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextXS
import com.aaron.chen.animeone.app.view.viewmodel.IAnimePlayerViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeDownloadViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeStorageViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeoneViewModel
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import com.aaron.chen.animeone.utils.MediaUtils.getImageRequest
import com.aaron.chen.animeone.utils.MediaUtils.getVideoHeaders
import com.aaron.chen.animeone.utils.MediaUtils.getVideoSrc
import com.aaron.chen.animeone.utils.MediaUtils.splitMessageWithMedia
import com.aaron.chen.animeone.utils.MessagePart
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(UnstableApi::class)
@Composable
fun AnimePlayerScreen(playerViewModel: IAnimePlayerViewModel, animeId: String, episode: Int, playLast: Boolean) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val animeoneViewModel = koinViewModel<AnimeoneViewModel>()
    val storageViewModel = koinViewModel<AnimeStorageViewModel>()
    val downloadViewModel = koinViewModel<AnimeDownloadViewModel>()
    val episodeLoadState = animeoneViewModel.episodeState.collectAsStateWithLifecycle(UiState.Idle)
    val commentsLoadState = animeoneViewModel.commentState.collectAsStateWithLifecycle(UiState.Idle)
    val favoriteBookState = storageViewModel.bookState.collectAsStateWithLifecycle(UiState.Idle)
    val selectedEpisodeState = playerViewModel.selectedEpisode.collectAsStateWithLifecycle()
    val currentVideoState = playerViewModel.currentVideo.collectAsStateWithLifecycle()
    val isVideoBufferingState = playerViewModel.isVideoBuffering.collectAsStateWithLifecycle()
    val isFullscreen = remember { mutableStateOf(false) }
    val imageDialogUrl = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        activity?.window?.let {
            val controller = WindowCompat.getInsetsController(it, it.decorView)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
        animeoneViewModel.requestAnimeEpisodes(animeId)
        storageViewModel.requestBookState(animeId)
    }

    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
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

    LaunchedEffect(episodeLoadState.value) {
        val state = episodeLoadState.value
        if (state is UiState.Success && state.data.isNotEmpty()) {
            playerViewModel.updateEpisodeData(data = state.data, episode = episode, playLast = playLast)
        }
    }

    LaunchedEffect(imageDialogUrl.value) {
        if (imageDialogUrl.value != null) {
            playerViewModel.pause()
        }
    }

    LaunchedEffect(selectedEpisodeState.value) {
        val episodeBean = selectedEpisodeState.value
        playerViewModel.isVideoBuffering.value = true
        episodeBean?.let {
            launch {
                animeoneViewModel.requestAnimeVideo(it.dataApireq)
                    .catch { error ->
                        Toast.makeText(context, "${context.resources.getString(R.string.error_text)}：${error.message}", Toast.LENGTH_SHORT).show()
                        playerViewModel.isVideoBuffering.value = false
                    }
                    .collect { video ->
                        playerViewModel.play(video)

                        val session = Clock.System.now().toEpochMilliseconds()
                        val title = "${it.title} ${context.resources.getString(R.string.episode_title, it.episode)}"
                        storageViewModel.addRecordAnime(AnimeRecordBean(id = animeId, title = title, episode = it.episode, session = session))
                    }
            }
            launch {
                //loading comments
                animeoneViewModel.requestAnimeComments(it.id)
            }
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
        if (selectedEpisodeState.value != null) {
            PlayerSection(
                player = playerViewModel.getPlayer(),
                isFullscreen = isFullscreen.value,
                onToggleFullscreen = { isFullscreen.value = !isFullscreen.value },
                onExit = {
                    if (!isFullscreen.value) {
                        activity?.finish()
                    } else {
                        isFullscreen.value = false
                    }
                },
                selectedEpisode = selectedEpisodeState.value,
                onShare = { shareText ->
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_to)))
                },
                isVideoBuffering = isVideoBufferingState.value
            )
            if (!isFullscreen.value) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                ) {
                    item {
                        AnimeTitleSection(
                            episode = selectedEpisodeState.value,
                            video = currentVideoState.value,
                            isFavorite = (favoriteBookState.value as? UiState.Success)?.data ?: false,
                            onDownload = { video, episode ->
                                downloadViewModel.download(
                                    url = getVideoSrc(video),
                                    headers = getVideoHeaders(video.cookie),
                                    episodeBean = episode
                                )
                            },
                            onToggleFavorite = { episode, isFavorite ->
                                coroutineScope.launch {
                                    if (isFavorite) {
                                        storageViewModel.unbookAnime(animeId)
                                        Toast.makeText(context, context.getString(R.string.favorite_remove), Toast.LENGTH_SHORT).show()
                                    } else {
                                        storageViewModel.bookAnime(
                                            AnimeFavoriteBean(
                                                id = animeId,
                                                title = episode.title,
                                                episode = episode.episode,
                                                session = Clock.System.now().toEpochMilliseconds()
                                            )
                                        )
                                        Toast.makeText(context, context.getString(R.string.favorite_success), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                    item {
                        selectedEpisodeState.value?.let { episode ->
                            CommonTextXS(text = stringResource(R.string.update_time, episode.updateTime), modifier = Modifier.padding(start = CommonMargin.m4))
                        }
                    }
                    item {
                        EpisodeSection(
                            episodes = (episodeLoadState.value as? UiState.Success)?.data.orEmpty(),
                            selectedEpisode = selectedEpisodeState.value,
                            onSelectEpisode = { playerViewModel.selectedEpisode.value = it }
                        )
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
                            items(state.data, key = { it.id }) { comment ->
                                Column(modifier = Modifier.padding(horizontal = CommonMargin.m4)) {
                                    CommentItem(comment) {
                                        imageDialogUrl.value = it
                                    }
                                    Divider(modifier = Modifier.padding(vertical = CommonMargin.m2))
                                }
                            }
                        }
                    }
                }
            }
        } else if (episodeLoadState.value is UiState.Error) {
            ErrorText((episodeLoadState.value as UiState.Error).message)
        } else if (episodeLoadState.value is UiState.Empty) {
            ErrorText(stringResource(R.string.episode_empty))
        } else {
           LoadingText()
        }
    }

    // comment image dialog
    imageDialogUrl.value?.let { url ->
        ImageDialog(
            imageUrl = url,
            onDismiss = { imageDialogUrl.value = null },
            onDownload = {
                downloadViewModel.download(url)
            }
        )
    }
}

@Composable
private fun PlayerSection(
    player: ExoPlayer,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onExit: () -> Unit,
    selectedEpisode: AnimeEpisodeBean?,
    onShare: (String) -> Unit,
    isVideoBuffering: Boolean
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val isWideMode = isFullscreen || isLandscape
    val showControls = remember { mutableStateOf(false) }
    Box(
        modifier = if (isWideMode) {
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .background(Color.Black)
        }
    ){
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    this.player = player
                    useController = true
                    setControllerVisibilityListener(
                        PlayerView.ControllerVisibilityListener { visibility ->
                            showControls.value = visibility == View.VISIBLE
                        }
                    )
                    setFullscreenButtonClickListener { onToggleFullscreen() }
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
                    onClick = { onExit() }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        selectedEpisode?.let { episode ->
                            val shareText = "${episode.title} - ${context.resources.getString(R.string.episode_title, episode.episode)}\n${RetrofitModule.BASE_URL}${episode.id}"
                            onShare(shareText)
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

        if (isVideoBuffering) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun AnimeTitleSection(
    episode: AnimeEpisodeBean?,
    video: AnimeVideoBean?,
    isFavorite: Boolean,
    onDownload: (AnimeVideoBean, AnimeEpisodeBean) -> Unit,
    onToggleFavorite: (AnimeEpisodeBean, Boolean) -> Unit
) {
    episode?.let {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CommonMargin.m4,
                    vertical = CommonMargin.m4
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CommonTextL(
                text = "${it.title} - ${stringResource(R.string.episode_title, it.episode)}",
                textAlign = TextAlign.Start,
                bold = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(CommonMargin.m5))
            IconButton(
                onClick = {
                    video?.let { v -> onDownload(v, it) }
                },
                modifier = Modifier.size(CommonMargin.m5)
            ) {
                Icon(
                    painter = painterResource(R.drawable.download),
                    contentDescription = "緩存",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(CommonMargin.m5))
            IconButton(
                onClick = {
                    onToggleFavorite(it, isFavorite)
                },
                modifier = Modifier.size(CommonMargin.m5)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "收藏",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EpisodeSection(
    episodes: List<AnimeEpisodeBean>,
    selectedEpisode: AnimeEpisodeBean?,
    onSelectEpisode: (AnimeEpisodeBean) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(CommonMargin.m2)
    ) {
        episodes.forEach { episode ->
            val isSelected = remember(selectedEpisode) { episode == selectedEpisode }
            Button(
                onClick = { onSelectEpisode(episode) },
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
            text = "${stringResource(R.string.error_text)}：$message",
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
fun CommentItem(comment: AnimeCommentBean, onImageClick: (String) -> Unit) {
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
                            CommentImageResources(it, onImageClick)
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
private fun CommentImageResources(media: MediaBean, onImageClick: (String) -> Unit) {
    Spacer(modifier = Modifier.height(CommonMargin.m2))
    val isLoaded = remember { mutableStateOf(false) }
    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = getImageRequest(context, media.url),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .heightIn(max = 200.dp)
            .clip(RoundedCornerShape(CommonMargin.m2))
            .background(Color.LightGray)
            .clickable(enabled = isLoaded.value) {
                onImageClick(media.url)
            },
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
            isLoaded.value = true
        },
        error = {
            isLoaded.value = false
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
private fun ImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
    onDownload: (String) -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // 圖片內容（置中）
            SubcomposeAsyncImage(
                model = getImageRequest(context, imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 下載按鈕
                IconButton(
                    modifier = Modifier.size(CommonMargin.m5),
                    onClick = {
                        onDownload(imageUrl)
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.download),
                        contentDescription = "下載",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.size(CommonMargin.m3))
                // 關閉按鈕
                IconButton(
                    onClick = onDismiss
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