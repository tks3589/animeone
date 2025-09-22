package com.aaron.chen.animeone.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(isoString: String): String {
        try {
            val dateTime = LocalDateTime.parse(isoString) // 解析 ISO 字串
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
            return dateTime.format(formatter)
        }catch (exception: Exception) {
            // 如果解析失敗，返回原始字串
            return isoString
        }
    }
}