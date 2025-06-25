package com.aaron.chen.animeone.app.model.repository.impl

import androidx.paging.PagingData
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
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
    fun requestAnimeComments(animeId: String): Flow<List<AnimeCommentBean>>
    suspend fun addRecordAnime(anime: AnimeRecordBean)
}