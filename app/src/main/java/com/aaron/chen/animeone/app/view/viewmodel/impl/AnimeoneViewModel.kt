package com.aaron.chen.animeone.app.view.viewmodel.impl

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.aaron.chen.animeone.app.model.repository.impl.IAnimeoneRepository
import com.aaron.chen.animeone.app.view.viewmodel.IAnimeoneViewModel
import com.aaron.chen.animeone.database.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow
import org.koin.android.annotation.KoinViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinViewModel
class AnimeoneViewModel: ViewModel(), IAnimeoneViewModel, KoinComponent {
    private val animeRepository: IAnimeoneRepository by inject()
    override fun requestAnimes(): Flow<PagingData<AnimeEntity>> {
        return animeRepository.requestAnimes()
    }
}