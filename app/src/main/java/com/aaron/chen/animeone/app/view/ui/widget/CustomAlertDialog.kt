package com.aaron.chen.animeone.app.view.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.theme.Dark
import com.aaron.chen.animeone.app.view.ui.theme.Light

@Composable
fun CustomAlertDialog(
    type: DialogType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .wrapContentHeight(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Dark)
        ) {
            Column(
                modifier = Modifier.padding(CommonMargin.m5),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 標題
                CommonTextXL(
                    text = context.getString(type.titleRes),
                    modifier = Modifier.padding(top = 15.dp),
                    color = Light,
                    bold = true
                )

                // 說明文字
                CommonTextS(
                    text = context.getString(type.messageRes),
                    modifier = Modifier.padding(top = 10.dp),
                    color = Light
                )

                // 確認按鈕
                CustomButton(
                    text = context.getString(R.string.go_to_settings),
                    modifier = Modifier.padding(vertical = 15.dp),
                    onClick = onConfirm
                )
            }
        }
    }
}

enum class DialogType(
    val titleRes: Int,
    val messageRes: Int
) {
    PERMISSION_NOTIFICATION(
        R.string.permission_notification_title,
        R.string.permission_notification_message
    ),
    PERMISSION_VIDEO_STORAGE(
        R.string.permission_video_storage_title,
        R.string.permission_video_storage_message
    )
}