package com.aaron.chen.animeone.app.view.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.data.bean.AnimeDownloadBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextM
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextS
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeDownloadViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DownloadScreen() {
    val viewModel = koinViewModel<AnimeDownloadViewModel>()
    val loadVideoState = viewModel.loadVideoState.collectAsStateWithLifecycle(UiState.Idle)
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // ✅ 編輯模式狀態
    val isEditing = remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateOf(setOf<String>()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDownloadedVideos()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CommonMargin.m6)
    ) {
        // ✅ 標題 + 編輯按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = CommonMargin.m2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CommonTextM(
                text = stringResource(R.string.download_manager)
            )

            Text(
                text = if (isEditing.value) "完成" else "編輯",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable {
                        if (isEditing.value) {
                            selectedItems.value = emptySet()
                        }
                        isEditing.value = !isEditing.value
                    }
            )
        }

        when (val state = loadVideoState.value) {
            is UiState.Idle -> {}
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            UiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CommonTextS(text = "沒有下載影片")
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CommonTextS(
                        text = "讀取失敗",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is UiState.Success -> {
                val videos = state.data
                LazyColumn {
                    items(videos.size) { index ->
                        VideoItem(
                            context = context,
                            video = videos[index],
                            viewModel = viewModel,
                            isEditing = isEditing.value,
                            isSelected = selectedItems.value.contains(videos[index].path),
                            onSelectChange = { checked ->
                                selectedItems.value = if (checked) {
                                    selectedItems.value + videos[index].path
                                } else {
                                    selectedItems.value - videos[index].path
                                }
                            }
                        )
                    }
                }

                // ✅ 底部刪除按鈕（編輯模式下才出現）
                if (isEditing.value && selectedItems.value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "刪除選取 (${selectedItems.value.size})",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.error)
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .clickable {
                                    // ✅ 刪除影片
                                    selectedItems.value.forEach { path ->
                                        viewModel.deleteVideo(context, path)
                                    }
                                    selectedItems.value = emptySet()
                                    viewModel.loadDownloadedVideos()
                                    isEditing.value = false
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItem(
    context: Context,
    video: AnimeDownloadBean,
    viewModel: AnimeDownloadViewModel,
    isEditing: Boolean,
    isSelected: Boolean,
    onSelectChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                if (isEditing) {
                    // ✅ 編輯模式：點整行切換勾選
                    onSelectChange(!isSelected)
                } else {
                    // ✅ 非編輯模式：開啟影片
                    val uri = video.path.toUri()
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "video/mp4")
                    }
                    context.startActivity(intent)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ 編輯模式下顯示 checkbox
        if (isEditing) {
            androidx.compose.material3.Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectChange(it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        video.preview?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } ?: Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("No preview", color = Color.White, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = video.name,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
