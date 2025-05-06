package com.huanli233.biliterminal2.data.setting

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.huanli233.biliterminal2.data.proto.AppSettings
import com.google.protobuf.InvalidProtocolBufferException
import com.huanli233.biliterminal2.data.menu.MenuConfig
import com.huanli233.biliterminal2.data.proto.NightMode
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings.newBuilder().apply {
        activeAccountId = 0L
        firstRun = true

        roundMode = false
        uiScale = 1.0f
        uiPaddingHorizontal = 0
        uiPaddingVertical = 0
        density = 0
        nightMode = NightMode.NIGHT_MODE_NIGHT

        animationsEnabled = true

        wbiMixinKey = ""
        wbiLastUpdated = 0L

        backDisabled = false
        snackbarEnabled = true
        asyncInflateEnabled = false
        marqueeEnabled = true
        stopLoadImageWhileScrolling = false
        gridListEnabled = false

        menuConfig = MenuConfig().toString()
    }.build()

    override suspend fun readFrom(input: InputStream): AppSettings {
        try {
            return AppSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        t.writeTo(output)
    }
}