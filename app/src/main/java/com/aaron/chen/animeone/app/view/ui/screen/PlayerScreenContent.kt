package com.aaron.chen.animeone.app.view.ui.screen

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeFavoriteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextL
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextM
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextS
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextXS
import com.aaron.chen.animeone.app.view.ui.widget.SpoilerText
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeDownloadViewModel
import com.aaron.chen.animeone.app.view.viewmodel.IAnimePlayerViewModel
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeStorageViewModel
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import com.aaron.chen.animeone.utils.CommentUtils.getImageRequest
import com.aaron.chen.animeone.utils.CommentUtils.getVideoHeaders
import com.aaron.chen.animeone.utils.CommentUtils.getVideoSrc
import com.aaron.chen.animeone.utils.CommentUtils.parseMessage
import com.aaron.chen.animeone.utils.MessagePart
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun PlayerScreenContent(
    activity: Activity,
    animeId: String,
    commentAnimeId: String,
    playerSourceState: PlayerSourceState,
    animeoneViewModel: IAnimeoneViewModel,
    playerViewModel: IAnimePlayerViewModel,
    storageViewModel: IAnimeStorageViewModel,
    downloadViewModel: IAnimeDownloadViewModel
) {
    val isFullscreen = playerSourceState.isFullscreen
    val selectedEpisodeState = playerSourceState.selectedEpisodeState
    val isVideoBufferingState = playerSourceState.isVideoBufferingState
    val currentVideoState = playerSourceState.currentVideoState
    val favoriteBookState = playerSourceState.favoriteBookState
    val episodeLoadState = playerSourceState.episodeLoadState
    val commentsLoadState = playerSourceState.commentsLoadState
    val imageDialogUrlState = playerSourceState.imageDialogUrlState
    val actionHandler = remember(activity) { PlayerActionHandler(activity, storageViewModel, downloadViewModel) }
    PlayerSection(
        player = playerViewModel.getPlayer(),
        isFullscreen = isFullscreen.value,
        onToggleFullscreen = { isFullscreen.value = !isFullscreen.value },
        onExit = {
            if (!isFullscreen.value) {
                activity.finish()
            } else {
                isFullscreen.value = false
            }
        },
        selectedEpisode = selectedEpisodeState.value,
        onShare = { text ->
            actionHandler.shareText(text)
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
                AnimeInfoSection(
                    animeId = animeId,
                    selectedEpisodeState = selectedEpisodeState,
                    currentVideoState = currentVideoState,
                    favoriteBookState = favoriteBookState,
                    actionHandler = actionHandler
                )
            }
            item {
                EpisodeSection(
                    episodes = (episodeLoadState.value as? UiState.Success)?.data.orEmpty(),
                    selectedEpisode = selectedEpisodeState.value,
                    onSelectEpisode = { playerViewModel.selectedEpisode.value = it }
                )
            }
            commentSection(
                commentsLoadState = commentsLoadState,
                imageDialogUrlState = imageDialogUrlState
            )
        }
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
    val playerView = remember {
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
    }
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
            factory = { playerView },
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
fun AnimeInfoSection(
    animeId: String,
    selectedEpisodeState: State<AnimeEpisodeBean?>,
    currentVideoState: State<AnimeVideoBean?>,
    favoriteBookState: State<UiState<Boolean>>,
    actionHandler: PlayerActionHandler
) {
    val coroutineScope = rememberCoroutineScope()
    AnimeTitleSection(
        episode = selectedEpisodeState.value,
        video = currentVideoState.value,
        isFavorite = (favoriteBookState.value as? UiState.Success)?.data ?: false,
        onDownload = { video, episode ->
            actionHandler.download(video, episode)
        },
        onToggleFavorite = { episode, isFavorite ->
            coroutineScope.launch {
                actionHandler.toggleFavorite(animeId, episode, isFavorite)
            }
        }
    )
    selectedEpisodeState.value?.let { episode ->
        CommonTextXS(text = stringResource(R.string.update_time, episode.updateTime), modifier = Modifier.padding(start = CommonMargin.m4))
    }
}

class PlayerActionHandler(
    private val activity: Activity,
    private val storageViewModel: IAnimeStorageViewModel,
    private val downloadViewModel: IAnimeDownloadViewModel
) {
    fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share_to)))
    }

    suspend fun toggleFavorite(animeId: String, episode: AnimeEpisodeBean, isFavorite: Boolean) {
        if (isFavorite) {
            storageViewModel.unbookAnime(animeId)
            showToast(activity.getString(R.string.favorite_remove))
        } else {
            storageViewModel.bookAnime(
                AnimeFavoriteBean(animeId, episode.title, episode.episode, Clock.System.now().toEpochMilliseconds())
            )
            showToast(activity.getString(R.string.favorite_success))
        }
    }

    fun download(video: AnimeVideoBean, episode: AnimeEpisodeBean) {
        downloadViewModel.download(
            url = getVideoSrc(video),
            headers = getVideoHeaders(video.cookie),
            episodeBean = episode
        )
    }

    private fun showToast(text: String) = Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
}

