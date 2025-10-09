package com.aaron.chen.animeone.utils

import com.aaron.chen.animeone.app.model.data.bean.AnimeEpisodeBean
import com.aaron.chen.animeone.app.model.data.responsevo.AnimeSeasonTimeLineRespVo
import com.aaron.chen.animeone.constant.DefaultConst
import org.jsoup.Jsoup

object HtmlUtils {
    fun toAnimeSeason(html: String): String {
        val document = Jsoup.parse(html)
        return document.getElementById("menu-item-16716")?.text()?.trim() ?: DefaultConst.EMPTY_STRING
    }
    fun toAnimeEpisodeList(html: String): List<AnimeEpisodeBean> {
        val document = Jsoup.parse(html)
        val articles = document.getElementsByTag("article").reversed()
        val videoTags = document.getElementsByTag("video").reversed()
        val updateTImes = document.getElementsByTag("time").reversed()
        val result = mutableListOf<AnimeEpisodeBean>()

        if (videoTags.isEmpty()) return result

        // 取得標題（例如 "某部動畫 [01]"）
        val fullTitle = document.getElementsByClass("entry-title").firstOrNull()?.text() ?: "未知標題"
        val titlePrefix = fullTitle.replace(Regex("\\s*\\[\\d+\\]$"), DefaultConst.EMPTY_STRING) // 去除尾部集數標籤

        for ((index, video) in videoTags.withIndex()) {
            val apiReq = video.attr("data-apireq")
            val episodeNumber = index + 1
            val title = titlePrefix
            val animeId = articles[index].attr("id").substringAfter("post-")
            val updateTime = DateTimeUtils.formatDate(updateTImes[index].text())

            result.add(
                AnimeEpisodeBean(
                    id = animeId,
                    title = title,
                    updateTime = updateTime,
                    episode = episodeNumber,
                    dataApireq = apiReq,
                )
            )
        }

        return result
    }

    fun toAnimeTimeLineRespVo(html: String): AnimeSeasonTimeLineRespVo {
        val document = Jsoup.parse(html)

        val seasonTitle = document.selectFirst("h2.entry-title")?.text()

        val table = document.selectFirst("div.entry-content")
        val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")

        val timeline = mutableListOf<AnimeSeasonTimeLineRespVo.AnimeLiteRespVo>()

        val rows = table?.select("tbody > tr")?.dropLast(1)
        rows?.forEach { row ->
            val cells = row.select("td")
            for ((index, cell) in cells.withIndex()) {
                val day = if (index < daysOfWeek.size) daysOfWeek[index] else DefaultConst.EMPTY_STRING

                // 支援一格多部作品
                val links = cell.select("a[href]")
                if (links.isNotEmpty()) {
                    for (link in links) {
                        val href = link.attr("href")
                        val id = Regex("cat=(\\d+)").find(href)?.groupValues?.get(1) ?: DefaultConst.EMPTY_STRING
                        val title = link.text()

                        timeline.add(
                            AnimeSeasonTimeLineRespVo.AnimeLiteRespVo().apply {
                                this.id = id
                                this.day = day
                                this.title = title
                            }
                        )
                    }
                } else if (cell.text().isNotBlank()) {
                    val title = cell.text()
                    timeline.add(
                        AnimeSeasonTimeLineRespVo.AnimeLiteRespVo().apply {
                            this.id = DefaultConst.EMPTY_STRING
                            this.day = day
                            this.title = title
                        }
                    )
                }
            }
        }

        return AnimeSeasonTimeLineRespVo().apply {
            this.seasonTitle = seasonTitle
            this.timeline = timeline
        }
    }
}