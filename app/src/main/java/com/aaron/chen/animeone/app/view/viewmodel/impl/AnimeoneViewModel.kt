package com.aaron.chen.animeone.app.view.viewmodel.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeoneRepository
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinViewModel
class AnimeoneViewModel: ViewModel(), IAnimeoneViewModel, KoinComponent {
    private val animeRepository: IAnimeoneRepository by inject()
    override fun requestAnimes(): Flow<PagingData<AnimeEntity>> {
        return animeRepository.requestAnimes().cachedIn(viewModelScope)
    }

    override fun requestAnimeSeasonTimeLine(): Flow<UiState<AnimeSeasonTimeLineBean>> {
        return flow {
            emit(UiState.Loading)
            try {
                val result = animeRepository.requestAnimeSeasonTimeLine().first()
                emit(UiState.Success(result))
            } catch (e: Exception) {
                emit(UiState.Error(e.message))
            }
        }
    }

    override fun requestAnimeEpisodes(animeId: String): Flow<UiState<List<AnimeEpisodeBean>>> {
        return flow {
            emit(UiState.Loading)
            try {
                val result = animeRepository.requestAnimeEpisodes(animeId).first()
                emit(UiState.Success(result))
            } catch (e: Exception) {
                emit(UiState.Error(e.message))
            }
        }
    }

    override fun requestAnimeVideo(dataRaw: String): Flow<AnimeVideoBean> {
        return animeRepository.requestAnimeVideo(dataRaw)
    }

    override fun requestRecordAnimes(): Flow<UiState<List<AnimeRecordBean>>> {
        return flow {
            emit(UiState.Loading)
            try {
                val result = animeRepository.requestRecordAnimes().first()
                emit(UiState.Success(result))
            } catch (e: Exception) {
                emit(UiState.Error(e.message))
            }
        }
    }

    override suspend fun addRecordAnime(anime: AnimeRecordBean) {
        viewModelScope.launch {
            animeRepository.addRecordAnime(anime)
        }
    }

    override fun requestAnimeComments(animeId: String): Flow<UiState<List<AnimeCommentBean>>> {
        return flow {
            emit(UiState.Loading)
            try {
                val result = animeRepository.requestAnimeComments(animeId).first()
                emit(UiState.Success(result))
            } catch (e: Exception) {
                emit(UiState.Error(e.message))
            }
        }
    }
}