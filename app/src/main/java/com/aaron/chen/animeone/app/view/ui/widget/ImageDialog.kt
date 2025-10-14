package com.aaron.chen.animeone.app.view.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.utils.MediaUtils.getImageRequest

@Composable
fun ImageDialog(
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