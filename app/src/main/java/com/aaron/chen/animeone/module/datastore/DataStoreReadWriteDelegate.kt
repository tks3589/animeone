package com.aaron.chen.animeone.module.datastore

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

class DataStoreReadWriteDelegate<T>(
    private val basePrefDataStore: IBasePrefDataStore,
    private val key: Preferences.Key<T>,
    private val defaultValue: T
) {
    fun readFlow(): Flow<T> {
        return basePrefDataStore.readValueFlow(key, defaultValue)
    }
    fun writeFlow(value: T): Flow<Unit> {
        return basePrefDataStore.writeValueFlow(key, value)
    }
}