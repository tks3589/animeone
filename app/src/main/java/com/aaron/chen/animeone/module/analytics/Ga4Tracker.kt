package com.aaron.chen.animeone.module.analytics

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object Ga4Tracker {
    private var analytics: FirebaseAnalytics? = null

    fun init(application: Application) {
        analytics = FirebaseAnalytics.getInstance(application)
    }

    private fun logEvent(event: String, params: Bundle.() -> Unit = {}) {
        val bundle = Bundle().apply(params)
        analytics?.logEvent(event, bundle)
    }

    /* -------------------- App -------------------- */

    fun trackAppOpen() {
        logEvent("app_open")
    }

    fun trackScreen(screenName: String) {
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    /* -------------------- Anime -------------------- */

    enum class ANIME_PLAY_TYPE{
        ANIME_PLAYING,
        ANIME_PAUSE,
        ANIME_COMPLETE
    }

    fun trackOpenAnime(title: String) {
        logEvent("open_anime") {
            putString("title", title)
        }
    }

    fun trackPlayAnimeStatus(type: ANIME_PLAY_TYPE, title: String, episode: Int) {
        val eventName = when (type) {
            ANIME_PLAY_TYPE.ANIME_PLAYING -> "play_anime"
            ANIME_PLAY_TYPE.ANIME_PAUSE -> "pause_anime"
            ANIME_PLAY_TYPE.ANIME_COMPLETE -> "complete_anime"
        }
        logEvent(eventName) {
            putString("title", title)
            putInt("episode", episode)
        }
    }

    /* -------------------- Search -------------------- */

    fun trackSearch(keyword: String) {
        logEvent("search_anime") {
            putString("keyword", keyword)
        }
    }

    /* -------------------- Download -------------------- */

    fun trackDownloadAnime(title: String, episode: Int) {
        logEvent("download_anime") {
            putString("title", title)
            putInt("episode", episode)
        }
    }

    /* -------------------- Favorite -------------------- */

    fun trackFavoriteAnime(title: String, episode: Int, isFavorite: Boolean) {
        logEvent("favorite_anime") {
            putString("title", title)
            putInt("episode", episode)
            putBoolean("isFavorite", isFavorite)
        }
    }
}