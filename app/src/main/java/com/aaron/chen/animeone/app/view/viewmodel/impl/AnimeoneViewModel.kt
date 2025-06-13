package com.aaron.chen.animeone.app.view.viewmodel.impl

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeoneRepository
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinViewModel
class AnimeoneViewModel: ViewModel(), IAnimeoneViewModel, KoinComponent {
    private val animeRepository: IAnimeoneRepository by inject()
    override fun requestAnimes(): Flow<PagingData<AnimeEntity>> {
        return animeRepository.requestAnimes()
    }

    override fun requestAnimeSeasonTimeLine(): Flow<UiState<AnimeSeasonTimeLineBean>> {
        return flow {
            emit(UiState.Loading)
            try {
                val result = animeRepository.requestAnimeSeasonTimeLine().first()
                emit(UiState.Success(result))
            } catch (e: Exception) {
                emit(UiState.Error(e.message ?: "未知錯誤"))
            }
        }
    }

}