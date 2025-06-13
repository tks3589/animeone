package com.aaron.chen.animeone.app.model.repository.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.aaron.chen.animeone.app.model.data.bean.AnimeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.responsevo.toAnimeList
import com.aaron.chen.animeone.app.model.data.responsevo.toTimeLine
import com.aaron.chen.animeone.app.model.repository.api.impl.IAnimeoneApiModel
import com.aaron.chen.animeone.module.retrofit.IRetrofitApi
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import com.aaron.chen.animeone.utils.AnimeSeason.getSeasonTitle
import com.aaron.chen.animeone.utils.HtmlUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Factory(binds = [IAnimeoneApiModel::class])
class AnimeoneApiModel: IAnimeoneApiModel, KoinComponent {
    private val apiModel: IRetrofitApi by inject()
    private val requestTag = this::class.java.simpleName
    private val animeListUrl = "https://d1zquzjgwo9yb.cloudfront.net/"

    override fun getAnimeList(): Flow<List<AnimeBean>> {
        return apiModel.getAnimeList(requestTag, animeListUrl).map { vo ->
            vo.toAnimeList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAnimeSeasonTimeLine(): Flow<AnimeSeasonTimeLineBean> {
        val url = RetrofitModule.BASE_URL + getSeasonTitle()
        return apiModel.getAnimeSeasonTimeLine(requestTag, url).map { html ->
            HtmlUtils.toAnimeTimeLineRespVo(html).toTimeLine()
        }
    }

    override fun getAnimeEpisodes(animeId: String): Flow<List<AnimeEpisodeBean>> {
        val url = RetrofitModule.BASE_URL + "?cat=" + animeId
        return apiModel.getAnimeEpisodes(requestTag, url).map { html ->
            HtmlUtils.toAnimeEpisodeList(html)
        }
    }
}