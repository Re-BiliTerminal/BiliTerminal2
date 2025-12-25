package com.huanli233.bilizepam.data.download

import androidx.room.TypeConverter

class DownloadTypeConverters {
    @TypeConverter
    fun toDownloadStatus(value: String): DownloadStatus {
        return DownloadStatus.valueOf(value)
    }

    @TypeConverter
    fun fromDownloadStatus(value: DownloadStatus): String {
        return value.name
    }
}
