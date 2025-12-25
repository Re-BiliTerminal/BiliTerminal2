package com.huanli233.bilizepam.data.download

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "downloads",
    indices = [Index(value = ["key"], unique = true)]
)
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val url: String,
    val headersJson: String? = null,
    val coverUrl: String? = null,
    val fileName: String,
    val mimeType: String? = null,
    val contentUri: String? = null,
    val status: DownloadStatus = DownloadStatus.ENQUEUED,
    val progress: Int = 0,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val errorMessage: String? = null,
    val workId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null
)
