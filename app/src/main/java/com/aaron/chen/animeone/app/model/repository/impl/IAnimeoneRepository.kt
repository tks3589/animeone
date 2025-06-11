package com.aaron.chen.animeone.app.model.repository.impl

import androidx.paging.PagingData
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow

interface IAnimeoneRepository {
    companion object {
        const val PAGE_SIZE = 50
        const val INITIAL_LOAD_SIZE = PAGE_SIZE * 2
    }
    fun requestAnimes(): Flow<PagingData<AnimeEntity>>
}