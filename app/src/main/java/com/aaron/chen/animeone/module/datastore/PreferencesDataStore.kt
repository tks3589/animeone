package com.aaron.chen.animeone.module.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val PREF_NAME_MXANIME = "PREF_MXANIME"

val Context.mxAnimePrefDataStore by preferencesDataStore(
    name = PREF_NAME_MXANIME
)