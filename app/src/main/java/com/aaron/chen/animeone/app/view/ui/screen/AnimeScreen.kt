package com.aaron.chen.animeone.app.view.ui.screen

import android.content.Intent
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.view.activity.AnimePlayerActivity
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextM
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextS
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextXS
import com.aaron.chen.animeone.app.view.ui.widget.PullToRefresh
import com.aaron.chen.animeone.app.view.viewmodel.impl.AnimeoneViewModel
import com.aaron.chen.animeone.constant.DefaultConst
import com.aaron.chen.animeone.constant.ExtraConst
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnimeScreen(
    navController: NavHostController
) {
    val viewModel = koinViewModel<AnimeoneViewModel>()
    val context = LocalContext.current
    val animeItems = viewModel.requestAnimeList().collectAsLazyPagingItems()
    var searchQuery by remember { mutableStateOf(DefaultConst.EMPTY_STRING) }
    val scrollToTopTriggerFlow = navController.currentBackStackEntry?.savedStateHandle
        ?.getStateFlow(ExtraConst.SCROLL_TO_TOP, false)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        listState.scrollToItem(0) // 進來就滾回top
        scrollToTopTriggerFlow?.collect { scrollToTop ->
            if (scrollToTop) {
                listState.scrollToItem(0)
                animeItems.refresh()
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    ExtraConst.SCROLL_TO_TOP,
                    false
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(CommonMargin.m6)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    coroutineScope.launch {
                        listState.scrollToItem(0)
                    }
                },
                placeholder = { Text(stringResource(R.string.search_anime)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(CommonMargin.m4))

            PullToRefresh(
                isRefreshing = animeItems.loadState.refresh is LoadState.Loading,
                onRefresh = { animeItems.refresh() },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(CommonMargin.m2)
                ) {
                    when (val state = animeItems.loadState.refresh) {
                        is LoadState.Error -> {
                            item {
                                CommonTextS(
                                    text = "${stringResource(R.string.error_text)} : ${state.error.localizedMessage}",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Start
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
                                    intent.putExtra(ExtraConst.ANIME_ID, anime.id)
                                    intent.putExtra(ExtraConst.PLAY_LAST, true)
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
            .padding(CommonMargin.m2),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = R.drawable.anime1,
                contentDescription = anime.title,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(CommonMargin.m2)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(CommonMargin.m3))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                CommonTextM(
                    text = anime.title,
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CommonTextXS(
                        text = anime.status,
                        color = Color.Gray,
                        maxLines = 1,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = RoundedCornerShape(CommonMargin.m1)
                            )
                            .padding(horizontal = CommonMargin.m1)
                    )

                    Spacer(modifier = Modifier.weight(1f))

//                    Text(
//                        text = "NEW!!",
//                        color = Color.Yellow,
//                        fontSize = 10.sp
//                    )
                    CommonTextXS(
                        text = "${anime.year} , ${anime.season}",
                        color = Color.Gray,
                        maxLines = 1,
                        modifier = Modifier.padding(start = CommonMargin.m2)
                    )
                }
            }
        }
    }

}
