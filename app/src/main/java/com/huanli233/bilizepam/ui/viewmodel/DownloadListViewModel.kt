package com.huanli233.bilizepam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.data.download.DownloadManager
import com.huanli233.bilizepam.data.download.DownloadEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadListViewModel @Inject constructor(
    private val downloadManager: DownloadManager
) : ViewModel() {

    val downloads: StateFlow<List<DownloadEntity>> =
        downloadManager.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cancel(downloadId: Long) {
        viewModelScope.launch {
            downloadManager.cancel(downloadId)
        }
    }

    fun retry(downloadId: Long) {
        viewModelScope.launch {
            downloadManager.retry(downloadId)
        }
    }

    fun delete(downloadId: Long, deleteFile: Boolean) {
        viewModelScope.launch {
            downloadManager.delete(downloadId, deleteFile)
        }
    }
}
