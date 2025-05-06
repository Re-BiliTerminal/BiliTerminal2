package com.huanli233.biliterminal2.data.setting

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.huanli233.biliterminal2.applicationContext
import com.huanli233.biliterminal2.applicationScope
import com.huanli233.biliterminal2.data.proto.AppSettings
import com.huanli233.biliterminal2.data.proto.NightMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

private const val APP_SETTINGS_FILE_NAME = "app_settings.pb"

val Context.appSettingsDataStore: DataStore<AppSettings> by dataStore(
    fileName = APP_SETTINGS_FILE_NAME,
    serializer = AppSettingsSerializer
)

fun NightMode.toSystemValue() = when (this) {
    NightMode.NIGHT_MODE_AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    NightMode.NIGHT_MODE_NIGHT -> AppCompatDelegate.MODE_NIGHT_YES
    NightMode.NIGHT_MODE_DAY -> AppCompatDelegate.MODE_NIGHT_NO
    else -> -1
}

inline fun AppSettings.edit(
    block: AppSettings.Builder.() -> Unit
): AppSettings = toBuilder().apply(block).build()

object DataStore {

    private val dataStore by lazy {
        applicationContext.appSettingsDataStore
    }

    private val appSettingsFlow: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(AppSettingsSerializer.defaultValue)
            } else {
                throw exception
            }
        }

    val appSettingsStateFlow: StateFlow<AppSettings?> by lazy {
        appSettingsFlow
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = null
            )
    }

    val appSettings: AppSettings
        get() = appSettingsStateFlow.value ?: runBlocking { appSettingsFlow.first() }

    suspend fun updateData(transform: suspend (settings: AppSettings) -> AppSettings): AppSettings {
        return dataStore.updateData { transform(it) }
    }

    suspend inline fun editData(crossinline transform: AppSettings.Builder.() -> Unit): AppSettings {
        return updateData {
            it.edit(transform)
        }
    }

}