package com.aaron.chen.animeone.app.view.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeoneViewModel
import com.aaron.chen.animeone.database.entity.AnimeEntity
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnimeScreen(
    viewModel: AnimeoneViewModel = koinViewModel()
) {
    val animeItems = viewModel.requestAnimes().collectAsLazyPagingItems()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.anime_list),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(animeItems.itemCount) { index ->
                    val anime = animeItems[index]
                    if (anime != null) {
                        AnimeItem(anime)
                    }
                }

                animeItems.apply {
                    when {
                        loadState.refresh is LoadState.Loading -> {
                            item { Text("載入中...") }
                        }
                        loadState.append is LoadState.Loading -> {
                            item { Text("載入更多中...") }
                        }
                        loadState.refresh is LoadState.Error -> {
                            val e = loadState.refresh as LoadState.Error
                            item { Text("載入失敗: ${e.error.localizedMessage}") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeItem(anime: AnimeEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        AsyncImage(
            model = R.drawable.anime1,
            contentDescription = anime.title,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            Text(
                text = anime.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )
        }
    }
}