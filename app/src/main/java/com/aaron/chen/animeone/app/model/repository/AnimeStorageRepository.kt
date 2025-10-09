package com.aaron.chen.animeone.app.model.repository

import com.aaron.chen.animeone.app.model.data.bean.AnimeFavoriteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.data.bean.toEntity
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeStorageRepository
import com.aaron.chen.animeone.database.dao.AnimeFavoriteDao
import com.aaron.chen.animeone.database.dao.AnimeRecordDao
import com.aaron.chen.animeone.database.entity.toBean
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@Factory(binds = [IAnimeStorageRepository::class])
class AnimeStorageRepository: IAnimeStorageRepository, KoinComponent {
    private val animeRecordDao: AnimeRecordDao by inject()
    private val animeFavoriteDao: AnimeFavoriteDao by inject()
    override fun requestRecordAnimes(): Flow<List<AnimeRecordBean>> {
        return animeRecordDao.getAll().map { entity ->
            entity.toBean()
        }
    }

    override fun requestFavoriteAnimes(): Flow<List<AnimeFavoriteBean>> {
        return animeFavoriteDao.getAll().map { entity ->
            entity.toBean()
        }
    }

    override fun requestBookState(animeId: String): Flow<Boolean> {
        return animeFavoriteDao.isFavorite(animeId)
    }

    override suspend fun addRecordAnime(anime: AnimeRecordBean) {
        animeRecordDao.insert(anime.toEntity())
    }

    override suspend fun bookAnime(anime: AnimeFavoriteBean) {
        animeFavoriteDao.insert(anime.toEntity())
    }

    override suspend fun unbookAnime(animeId: String) {
        animeFavoriteDao.deleteFromId(animeId)
    }
}