package com.aaron.chen.animeone.app.model.deserializer

import com.aaron.chen.animeone.app.model.data.responsevo.AnimeListRespVo
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class AnimeListRespVoDeserializer : JsonDeserializer<AnimeListRespVo> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AnimeListRespVo {
        val rawList = json.asJsonArray

        val animeList = rawList.map { row ->
            val array = row.asJsonArray
            AnimeListRespVo.AnimeRespVo().apply {
                id = array.getOrNull(0)?.asString.orEmpty()
                title = array.getOrNull(1)?.asString.orEmpty()
                status = array.getOrNull(2)?.asString.orEmpty()
                year = array.getOrNull(3)?.asString.orEmpty()
                season = array.getOrNull(4)?.asString.orEmpty()
                fansub = array.getOrNull(5)?.asString.orEmpty()
            }
        }

        return AnimeListRespVo().apply {
            // 用反射或改寫 AnimeListRespVo 結構來填入 animes
            val field = AnimeListRespVo::class.java.getDeclaredField("animes")
            field.isAccessible = true
            field.set(this, animeList)
        }
    }

    private fun JsonArray.getOrNull(index: Int): JsonElement? {
        return if (index in 0 until this.size()) this[index] else null
    }
}
