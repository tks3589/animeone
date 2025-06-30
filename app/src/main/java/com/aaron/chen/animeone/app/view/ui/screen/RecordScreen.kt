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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aaron.chen.animeone.R
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.activity.AnimePlayerActivity
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel

@Composable
fun RecordScreen(viewModel: IAnimeoneViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.recordState.collectAsState(UiState.Idle)

    LaunchedEffect(Unit) {
        viewModel.requestRecordAnimes()
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
                Text(text = "載入失敗：${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
        is UiState.Success -> {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(text = stringResource(R.string.record_list), style = MaterialTheme.typography.titleMedium)
                val recordList = state.data
                if (recordList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.no_record_list), style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                        items(recordList) { anime ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(context, AnimePlayerActivity::class.java)
                                        intent.putExtra("animeId", anime.id)
                                        intent.putExtra("episode", anime.episode)
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