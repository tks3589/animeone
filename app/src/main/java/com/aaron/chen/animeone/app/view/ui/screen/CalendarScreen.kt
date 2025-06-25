import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.activity.AnimePlayerActivity
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.constant.DefaultConst
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(viewModel: IAnimeoneViewModel) {
    val context = LocalContext.current
    val uiState = remember { mutableStateOf<UiState<AnimeSeasonTimeLineBean>>(UiState.Loading) }
    val daysOfWeek = listOf("一", "二", "三", "四", "五", "六", "日")
    val coroutineScope = rememberCoroutineScope()
    val todayIndex = (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)
    val pagerState = rememberPagerState(
        initialPage = todayIndex,
        pageCount = { daysOfWeek.size }
    )

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
                                    pagerState.scrollToPage(index)
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
                    val currentDay = daysOfWeek.getOrNull(page) ?: DefaultConst.EMPTY_STRING
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
                                        .clickable {
                                            val intent = Intent(context, AnimePlayerActivity::class.java)
                                            intent.putExtra("animeId", anime.id)
                                            context.startActivity(intent)
                                        }
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
