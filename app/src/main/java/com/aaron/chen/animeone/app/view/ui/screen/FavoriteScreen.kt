package com.aaron.chen.animeone.app.view.ui.screen

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.activity.AnimePlayerActivity
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextM
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextS
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.constant.ExtraConst

@Composable
fun FavoriteScreen(viewModel: IAnimeoneViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.favoriteState.collectAsState(UiState.Idle)

    LaunchedEffect(Unit) {
        viewModel.requestFavoriteAnimes()
    }

    when (val state = uiState.value) {
        UiState.Idle -> {
            // 初始狀態
        }
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        UiState.Empty -> {}
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CommonTextS(
                    text = "${stringResource(R.string.error_text)}：${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is UiState.Success -> {
            Column(
                modifier = Modifier.padding(CommonMargin.m6)
            ) {
                CommonTextM(
                    text = stringResource(R.string.my_favorite)
                )
                val favoriteList = state.data
                if (favoriteList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CommonTextS(
                            text = stringResource(R.string.no_favorite_list)
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = CommonMargin.m4)) {
                        items(favoriteList) { anime ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(context, AnimePlayerActivity::class.java)
                                        intent.putExtra(ExtraConst.ANIME_ID, anime.id)
                                        intent.putExtra(ExtraConst.EPISODE, anime.episode)
                                        context.startActivity(intent)
                                    }
                                    .padding(vertical = CommonMargin.m1),
                                elevation = CardDefaults.cardElevation(CommonMargin.m1)
                            ) {
                                Column(modifier = Modifier.padding(CommonMargin.m4)) {
                                    CommonTextM(
                                        text = anime.title,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}