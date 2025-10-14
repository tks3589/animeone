package com.aaron.chen.animeone.app.view.ui.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin

@Composable
fun LoadingText() {
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
fun ErrorText(message: String? = null) {
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