package com.huanli233.bilizepam.data.download

data class DownloadRequest(
    val key: String,
    val url: String,
    val headersJson: String? = null,
    val coverUrl: String? = null,
    val fileName: String,
    val mimeType: String? = null
)
