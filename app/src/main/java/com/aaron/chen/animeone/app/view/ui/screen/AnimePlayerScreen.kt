package com.aaron.chen.animeone.app.view.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AnimePlayerScreen(animeId: String) {
    // 你可以用 ViewModel 根據 ID 撈資料
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "播放動畫 ID: $animeId",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    // TODO: 整合 ExoPlayer 或 WebView 實際播放內容
}
