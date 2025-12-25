package com.huanli233.bilizepam.ui.screens.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.api.apiResult
import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.bilizepam.data.download.DownloadManager
import com.huanli233.bilizepam.data.download.DownloadRequest
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.dialog.VideoPage
import com.huanli233.biliwebapi.api.interfaces.IVideoApi
import com.huanli233.biliwebapi.bean.video.FavoriteFolder
import com.huanli233.biliwebapi.bean.video.Tag
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.biliwebapi.bean.video.VideoRelation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val videoInfo: VideoInfo? = null,
    val tags: List<Tag> = emptyList(),
    val relation: VideoRelation? = null,
    val isLiking: Boolean = false,
    val favoriteFolders: List<FavoriteFolder> = emptyList(),
    val isLoadingFolders: Boolean = false
)

sealed interface VideoDetailEvent {
    data class LikeSuccess(val action: Int) : VideoDetailEvent
    data class LikeFailed(val message: String?) : VideoDetailEvent
    data object NotLoggedIn : VideoDetailEvent
    data object CoinSuccess : VideoDetailEvent
    data object FavoriteSuccess : VideoDetailEvent
    data object WatchLaterSuccess : VideoDetailEvent
    data class OperationFailed(val message: String?) : VideoDetailEvent
    data class DownloadEnqueued(val count: Int) : VideoDetailEvent
}

