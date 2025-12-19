package com.aaron.chen.animeone.app.model.repository

import android.util.Log
import com.aaron.chen.animeone.app.model.data.bean.AnimeFavoriteBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeRecordBean
import com.aaron.chen.animeone.app.model.data.bean.toEntity
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeStorageRepository
import com.aaron.chen.animeone.constant.DefaultConst
import com.aaron.chen.animeone.database.dao.AnimeFavoriteDao
import com.aaron.chen.animeone.database.dao.AnimeRecordDao
import com.aaron.chen.animeone.database.entity.toBean
import com.aaron.chen.animeone.module.datastore.IAnimePrefRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@Factory(binds = [IAnimeStorageRepository::class])
class AnimeStorageRepository: IAnimeStorageRepository, KoinComponent {
    private val animeRecordDao: AnimeRecordDao by inject()
    private val animeReviewPref: IAnimePrefRepository by inject()
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

    override suspend fun increaseAnimeListClick() {
        animeReviewPref.animeListClickCount.writeFlow(animeReviewPref.animeListClickCount.readFlow().first() + 1).first()
    }

    override suspend fun resetAnimeListClick() {
        animeReviewPref.animeListClickCount.writeFlow(DefaultConst.INT_COUNT).first()
    }

    override suspend fun recordFirstLaunchDate() {
        if (animeReviewPref.lastReviewTriggerTime.readFlow().first() == DefaultConst.EMPTY_STRING) {
            animeReviewPref.lastReviewTriggerTime.writeFlow(System.currentTimeMillis().toString()).first()
        }
    }

    override suspend fun markReviewFirstTriggered(trigger: Boolean) {
        animeReviewPref.hasReviewInviteTriggered.writeFlow(trigger).first()
    }

    override suspend fun shouldTryReview(): Boolean {
        val firstTriggerTime = animeReviewPref.lastReviewTriggerTime.readFlow().first()
        val clickCount = animeReviewPref.animeListClickCount.readFlow().first()
        val hasReviewTriggered = animeReviewPref.hasReviewInviteTriggered.readFlow().first()
        Log.d("aaron_tt[shouldTryReview]", "firstTriggerTime: $firstTriggerTime, clickCount: $clickCount, hasReviewTriggered: $hasReviewTriggered")
        return if (firstTriggerTime.isEmpty() || hasReviewTriggered) {
            false
        } else {
            val days = (System.currentTimeMillis() - firstTriggerTime.toLong()) / (1000 * 60 * 60 * 24)
            days >= 7 && clickCount >= 10
        }
    }
}