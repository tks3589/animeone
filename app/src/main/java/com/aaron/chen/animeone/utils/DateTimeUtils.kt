package com.aaron.chen.animeone.utils

import android.util.Log
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTimeUtils {
    fun formatDate(isoString: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        return try {
            val dateTime = OffsetDateTime.parse(isoString)
            dateTime.format(formatter)
        } catch (e: DateTimeParseException) {
            try {
                val dateTime = LocalDateTime.parse(isoString).atOffset(ZoneOffset.ofHours(8))
                dateTime.format(formatter)
            } catch (e2: Exception) {
                Log.d("aaron_tt[DateTimeParseException]", e2.message.toString())
                isoString
            }
        } catch (e: Exception) {
            Log.d("aaron_tt[formatIsoToDateTime]", e.message.toString())
            isoString
        }
    }
}