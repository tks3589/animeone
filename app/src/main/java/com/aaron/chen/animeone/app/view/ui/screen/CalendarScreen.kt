import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(viewModel: IAnimeoneViewModel) {
    val uiState = remember { mutableStateOf<UiState<AnimeSeasonTimeLineBean>>(UiState.Loading) }
    val daysOfWeek = listOf("一", "二", "三", "四", "五", "六", "日")
    val pagerState = rememberPagerState(initialPage = 0) {
        daysOfWeek.size
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.requestAnimeSeasonTimeLine()
            .collect { uiState.value = it }
    }

    when (val state = uiState.value) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "載入失敗：${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            val calendarState = state.data
            val timelineByDay = remember(calendarState) {
                calendarState.timeLine.groupBy { it.day }
            }
            val seasonTitle = calendarState.seasonTitle

            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = seasonTitle, style = MaterialTheme.typography.titleMedium)

                TabRow(selectedTabIndex = pagerState.currentPage) {
                    daysOfWeek.forEachIndexed { index, day ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(day) }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val currentDay = daysOfWeek.getOrNull(page) ?: ""
                    val animeList = timelineByDay[currentDay] ?: emptyList()

                    if (animeList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "當天無新番", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(animeList) { anime ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = anime.title, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
