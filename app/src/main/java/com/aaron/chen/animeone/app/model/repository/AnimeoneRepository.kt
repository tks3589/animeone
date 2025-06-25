package com.aaron.chen.animeone.app.model.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.aaron.chen.animeone.app.model.data.bean.AnimeCommentBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.data.bean.toEntity
import com.aaron.chen.animeone.app.model.repository.api.impl.IAnimeoneApiModel
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeoneRepository
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeoneRepository.Companion.INITIAL_LOAD_SIZE
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeoneRepository.Companion.PAGE_SIZE
import com.aaron.chen.animeone.database.dao.AnimeListDao
import com.aaron.chen.animeone.database.dao.AnimeRecordDao
import com.aaron.chen.animeone.database.entity.AnimeEntity
import com.aaron.chen.animeone.database.entity.toBean
import com.aaron.chen.animeone.module.paging.remotemediator.AnimeRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Factory(binds = [IAnimeoneRepository::class])
class AnimeoneRepository: IAnimeoneRepository, KoinComponent {
    private val pagingConfig: PagingConfig = PagingConfig(
        pageSize = PAGE_SIZE,
        initialLoadSize = INITIAL_LOAD_SIZE,
        enablePlaceholders = true
    )
    private var currentPagingSource: PagingSource<Int, AnimeEntity>? = null
    private val animeDao: AnimeListDao by inject()
    private val animeRecordDao: AnimeRecordDao by inject()
    private val animeApiModel: IAnimeoneApiModel by inject()
    private val animeRemoteMediator = AnimeRemoteMediator(animeDao, animeApiModel)

    @OptIn(ExperimentalPagingApi::class)
    override fun requestAnimes(): Flow<PagingData<AnimeEntity>> {
        return Pager(
            config = pagingConfig,
            remoteMediator = animeRemoteMediator,
            pagingSourceFactory = {
                animeDao.getAll()
                    .also { currentPagingSource = it }
            }
        ).flow
    }

    override fun requestAnimeSeasonTimeLine(): Flow<AnimeSeasonTimeLineBean> {
        return animeApiModel.getAnimeSeasonTimeLine()
    }

    override fun requestAnimeEpisodes(animeId: String): Flow<List<AnimeEpisodeBean>> {
        return animeApiModel.getAnimeEpisodes(animeId)
    }

    override fun requestAnimeVideo(dataRaw: String): Flow<AnimeVideoBean> {
        return animeApiModel.requestAnimeVideo(dataRaw)
    }

    override fun requestRecordAnimes(): Flow<List<AnimeRecordBean>> {
        return animeRecordDao.getAll().map { entity ->
            entity.toBean()
        }
    }

    override fun requestAnimeComments(animeId: String): Flow<List<AnimeCommentBean>> {
        return animeApiModel.requestComments(animeId)
    }

    override suspend fun addRecordAnime(anime: AnimeRecordBean) {
        return animeRecordDao.insert(anime.toEntity())
    }
}