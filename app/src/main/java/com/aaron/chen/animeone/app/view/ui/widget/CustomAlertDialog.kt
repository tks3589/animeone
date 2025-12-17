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

    Dialog(onDismissRequest = {}) {
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
                    text = if (type == DialogType.REVIEW_INVITE) { context.getString(R.string.review_invite_confirm) } else { context.getString(R.string.go_to_settings) },
                    modifier = Modifier.padding(vertical = 15.dp),
                    onClick = onConfirm
                )

                if (type == DialogType.REVIEW_INVITE) {
                    CustomButton(
                        text = context.getString(R.string.review_invite_dismiss),
                        modifier = Modifier.padding(vertical = 15.dp),
                        onClick = onDismiss
                    )
                }
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
    PERMISSION_VIDEO_READ(
        R.string.permission_video_read_title,
        R.string.permission_video_read_message
    ),
    PERMISSION_VIDEO_WRITE(
        R.string.permission_video_write_title,
        R.string.permission_video_write_message
    ),
    REVIEW_INVITE(
        R.string.review_invite_title,
        R.string.review_invite_message
    )
}