package com.aaron.chen.animeone.app.view.viewmodel

import com.aaron.chen.animeone.app.model.data.bean.AnimeFavoriteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.state.UiState
import kotlinx.coroutines.flow.StateFlow

interface IAnimeStorageViewModel {
    val recordState: StateFlow<UiState<List<AnimeRecordBean>>>
    val favoriteState: StateFlow<UiState<List<AnimeFavoriteBean>>>
    val bookState: StateFlow<UiState<Boolean>>

    fun requestRecordAnimes()
    suspend fun addRecordAnime(anime: AnimeRecordBean)
    fun requestFavoriteAnimes()
    fun requestBookState(animeId: String)
    suspend fun bookAnime(anime: AnimeFavoriteBean)
    suspend fun unbookAnime(animeId: String)
    fun increaseAnimeListClick()
    fun resetAnimeListClick()
    fun markReviewFirstTriggered(trigger: Boolean)
    suspend fun recordFirstLaunchDate()
    suspend fun shouldTryReview(): Boolean
}