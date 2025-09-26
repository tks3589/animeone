package com.aaron.chen.animeone.app.model.repository.impl

import androidx.paging.PagingData
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeFavoriteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow

interface IAnimeoneRepository {
    companion object {
        const val PAGE_SIZE = 50
        const val INITIAL_LOAD_SIZE = PAGE_SIZE * 40
    }
    fun requestAnimes(): Flow<PagingData<AnimeEntity>>
    fun requestAnimeSeasonTimeLine(): Flow<AnimeSeasonTimeLineBean>
    fun requestAnimeEpisodes(animeId: String): Flow<List<AnimeEpisodeBean>>
    fun requestAnimeVideo(dataRaw: String): Flow<AnimeVideoBean>
    fun requestRecordAnimes(): Flow<List<AnimeRecordBean>>
    fun requestFavoriteAnimes(): Flow<List<AnimeFavoriteBean>>
    fun requestAnimeComments(animeId: String): Flow<List<AnimeCommentBean>>
    fun requestFavoriteState(animeId: String): Flow<Boolean>
    suspend fun addRecordAnime(anime: AnimeRecordBean)
    suspend fun addFavoriteAnime(anime: AnimeFavoriteBean)
    suspend fun removeFavoriteAnime(animeId: String)
}