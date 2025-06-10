package com.aaron.chen.animeone.app.model.repository.impl

import androidx.paging.PagingData
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow

interface IAnimeoneRepository {
    fun requestAnimes(): Flow<PagingData<AnimeEntity>>
}