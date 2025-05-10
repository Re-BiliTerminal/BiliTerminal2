package com.huanli233.biliterminal2.data.setting

import android.R.string
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.InvalidProtocolBufferException
import com.huanli233.biliterminal2.data.menu.MenuConfig
import com.huanli233.biliterminal2.data.proto.ApiCache
import com.huanli233.biliterminal2.data.proto.AppSettings
import com.huanli233.biliterminal2.data.proto.ImageFormat
import com.huanli233.biliterminal2.data.proto.NightMode
import com.huanli233.biliterminal2.data.proto.Preferences
import com.huanli233.biliterminal2.data.proto.Theme
import com.huanli233.biliterminal2.data.proto.UiSettings
import com.huanli233.biliterminal2.utils.ThemeUtil
import java.io.InputStream
import java.io.OutputStream


inline fun <M : GeneratedMessageLite<M, B>, B : GeneratedMessageLite.Builder<M, B>> GeneratedMessageLite<M, B>.edit(
    block: B.() -> Unit
): M = toBuilder().apply(block).build()

inline fun <M : GeneratedMessageLite<M, B>, B : GeneratedMessageLite.Builder<M, B>> B.build(
    block: B.() -> Unit
): M = apply(block).build()

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings.newBuilder().build {
        activeAccountId = 0L
        firstRun = true

        uiSettings = UiSettings.newBuilder().build {
            roundMode = false
            uiScale = 1.0f
            uiPaddingHorizontal = 0
            uiPaddingVertical = 0
            density = 0
            snackbarEnabled = true
            marqueeEnabled = true
            gridListEnabled = false
        }
        theme = Theme.newBuilder().build {
            nightMode = NightMode.NIGHT_MODE_NIGHT
            followSystemAccent = true
            colorTheme = ThemeUtil.THEME_DEFAULT
            animationsEnabled = true
            fullScreenDialogDisabled = false
        }
        apiCache = ApiCache.newBuilder().build {
            wbiMixinKey = ""
            wbiLastUpdated = 0L
        }
        preferences = Preferences.newBuilder().build {
            backDisabled = false
            stopLoadImageWhileScrolling = false
            imageFormat = ImageFormat.IMAGE_FORMAT_JPEG
            asyncInflateEnabled = false
        }
        menuConfig = MenuConfig().toString()
    }

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