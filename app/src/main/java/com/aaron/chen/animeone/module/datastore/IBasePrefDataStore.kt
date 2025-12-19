package com.aaron.chen.animeone.module.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.*

interface IBasePrefDataStore {
    private val logTag: String
        get() = IBasePrefDataStore::class.java.simpleName
    val dataStore: DataStore<Preferences>

    fun <T>writeValueFlow(key: Preferences.Key<T>, value: T): Flow<Unit> {
        return flow {
            dataStore.edit {
                it[key] = value
            }
            emit(Unit)
        }.catch { e ->
            Log.w(logTag,  "[writeValueFlow] Write ${key.name} failed")
            emit(Unit)
        }
    }

    fun <T>readValueFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return flow {
            val result = dataStore.data.firstOrNull()?.get(key) ?: defaultValue
            emit(result)
        }.catch { e ->
            Log.w(logTag,  "[readValueFlow] Read ${key.name} failed")
            emit(defaultValue)
        }
    }

    fun clearFlow(): Flow<Unit> {
        return flow {
            dataStore.edit { it.clear() }
            emit(Unit)
        }.catch { e ->
            Log.w(logTag,  "[clearFlow] Clear failed")
            emit(Unit)
        }
    }
}