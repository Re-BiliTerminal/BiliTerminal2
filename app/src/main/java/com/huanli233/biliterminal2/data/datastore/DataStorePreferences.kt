package com.huanli233.biliterminal2.data.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.data.IPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DataStorePreferences: IPreferences {

    val dataStore = applicationContext.dataStore

    override fun <T> get(key: Preferences.Key<T>): T? =
        runBlocking(Dispatchers.IO) {
            runCatching {
                dataStore.data.first()[key]
            }.getOrNull()
        }

    override suspend fun <T> set(key: Preferences.Key<T>, value: T?) {
        dataStore.edit { preferences ->
            value?.let { preferences[key] = it }?: preferences.remove(key)
        }
    }

    override fun data() = dataStore.data

}