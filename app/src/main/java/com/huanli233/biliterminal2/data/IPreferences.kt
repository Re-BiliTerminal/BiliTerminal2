package com.huanli233.biliterminal2.data

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface IPreferences {
    fun <T> get(key: Preferences.Key<T>): T?
    suspend fun <T> set(
        key: Preferences.Key<T>,
        value: T?
    )
    fun data(): Flow<Preferences>
}