fun LazyListScope.commentSection(
    commentsLoadState: State<UiState<List<AnimeCommentBean>>>,
    imageDialogUrlState: MutableState<Pair<Int, List<String>>?>
) {
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
                        imageDialogUrlState.value = it
                    }
                    Divider(
                        modifier = Modifier.padding(vertical = CommonMargin.m2)
                    )
                }
            }
            item {
                CommonTextS(
                    text = stringResource(R.string.has_no_more_comment),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(CommonMargin.m2),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CommentItem(comment: AnimeCommentBean, onImageClick: (Pair<Int, List<String>>) -> Unit) {
    val isReply = comment.isReply
    val parts = parseMessage(comment.text.trim())
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(
            start = if (isReply) CommonMargin.m10 else 0.dp,
            end = CommonMargin.m4
        )
    ) {
        Avatar(url = comment.user.picture)
        Spacer(modifier = Modifier.width(CommonMargin.m2))
        Column(modifier = Modifier.weight(1f)) {
            CommonTextS(
                text = comment.user.name,
                color = MaterialTheme.colorScheme.primary
            )
            val mediaUrlList = parts.filter { it is MessagePart.Media }.map { (it as MessagePart.Media).url }
            parts.forEach { part ->
                when (part) {
                    is MessagePart.Text -> {
                        CommonTextS(
                            text = part.text.trim(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(top = CommonMargin.m1)
                        )
                    }
                    is MessagePart.Spoiler -> {
                        SpoilerText(
                            text = part.text.trim(),
                            modifier = Modifier.padding(top = CommonMargin.m1)
                        )
                    }
                    is MessagePart.Media -> {
                        CommentImageResources(mediaUrlList, mediaUrlList.indexOf(part.url), onImageClick)
                    }
                    MessagePart.BlankLine -> {
                        Spacer(modifier = Modifier.height(CommonMargin.m1))
                    }
                }
            }
            CommonTextXS(
                text = comment.time,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun CommentImageResources(mediaUrlList: List<String>, index: Int, onImageClick: (Pair<Int, List<String>>) -> Unit) {
    Spacer(modifier = Modifier.height(CommonMargin.m2))
    val isLoaded = remember { mutableStateOf(false) }
    val context = LocalContext.current
    SubcomposeAsyncImage(
        model = getImageRequest(context, mediaUrlList[index]),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .heightIn(max = 200.dp)
            .clip(RoundedCornerShape(CommonMargin.m2))
            .background(Color.LightGray)
            .clickable(enabled = isLoaded.value) {
                onImageClick(Pair(index, mediaUrlList))
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
    val defaultAvatar = painterResource(R.drawable.ic_default_avatar)
    AsyncImage(
        model = url,
        contentDescription = "頭像",
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Fit,
        placeholder = defaultAvatar, // 載入中
        error = defaultAvatar,       // 失敗
        fallback = defaultAvatar     // url 為 null
    )
}