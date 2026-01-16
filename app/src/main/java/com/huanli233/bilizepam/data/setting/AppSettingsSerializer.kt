package com.huanli233.bilizepam.data.setting

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.InvalidProtocolBufferException
import com.huanli233.bilizepam.data.menu.MenuConfig
import com.huanli233.bilizepam.data.proto.ApiCache
import com.huanli233.bilizepam.data.proto.AppSettings
import com.huanli233.bilizepam.data.proto.ImageFormat
import com.huanli233.bilizepam.data.proto.NightMode
import com.huanli233.bilizepam.data.proto.PlayerSettings
import com.huanli233.bilizepam.data.proto.Preferences
import com.huanli233.bilizepam.data.proto.Theme
import com.huanli233.bilizepam.data.proto.UiSettings
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
            videoCardBackgroundStyle = false
            userProfileBackgroundEnabled = false
        }
        theme = Theme.newBuilder().build {
            nightMode = NightMode.NIGHT_MODE_NIGHT
            followSystemAccent = true
            colorTheme = "DEFAULT"
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
        playerSettings = PlayerSettings.newBuilder().build {
            useSoftwareDecoder = false
            autoPlay = true
            defaultQuality = 64
            useTextureView = false
            defaultDanmakuEnabled = true
            defaultSpeed = 1.0f
            rememberDanmakuEnabled = false
            rememberSpeed = false
            danmakuFontSize = 16f
            danmakuMaxCount = 50
            danmakuScrollEnabled = true
            danmakuTopEnabled = true
            danmakuBottomEnabled = true
            danmakuAdvancedEnabled = true
            danmakuTransparency = 0.8f
            danmakuScrollSpeed = 1.0f
            danmakuStrokeWidth = 3f
            danmakuMergeDuplicate = true
            danmakuBold = false
            danmakuAreaTop = 0.0f
            danmakuAreaBottom = 0.0f
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