package com.aaron.chen.animeone.module.paging.remotemediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.aaron.chen.animeone.app.model.data.bean.AnimeBean
import com.aaron.chen.animeone.app.model.repository.api.impl.IAnimeoneApiModel
import com.aaron.chen.animeone.database.DataBase
import com.aaron.chen.animeone.database.dao.AnimeDao
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalPagingApi::class)
class AnimeRemoteMediator(
    private val animeDao: AnimeDao,
    private val animeApiModel: IAnimeoneApiModel
): RemoteMediator<Int, AnimeEntity>(), KoinComponent {
    private val logTag = AnimeRemoteMediator::class.java.simpleName
    private var cacheId: String? = null
    private val defaultLoadKey: String? = null
    private val database: DataBase by inject()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AnimeEntity>
    ): MediatorResult {
        return try {
            loadAndCache(loadType)
        } catch (e: Throwable) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }

    private suspend fun loadAndCache(loadType: LoadType): MediatorResult.Success {
        Log.d(logTag, "[loadAndCache] loadType=$loadType")

        cacheId = when (loadType) {
            LoadType.REFRESH -> defaultLoadKey
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                cacheId ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        val commentListResp = loadDataFromRemote(loadType = loadType, cacheId = cacheId)
        cacheData(loadType, commentListResp)
        //cacheId = commentListResp.cacheId

        Log.d(logTag, "[loadAndCache] nextCacheId=$cacheId")

        return MediatorResult.Success(endOfPaginationReached = (null == cacheId))
    }

    private suspend fun cacheData(loadType: LoadType, animeList: List<AnimeBean>) {
        val removeOldData = loadType.shouldRemoveOldData()
        updateDb(data = animeList, removeOldData = removeOldData)
    }

    private suspend fun loadDataFromRemote(loadType: LoadType, cacheId: String?): List<AnimeBean> {
        return animeApiModel.getAnimeList().catch { throwable ->
            // if load type is REFRESH and catch API error, remove old data here
            if (loadType.shouldRemoveOldData()) {
                removeOldData()
            }

            throw throwable
        }.first()
    }

    private suspend fun updateDb(data: List<AnimeBean>, removeOldData: Boolean) {
        database.withTransaction {
            if (removeOldData) {
                animeDao.deleteAll()
            }

            saveDataToDb(data)
        }
    }

    private suspend fun removeOldData() {
        animeDao.deleteAll()
    }

    private suspend fun saveDataToDb(list: List<AnimeBean>) {
        val dbList = list.map {
            AnimeEntity(
                id = it.id,
                title = it.title,
                status = it.status,
                year = it.year,
                season = it.season,
                fansub = it.fansub
            )
        }

        animeDao.insertAll(dbList)
    }

    private fun LoadType.shouldRemoveOldData(): Boolean = (this == LoadType.REFRESH)
}