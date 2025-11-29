package com.huanli233.bilizepam.ui.screens.player

import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
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
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.net.URL
import javax.inject.Inject

private const val BILIBILI_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"
private const val BILIBILI_REFERER = "https://bilibili.com"

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    private var progressReportJob: Job? = null
    private var lastReportedProgress: Long = 0

    val ijkPlayer: IjkMediaPlayer = IjkMediaPlayer().apply {
        Log.d("PlayerViewModel", "Initializing IjkMediaPlayer")
        
        // 基础配置
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", BILIBILI_USER_AGENT)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 30000000)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http_persistent", 1)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0)
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek")
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "file,http,https,tcp,tls,crypto")
        setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0)
        
        // 播放器配置
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 3000)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1)
        
        // 添加渲染相关选项解决绿屏问题
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "vout", "android")
        setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
        
        // 根据设置配置解码器
        val settings = LocalData.settingsStateFlow.value?.playerSettings
        if (settings?.useSoftwareDecoder == true) {
            Log.d("PlayerViewModel", "Using software decoder")
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0)
        } else {
            Log.d("PlayerViewModel", "Using hardware decoder")
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1)
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-sync", 0)
        }
        
        // 根据 Surface 类型设置渲染格式
        if (settings?.useTextureView == true) {
            Log.d("PlayerViewModel", "Using TextureView - letting system choose optimal format")
            // TextureView 通常与 RGB 格式兼容性更好，让系统自动选择
        } else {
            Log.d("PlayerViewModel", "Using SurfaceView - setting YV12 format")
            setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_YV12.toLong())
        }
        
        // 设置监听器
        setOnPreparedListener { player ->
            Log.d("PlayerViewModel", "Player prepared")
            val autoPlay = LocalData.settingsStateFlow.value?.playerSettings?.autoPlay ?: false
            if (autoPlay) {
                // 延迟一点时间确保 Surface 已经设置
                viewModelScope.launch {
                    delay(200)
                    if (player.isPlayable) {
                        Log.d("PlayerViewModel", "Auto-starting playback")
                        player.start()
                    }
                }
            }
        }
        
        setOnVideoSizeChangedListener { _, width, height, _, _ ->
            Log.d("PlayerViewModel", "Video size changed: ${width}x${height}")
            if (width > 0 && height > 0) {
                val aspectRatio = width.toFloat() / height.toFloat()
                _uiState.value = _uiState.value.copy(videoAspectRatio = aspectRatio)
            }
        }
        
        setOnErrorListener { _, what, extra ->
            Log.e("PlayerViewModel", "Player error: what=$what, extra=$extra")
            false
        }
        
        setOnInfoListener { _, what, extra ->
            when (what) {
                IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    Log.d("PlayerViewModel", "First frame rendered")
                }
                IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    Log.d("PlayerViewModel", "Buffering start")
                }
                IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    Log.d("PlayerViewModel", "Buffering end")
                }
            }
            false
        }
    }
    
    fun playVideo(videoUrl: String) {
        if (videoUrl.isEmpty()) return
        
        viewModelScope.launch {
            try {
                Log.d("PlayerViewModel", "Playing video: $videoUrl")
                
                // 停止当前播放
                if (ijkPlayer.isPlaying) {
                    ijkPlayer.stop()
                }
                
                // 设置数据源和请求头
                val headers = mapOf("Referer" to BILIBILI_REFERER)
                ijkPlayer.setDataSource(videoUrl, headers)
                ijkPlayer.prepareAsync()
                
                Log.d("PlayerViewModel", "Video prepared for playback")
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error playing video", e)
            }
        }
    }
    
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
                
                val defaultQuality = LocalData.settings.playerSettings?.defaultQuality ?: 64
                
                // 首先获取所有可用清晰度信息
                val qualityInfoResult = bilibiliApi.api(IVideoApi::class) {
                    getPlayUrl(aid = aid, cid = cid, qn = defaultQuality, fnval = 4048)
                }.apiResultNonNull()
                
                val qualityInfoData = qualityInfoResult.getOrNull()
                
                if (qualityInfoData != null) {
                    // 解析可用清晰度列表
                    val availableQualities = mutableListOf<VideoQuality>()
                    val acceptQuality = qualityInfoData.acceptQuality ?: emptyList()
                    val acceptDescription = qualityInfoData.acceptDescription ?: emptyList()
                    
                    for (i in acceptQuality.indices) {
                        if (i < acceptDescription.size) {
                            availableQualities.add(
                                VideoQuality(
                                    qn = acceptQuality[i],
                                    description = acceptDescription[i]
                                )
                            )
                        }
                    }
                    
                    // 如果没有获取到清晰度列表，使用默认列表
                    if (availableQualities.isEmpty()) {
                        availableQualities.addAll(getDefaultQualities())
                    }
                    
                    // 获取实际视频URL
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
                            historyProgress = historyProgress,
                            availableQualities = availableQualities,
                            currentQuality = defaultQuality
                        )
                        
                        // 播放视频
                        playVideo(videoUrl)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load video"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to get quality info"
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
    
    private fun getDefaultQualities(): List<VideoQuality> {
        return listOf(
            VideoQuality(16, "360P 流畅"),
            VideoQuality(32, "480P 清晰"),
            VideoQuality(64, "720P 高清"),
            VideoQuality(74, "720P60 高帧率"),
            VideoQuality(80, "1080P 高清"),
            VideoQuality(112, "1080P+ 高码率"),
            VideoQuality(116, "1080P60 高帧率"),
            VideoQuality(120, "4K 超清")
        )
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
    
    fun changeQuality(quality: Int) {
        val currentState = _uiState.value
        if (currentState.aid > 0 && currentState.cid > 0) {
            _uiState.value = currentState.copy(currentQuality = quality)
            // 重新加载视频，使用新的清晰度
            loadVideoWithQuality(currentState.aid, currentState.cid, quality)
        }
    }
    
    private fun loadVideoWithQuality(aid: Long, cid: Long, quality: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val playUrlResult = bilibiliApi.api(IVideoApi::class) {
                    getPlayUrl(aid = aid, cid = cid, qn = quality)
                }.apiResultNonNull()
                
                val playUrlData = playUrlResult.getOrNull()
                
                if (playUrlData != null) {
                    val videoUrl = playUrlData.durl?.firstOrNull()?.url ?: ""
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        videoUrl = videoUrl,
                        currentQuality = quality
                    )
                    
                    Log.d("PlayerViewModel", "Quality changed to $quality, new URL: $videoUrl")
                } else {
                    // 如果当前清晰度不可用，尝试降级
                    val fallbackQualities = listOf(64, 32, 16) // 720P -> 480P -> 360P
                    val nextQuality = fallbackQualities.find { it < quality }
                    
                    if (nextQuality != null) {
                        Log.w("PlayerViewModel", "Quality $quality not available, trying $nextQuality")
                        loadVideoWithQuality(aid, cid, nextQuality)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No available quality found"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Failed to change quality", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to change quality: ${e.message}"
                )
            }
        }
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
        ijkPlayer.release()
        Log.d("PlayerViewModel", "IjkMediaPlayer released")
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
    val historyProgress: Long = 0,
    val availableQualities: List<VideoQuality> = emptyList(),
    val currentQuality: Int = 64,
    val videoAspectRatio: Float = 16f / 9f
)

data class VideoPage(
    val cid: Long,
    val page: Int,
    val part: String
)

data class VideoQuality(
    val qn: Int,
    val description: String
)
