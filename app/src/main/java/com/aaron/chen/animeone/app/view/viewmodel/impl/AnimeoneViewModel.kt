package com.aaron.chen.animeone.app.view.viewmodel.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeoneRepository
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinViewModel
class AnimeoneViewModel: ViewModel(), IAnimeoneViewModel, KoinComponent {
    override val timeLineState: MutableStateFlow<UiState<AnimeSeasonTimeLineBean>> = MutableStateFlow(UiState.Idle)
    override val episodeState: MutableStateFlow<UiState<List<AnimeEpisodeBean>>> = MutableStateFlow(UiState.Idle)
    override val commentState: MutableStateFlow<UiState<List<AnimeCommentBean>>> = MutableStateFlow(UiState.Idle)
    private val animeRepository: IAnimeoneRepository by inject()
    override fun requestAnimeList(): Flow<PagingData<AnimeEntity>> {
        return animeRepository.requestAnimes().cachedIn(viewModelScope)
    }
    private var commentJob: Job? = null

    override fun requestAnimeSeasonTimeLine() {
        viewModelScope.launch {
            animeRepository.requestAnimeSeasonTimeLine()
                .onStart {
                    timeLineState.value = UiState.Loading
                }.catch {
                    timeLineState.value = UiState.Error(it.message)
                }.collect { result ->
                    timeLineState.value = UiState.Success(result)
                }
        }
    }

    override fun requestAnimeEpisodes(animeId: String){
        viewModelScope.launch {
            animeRepository.requestAnimeEpisodes(animeId)
                .onStart {
                    episodeState.value = UiState.Loading
                }.catch {
                    episodeState.value = UiState.Error(it.message)
                }.collect { result ->
                    if (result.isEmpty()) {
                        episodeState.value = UiState.Empty
                    } else {
                        episodeState.value = UiState.Success(result)
                    }
                }
        }
    }

    override fun requestAnimeVideo(dataRaw: String): Flow<AnimeVideoBean> {
        return animeRepository.requestAnimeVideo(dataRaw)
    }

    override fun requestAnimeComments(animeId: String) {
        commentJob?.cancel()
        commentJob = viewModelScope.launch {
            animeRepository.requestAnimeComments(animeId)
                .onStart {
                    commentState.value = UiState.Loading
                }.catch {
                    commentState.value = UiState.Error(it.message)
                }.collect { result ->
                    if (result.isEmpty()) {
                        commentState.value = UiState.Empty
                    } else {
                        commentState.value = UiState.Success(result)
                    }
                }
        }
    }
}