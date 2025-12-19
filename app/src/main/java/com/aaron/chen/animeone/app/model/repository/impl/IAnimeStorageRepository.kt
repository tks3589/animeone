package com.aaron.chen.animeone.app.model.repository.impl

import com.aaron.chen.animeone.app.model.data.bean.AnimeFavoriteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import kotlinx.coroutines.flow.Flow

interface IAnimeStorageRepository {
    fun requestRecordAnimes(): Flow<List<AnimeRecordBean>>
    fun requestFavoriteAnimes(): Flow<List<AnimeFavoriteBean>>
    fun requestBookState(animeId: String): Flow<Boolean>
    suspend fun addRecordAnime(anime: AnimeRecordBean)
    suspend fun bookAnime(anime: AnimeFavoriteBean)
    suspend fun unbookAnime(animeId: String)
    suspend fun increaseAnimeListClick()
    suspend fun resetAnimeListClick()
    suspend fun recordFirstLaunchDate()
    suspend fun markReviewFirstTriggered(trigger: Boolean)
    suspend fun shouldTryReview(): Boolean
}