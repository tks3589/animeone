package com.aaron.chen.animeone.app.view.ui.screen

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.ui.widget.ErrorText
import com.aaron.chen.animeone.app.view.ui.widget.ImageDialog
import com.aaron.chen.animeone.app.view.ui.widget.LoadingText
import com.aaron.chen.animeone.app.view.viewmodel.IAnimePlayerViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeDownloadViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeStorageViewModel
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeoneViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.Q)
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
            activity?.let {
                PlayerScreenContent(
                    activity = it,
                    animeId = animeId,
                    isFullscreen = isFullscreen,
                    selectedEpisodeState = selectedEpisodeState,
                    isVideoBufferingState = isVideoBufferingState,
                    currentVideoState = currentVideoState,
                    favoriteBookState = favoriteBookState,
                    episodeLoadState = episodeLoadState,
                    commentsLoadState = commentsLoadState,
                    imageDialogUrl = imageDialogUrl,
                    playerViewModel = playerViewModel,
                    storageViewModel = storageViewModel,
                    downloadViewModel = downloadViewModel
                )
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