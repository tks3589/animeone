package com.aaron.chen.animeone.utils

import com.aaron.chen.animeone.app.model.data.responsevo.AnimeSeasonTimeLineRespVo
import org.jsoup.Jsoup

object HtmlUtils {
    fun toAnimeTimeLineRespVo(html: String): AnimeSeasonTimeLineRespVo {
        val document = Jsoup.parse(html)

        val seasonTitle = document.selectFirst("h2.entry-title")?.text()

        val table = document.selectFirst("div.entry-content")
        val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")

        val timeline = mutableListOf<AnimeSeasonTimeLineRespVo.AnimeLiteRespVo>()

        val rows = table?.select("tbody > tr")
        rows?.forEach { row ->
            val cells = row.select("td")
            for ((index, cell) in cells.withIndex()) {
                val day = if (index < daysOfWeek.size) daysOfWeek[index] else ""

                // 支援一格多部作品
                val links = cell.select("a[href]")
                if (links.isNotEmpty()) {
                    for (link in links) {
                        val href = link.attr("href")
                        val id = Regex("cat=(\\d+)").find(href)?.groupValues?.get(1) ?: ""
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
                    // 處理純文字動畫（無 <a> 的情況，例如 "戰隊大失格"）
                    val title = cell.text()
                    timeline.add(
                        AnimeSeasonTimeLineRespVo.AnimeLiteRespVo().apply {
                            this.id = ""  // 沒有 ID 可抓
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