@HiltViewModel
class VideoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val downloadManager: DownloadManager
) : ViewModel() {

    private val avid = savedStateHandle.get<Long>("avid") ?: 0
    private val bvid = savedStateHandle.get<String>("bvid").orEmpty()

    private val _uiState = MutableStateFlow(VideoDetailUiState())
    val uiState: StateFlow<VideoDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<VideoDetailEvent>()
    val events = _events.receiveAsFlow()

    private var likeJob: Job? = null

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val videoInfoResult = bilibiliApi.api(IVideoApi::class) {
                getVideoInfo(aid = avid, bvid = bvid)
            }.apiResultNonNull()
            val tagsResult = bilibiliApi.api(IVideoApi::class) {
                getVideoTags(aid = avid, bvid = bvid)
            }.apiResultNonNull()
            val relationResult = if (AccountManager.loggedIn()) {
                bilibiliApi.api(IVideoApi::class) {
                    getVideoRelation(aid = avid, bvid = bvid)
                }.apiResult()
            } else {
                Result.success(null)
            }

            val videoInfo = videoInfoResult.getOrNull()
            val tags = tagsResult.getOrNull()
            val relation = relationResult.getOrNull()

            val error = when {
                videoInfoResult.isFailure -> videoInfoResult.exceptionOrNull()?.message
                tagsResult.isFailure -> tagsResult.exceptionOrNull()?.message
                relationResult.isFailure -> relationResult.exceptionOrNull()?.message
                videoInfo == null || tags == null -> "Failed to load video data"
                else -> null
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = error,
                videoInfo = videoInfo,
                tags = tags ?: emptyList(),
                relation = relation
            )
        }
    }

    fun like() {
        likeJob?.cancel()
        likeJob = viewModelScope.launch {
            val currentUiState = _uiState.value

            val videoInfo = currentUiState.videoInfo ?: run {
                _events.send(VideoDetailEvent.LikeFailed(null))
                return@launch
            }
            val currentRelation = currentUiState.relation ?: run {
                _events.send(VideoDetailEvent.NotLoggedIn)
                return@launch
            }

            val currentLikeStatus = currentRelation.like
            val action = if (currentLikeStatus) 2 else 1

            val previousRelation = currentRelation
            val originalCount = videoInfo.stat.like
            val newCount = videoInfo.stat.like + if (currentLikeStatus) -1 else 1
            _uiState.value = _uiState.value.copy(
                isLiking = true,
                videoInfo = videoInfo.copy(stat = videoInfo.stat.copy(like = newCount)),
                relation = currentRelation.copy(like = !currentLikeStatus)
            )

            try {
                val result = bilibiliApi.api(IVideoApi::class) { likeVideo(videoInfo.aid, action) }.apiResult()
                when {
                    result.isSuccess -> {
                        _events.send(VideoDetailEvent.LikeSuccess(action))
                    }
                    result.isFailure -> {
                        _uiState.value = _uiState.value.copy(
                            videoInfo = videoInfo.copy(stat = videoInfo.stat.copy(like = originalCount)),
                            relation = previousRelation
                        )
                        _events.send(VideoDetailEvent.LikeFailed(result.exceptionOrNull()?.message))
                    }
                }
            } catch (e: CancellationException) {
                _uiState.value = _uiState.value.copy(relation = previousRelation)
                throw e
            } finally {
                if (coroutineContext.isActive) {
                    _uiState.value = _uiState.value.copy(isLiking = false)
                }
            }
        }
    }
    
    // 投币
    fun coin(count: Int, alsoLike: Boolean) {
        viewModelScope.launch {
            val videoInfo = _uiState.value.videoInfo ?: run {
                _events.send(VideoDetailEvent.OperationFailed("视频信息未加载"))
                return@launch
            }
            
            if (!AccountManager.loggedIn()) {
                _events.send(VideoDetailEvent.NotLoggedIn)
                return@launch
            }
            
            try {
                val result = bilibiliApi.api(IVideoApi::class) {
                    coinVideo(
                        aid = videoInfo.aid,
                        multiply = count,
                        selectLike = if (alsoLike) 1 else 0
                    )
                }.apiResult()
                
                if (result.isSuccess) {
                    // 更新UI状态
                    _uiState.value.relation?.let { relation ->
                        _uiState.value = _uiState.value.copy(
                            relation = relation.copy(coin = count),
                            videoInfo = videoInfo.copy(
                                stat = videoInfo.stat.copy(
                                    coin = videoInfo.stat.coin + count,
                                    like = if (alsoLike && !relation.like) videoInfo.stat.like + 1 else videoInfo.stat.like
                                )
                            )
                        )
                        if (alsoLike && !relation.like) {
                            _uiState.value = _uiState.value.copy(
                                relation = relation.copy(like = true, coin = count)
                            )
                        }
                    }
                    _events.send(VideoDetailEvent.CoinSuccess)
                } else {
                    _events.send(VideoDetailEvent.OperationFailed(result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                _events.send(VideoDetailEvent.OperationFailed(e.message))
            }
        }
    }
    
    // 加载收藏夹列表
    fun loadFavoriteFolders() {
        viewModelScope.launch {
            val videoInfo = _uiState.value.videoInfo ?: return@launch
            
            if (!AccountManager.loggedIn()) {
                _events.send(VideoDetailEvent.NotLoggedIn)
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoadingFolders = true)
            
            try {
                val mid = AccountManager.currentAccount.accountId
                val result = bilibiliApi.api(IVideoApi::class) {
                    getFavoriteFolders(rid = videoInfo.aid, upMid = mid)
                }.apiResult()
                
                if (result.isSuccess) {
                    val folders = result.getOrNull()?.list ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        favoriteFolders = folders,
                        isLoadingFolders = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingFolders = false)
                    _events.send(VideoDetailEvent.OperationFailed(result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingFolders = false)
                _events.send(VideoDetailEvent.OperationFailed(e.message))
            }
        }
    }
    
    // 更新收藏
    fun updateFavorites(selectedFids: List<Long>, deselectedFids: List<Long>) {
        viewModelScope.launch {
            val videoInfo = _uiState.value.videoInfo ?: return@launch
            
            if (!AccountManager.loggedIn()) {
                _events.send(VideoDetailEvent.NotLoggedIn)
                return@launch
            }
            
            try {
                val mid = AccountManager.currentAccount.accountId
                val midSuffix = mid.toString().takeLast(2)
                
                // 构建添加和删除的ID字符串
                val addMediaIds = selectedFids.joinToString(",") { "${it}$midSuffix" }
                val delMediaIds = deselectedFids.joinToString(",") { "${it}$midSuffix" }
                
                val result = bilibiliApi.api(IVideoApi::class) {
                    updateFavorite(
                        rid = videoInfo.aid,
                        addMediaIds = addMediaIds,
                        delMediaIds = delMediaIds
                    )
                }.apiResult()
                
                if (result.isSuccess) {
                    // 更新收藏状态
                    val isFavorited = selectedFids.isNotEmpty() || 
                        (_uiState.value.relation?.favorite == true && deselectedFids.isEmpty())
                    
                    _uiState.value.relation?.let { relation ->
                        _uiState.value = _uiState.value.copy(
                            relation = relation.copy(favorite = isFavorited)
                        )
                    }
                    
                    _events.send(VideoDetailEvent.FavoriteSuccess)
                } else {
                    _events.send(VideoDetailEvent.OperationFailed(result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                _events.send(VideoDetailEvent.OperationFailed(e.message))
            }
        }
    }
    
    // 添加到稍后再看
    fun addToWatchLater() {
        viewModelScope.launch {
            val videoInfo = _uiState.value.videoInfo ?: run {
                _events.send(VideoDetailEvent.OperationFailed("视频信息未加载"))
                return@launch
            }
            
            if (!AccountManager.loggedIn()) {
                _events.send(VideoDetailEvent.NotLoggedIn)
                return@launch
            }
            
            try {
                val result = bilibiliApi.api(IVideoApi::class) {
                    addToWatchLater(aid = videoInfo.aid)
                }.apiResult()
                
                if (result.isSuccess) {
                    _events.send(VideoDetailEvent.WatchLaterSuccess)
                } else {
                    _events.send(VideoDetailEvent.OperationFailed(result.exceptionOrNull()?.message))
                }
            } catch (e: Exception) {
                _events.send(VideoDetailEvent.OperationFailed(e.message))
            }
        }
    }

    fun enqueueDownloads(pages: List<VideoPage>) {
        val videoInfo = _uiState.value.videoInfo ?: return
        if (pages.isEmpty()) return

        viewModelScope.launch {
            val defaultQuality = LocalData.settings.playerSettings?.defaultQuality ?: 64
            val title = sanitizeFileName(videoInfo.title)
            var successCount = 0

            for (page in pages) {
                val playUrlResult = bilibiliApi.api(IVideoApi::class) {
                    getPlayUrl(aid = videoInfo.aid, cid = page.cid, qn = defaultQuality)
                }.apiResultNonNull()

                val playUrlData = playUrlResult.getOrNull()
                val url = playUrlData?.durl?.firstOrNull()?.url.orEmpty()

                if (url.isBlank()) {
                    _events.send(VideoDetailEvent.OperationFailed("获取下载地址失败"))
                    continue
                }

                val fileName = buildString {
                    append(title)
                    append("-P")
                    append(page.page)
                    val part = sanitizeFileName(page.part)
                    if (part.isNotBlank()) {
                        append("-")
                        append(part)
                    }
                    append(".mp4")
                }

                downloadManager.enqueue(
                    DownloadRequest(
                        key = "video_${videoInfo.aid}_${page.cid}_$defaultQuality",
                        url = url,
                        headersJson = "{\"Referer\":\"https://bilibili.com\",\"User-Agent\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36\"}",
                        coverUrl = videoInfo.pic,
                        fileName = fileName,
                        mimeType = "video/mp4"
                    )
                )
                successCount += 1
            }

            if (successCount > 0) {
                _events.send(VideoDetailEvent.DownloadEnqueued(successCount))
            }
        }
    }
}

private fun sanitizeFileName(value: String): String {
    return value
        .replace("\\\\", "_")
        .replace("/", "_")
        .replace(":", "_")
        .replace("*", "_")
        .replace("?", "_")
        .replace("\"", "_")
        .replace("<", "_")
        .replace(">", "_")
        .replace("|", "_")
        .trim()
        .take(80)
}
