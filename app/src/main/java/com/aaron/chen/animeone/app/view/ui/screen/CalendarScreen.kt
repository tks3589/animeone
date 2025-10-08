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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.activity.AnimePlayerActivity
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextM
import com.aaron.chen.animeone.app.view.ui.widget.CommonTextS
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.constant.DefaultConst
import com.aaron.chen.animeone.constant.ExtraConst
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(viewModel: IAnimeoneViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.timeLineState.collectAsStateWithLifecycle(UiState.Idle)
    val daysOfWeek = listOf("一", "二", "三", "四", "五", "六", "日")
    val coroutineScope = rememberCoroutineScope()
    val todayIndex = (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)
    val pagerState = rememberPagerState(
        initialPage = todayIndex,
        pageCount = { daysOfWeek.size }
    )

    LaunchedEffect(Unit) {
        viewModel.requestAnimeSeasonTimeLine()
    }

    when (val state = uiState.value) {
        is UiState.Idle -> {
            // 初始狀態，無需顯示任何內容
        }
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Empty -> {}
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CommonTextS(
                    text = "${stringResource(R.string.error_text)}：${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is UiState.Success -> {
            val calendarState = state.data
            val timelineByDay = remember(calendarState) {
                calendarState.timeLine.groupBy { it.day }
            }
            val seasonTitle = calendarState.seasonTitle

            Column(modifier = Modifier.padding(CommonMargin.m6)) {
                CommonTextM(
                    text = seasonTitle
                )

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
                                .padding(CommonMargin.m4),
                            contentAlignment = Alignment.Center
                        ) {
                            CommonTextS(
                                text = stringResource(R.string.no_newest_anime)
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(animeList) { anime ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val intent = Intent(context, AnimePlayerActivity::class.java)
                                            intent.putExtra(ExtraConst.ANIME_ID, anime.id)
                                            intent.putExtra(ExtraConst.PLAY_LAST, true)
                                            context.startActivity(intent)
                                        }
                                        .padding(vertical = CommonMargin.m1),
                                    elevation = CardDefaults.cardElevation(CommonMargin.m1),
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
}
