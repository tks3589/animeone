package com.aaron.chen.animeone.module.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.aaron.chen.animeone.constant.DefaultConst
import org.koin.core.annotation.Factory

@Factory(binds = [IAnimePrefRepository::class])
class AnimePrefRepository(context: Context): IAnimePrefRepository {
    override val dataStore: DataStore<Preferences> = context.mxAnimePrefDataStore

    companion object PreferencesKeys {
        private val PREF_KEY_ANIME_LIST_CLICK_COUNT = intPreferencesKey("anime_list_click_count")
        private val PREF_KEY_LAST_REVIEW_TRIGGER_TIME = stringPreferencesKey("last_review_trigger_time")
        private val PREF_KEY_HAS_REVIEW_INVITE_TRIGGERED = booleanPreferencesKey("has_review_invite_triggered")
    }

    override val animeListClickCount: DataStoreReadWriteDelegate<Int> = DataStoreReadWriteDelegate(this, PREF_KEY_ANIME_LIST_CLICK_COUNT, DefaultConst.INT_COUNT)
    override val lastReviewTriggerTime: DataStoreReadWriteDelegate<String> = DataStoreReadWriteDelegate(this, PREF_KEY_LAST_REVIEW_TRIGGER_TIME, DefaultConst.EMPTY_STRING)
    override val hasReviewInviteTriggered: DataStoreReadWriteDelegate<Boolean> = DataStoreReadWriteDelegate(this, PREF_KEY_HAS_REVIEW_INVITE_TRIGGERED, false)
}