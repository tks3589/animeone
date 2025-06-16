package com.aaron.chen.animeone.app.model.repository.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.aaron.chen.animeone.app.model.data.bean.AnimeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeSeasonTimeLineBean
import com.aaron.chen.animeone.app.model.data.bean.AnimeVideoBean
import com.aaron.chen.animeone.app.model.data.responsevo.toAnimeList
import com.aaron.chen.animeone.app.model.data.responsevo.toTimeLine
import com.aaron.chen.animeone.app.model.data.responsevo.toVideo
import com.aaron.chen.animeone.app.model.repository.api.impl.IAnimeoneApiModel
import com.aaron.chen.animeone.module.retrofit.IRetrofitApi
import com.aaron.chen.animeone.module.retrofit.RetrofitModule
import com.aaron.chen.animeone.utils.AnimeSeason.getSeasonTitle
import com.aaron.chen.animeone.utils.HtmlUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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

    override fun requestAnimeVideo(dataRaw: String): Flow<AnimeVideoBean> {
        val url = RetrofitModule.VIDEO_API_URL
        val bodyString = "d=$dataRaw"
        val requestBody = bodyString.toRequestBody("application/x-www-form-urlencoded".toMediaType())
        return apiModel.requestAnimeVideo(requestTag, url, requestBody).map { response ->
            val allCookies= response.headers().values("Set-Cookie")
            val filteredCookie = allCookies.mapNotNull { cookie ->
                val parts = cookie.toString().split(";")[0].split("=")
                if (parts.size == 2) {
                    val key = parts[0]
                    val value = parts[1]
                    if (key == "e" || key == "p" || key == "h" || key.startsWith("_ga")) {
                        "$key=$value"
                    } else {
                        null
                    }
                } else {
                    null
                }
            }.joinToString("; ")

            if (filteredCookie.isEmpty() == true) {
                throw Exception("No valid cookies found")
            } else {
                response.body()?.toVideo(filteredCookie.toString()) ?: throw Exception("Response body is null")
            }
        }
    }
}