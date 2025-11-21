package com.huanli233.bilizepam.ui.screens.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.bilizepam.data.repository.VideoRepository
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.biliwebapi.api.interfaces.IVideoApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    private var progressReportJob: Job? = null
    private var lastReportedProgress: Long = 0
    
    fun loadVideoInfo(aid: Long) {
        viewModelScope.launch {
            try {
                val videoInfoResult = bilibiliApi.api(IVideoApi::class) {
                    getVideoInfo(aid = aid)
                }.apiResultNonNull()
                
                val videoInfo = videoInfoResult.getOrNull()
                if (videoInfo != null) {
                    val pages = videoInfo.pages.mapIndexed { index, page ->
                        VideoPage(
                            cid = page.cid,
                            page = page.page,
                            part = page.part
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        pages = pages,
                        title = videoInfo.title
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun loadVideo(aid: Long, cid: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val defaultQuality = LocalData.settingsStateFlow.value?.playerSettings?.defaultQuality ?: 64
                
                val playUrlResult = bilibiliApi.api(IVideoApi::class) {
                    getPlayUrl(aid = aid, cid = cid, qn = defaultQuality)
                }.apiResultNonNull()
                
                val playUrlData = playUrlResult.getOrNull()
                
                if (playUrlData != null) {
                    val videoUrl = playUrlData.durl?.firstOrNull()?.url ?: ""
                    val danmakuUrl = "https://comment.bilibili.com/$cid.xml"
                    
                    val currentPageIndex = _uiState.value.pages.indexOfFirst { it.cid == cid }
                    
                    val playerInfo = videoRepository.getPlayerInfo(aid, cid).getOrNull()
                    val historyProgress = if (playerInfo?.lastPlayCid == cid) {
                        playerInfo.lastPlayTime
                    } else {
                        0L
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        videoUrl = videoUrl,
                        danmakuUrl = danmakuUrl,
                        aid = aid,
                        cid = cid,
                        currentPage = currentPageIndex,
                        historyProgress = historyProgress
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load video"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    suspend fun createDanmakuParser(danmakuUrl: String): BaseDanmakuParser? {
        _uiState.value = _uiState.value.copy(isLoadingDanmaku = true, danmakuError = null)
        return withContext(Dispatchers.IO) {
            try {
                val parser = BiliDanmukuParser()

                val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
                if (loader == null) {
                    Log.e("Danmaku", "Failed to create danmaku loader")
                    return@withContext null
                }

                // B站的弹幕数据是 deflate 压缩的（不带 zlib header），需要先解压
                val rawInputStream = URL(danmakuUrl).openStream()
                val inflater = java.util.zip.Inflater(true)
                val inflaterInputStream = java.util.zip.InflaterInputStream(rawInputStream, inflater)
                
                loader.load(inflaterInputStream)

                val dataSource = loader.dataSource
                if (dataSource == null) {
                    Log.e("Danmaku", "Failed to get danmaku data source")
                    return@withContext null
                }

                parser.load(dataSource)
                return@withContext parser
            } catch (e: Exception) {
                val errorMsg = "Error loading danmaku: ${e.message}"
                Log.e("Danmaku", errorMsg, e)
                _uiState.value = _uiState.value.copy(danmakuError = errorMsg)
                return@withContext null
            } finally {
                _uiState.value = _uiState.value.copy(isLoadingDanmaku = false)
            }
        }
    }
    
    fun toggleDanmaku() {
        _uiState.value = _uiState.value.copy(
            isDanmakuVisible = !_uiState.value.isDanmakuVisible
        )
    }
    
    fun startProgressReporting(getCurrentPosition: () -> Long) {
        progressReportJob?.cancel()
        progressReportJob = viewModelScope.launch {
            while (isActive) {
                delay(15000)
                val currentPosition = getCurrentPosition()
                if (currentPosition > 0) {
                    val progressInSeconds = currentPosition / 1000
                    if (progressInSeconds != lastReportedProgress) {
                        lastReportedProgress = progressInSeconds
                        reportProgress(progressInSeconds)
                    }
                }
            }
        }
    }
    
    fun stopProgressReporting() {
        progressReportJob?.cancel()
        progressReportJob = null
    }
    
    fun reportFinalProgress(currentPosition: Long) {
        val aid = _uiState.value.aid
        val cid = _uiState.value.cid
        if (aid != 0L && cid != 0L && currentPosition > 0) {
            viewModelScope.launch {
                val progressInSeconds = currentPosition / 1000
                reportProgress(progressInSeconds)
            }
        }
    }
    
    private suspend fun reportProgress(progressInSeconds: Long) {
        val aid = _uiState.value.aid
        val cid = _uiState.value.cid
        if (aid != 0L && cid != 0L) {
            try {
                videoRepository.reportHistory(aid, cid, progressInSeconds)
                Log.d("PlayerViewModel", "Progress reported: aid=$aid, cid=$cid, progress=$progressInSeconds")
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Failed to report progress", e)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopProgressReporting()
    }
}

data class PlayerUiState(
    val isLoading: Boolean = false,
    val isLoadingDanmaku: Boolean = false,
    val danmakuError: String? = null,
    val error: String? = null,
    val videoUrl: String = "",
    val danmakuUrl: String = "",
    val title: String = "",
    val aid: Long = 0,
    val cid: Long = 0,
    val pages: List<VideoPage> = emptyList(),
    val currentPage: Int = 0,
    val isDanmakuVisible: Boolean = true,
    val historyProgress: Long = 0
)

data class VideoPage(
    val cid: Long,
    val page: Int,
    val part: String
)
