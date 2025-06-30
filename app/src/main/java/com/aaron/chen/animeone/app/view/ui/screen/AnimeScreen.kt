package com.aaron.chen.animeone.app.view.ui.screen

import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.view.activity.AnimePlayerActivity
import com.aaron.chen.animeone.app.view.ui.widget.PullToRefresh
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.database.entity.AnimeEntity

@Composable
fun AnimeScreen(
    viewModel: IAnimeoneViewModel
) {
    val context = LocalContext.current
    val animeItems = viewModel.requestAnimeList().collectAsLazyPagingItems()
    var searchQuery by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜尋動畫名稱") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PullToRefresh(
                isRefreshing = animeItems.loadState.refresh is LoadState.Loading,
                onRefresh = { animeItems.refresh() },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (val state = animeItems.loadState.refresh) {
                        is LoadState.Error -> {
                            item {
                                Text(
                                    text = "載入失敗: ${state.error.localizedMessage}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }
                        else -> {
                            val filteredItems = animeItems.itemSnapshotList.items.filter {
                                searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)
                            }
                            items(filteredItems) { anime ->
                                AnimeItem(anime, onClick = {
                                    val intent = Intent(context, AnimePlayerActivity::class.java)
                                    intent.putExtra("animeId", anime.id)
                                    context.startActivity(intent)
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeItem(anime: AnimeEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = anime.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )

            Text(
                text = anime.status+" , "+anime.year+" , "+anime.season,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}
