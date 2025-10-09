package com.aaron.chen.animeone.app.view.viewmodel.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaron.chen.animeone.app.model.data.bean.AnimeFavoriteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeStorageRepository
import com.aaron.chen.animeone.app.model.state.UiState
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeStorageViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinViewModel
class AnimeStorageViewModel: ViewModel(), IAnimeStorageViewModel, KoinComponent {
    override val recordState: MutableStateFlow<UiState<List<AnimeRecordBean>>> = MutableStateFlow(UiState.Idle)
    override val favoriteState: MutableStateFlow<UiState<List<AnimeFavoriteBean>>> = MutableStateFlow(UiState.Idle)
    override val bookState: MutableStateFlow<UiState<Boolean>> = MutableStateFlow(UiState.Idle)
    private val storageRepository: IAnimeStorageRepository by inject()

    override fun requestRecordAnimes() {
        viewModelScope.launch {
            storageRepository.requestRecordAnimes()
                .onStart {
                    recordState.value = UiState.Loading
                }.catch {
                    recordState.value = UiState.Error(it.message)
                }.collect { result ->
                    recordState.value = UiState.Success(result)
                }
        }
    }

    override suspend fun addRecordAnime(anime: AnimeRecordBean) {
        viewModelScope.launch {
            storageRepository.addRecordAnime(anime)
        }
    }

    override fun requestFavoriteAnimes() {
        viewModelScope.launch {
            storageRepository.requestFavoriteAnimes()
                .onStart {
                    favoriteState.value = UiState.Loading
                }.catch {
                    favoriteState.value = UiState.Error(it.message)
                }.collect { result ->
                    favoriteState.value = UiState.Success(result)
                }
        }
    }

    override fun requestBookState(animeId: String) {
        viewModelScope.launch {
            storageRepository.requestBookState(animeId)
                .collect { result ->
                    bookState.value = UiState.Success(result)
                }
        }
    }

    override suspend fun bookAnime(anime: AnimeFavoriteBean) {
        viewModelScope.launch {
            storageRepository.bookAnime(anime)
        }
    }

    override suspend fun unbookAnime(animeId: String) {
        viewModelScope.launch {
            storageRepository.unbookAnime(animeId)
        }
    }
}