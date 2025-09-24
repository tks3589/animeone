package com.aaron.chen.animeone.app.view.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextM
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel

@Composable
fun DownloadScreen(viewModel: IAnimeoneViewModel) {
    Column(
        modifier = Modifier.padding(CommonMargin.m6)
    ) {
        CommonTextM(
            text = stringResource(R.string.download_manager)
        )
    }
}