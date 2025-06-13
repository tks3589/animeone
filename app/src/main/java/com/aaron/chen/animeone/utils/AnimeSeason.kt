package com.aaron.chen.animeone.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

object AnimeSeason {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSeasonTitle(date: LocalDate = LocalDate.now()): String {
        val year = date.year
        val month = date.monthValue
        val day = date.dayOfMonth

        val season = when {
            (month == 1 && day >= 2) || (month == 2) || (month == 3) || (month == 4 && day < 2) -> 0 // 冬
            (month == 4 && day >= 2) || (month == 5) || (month == 6) || (month == 7 && day < 2) -> 1 // 春
            (month == 7 && day >= 2) || (month == 8) || (month == 9) || (month == 10 && day < 2) -> 2 // 夏
            else -> 3 // 秋
        }

        val seasonNames = listOf("冬季", "春季", "夏季", "秋季")

        return "${year}年${seasonNames[season]}新番"
    }
}