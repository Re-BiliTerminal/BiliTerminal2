package com.huanli233.bilizepam.ui.fragment.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huanli233.bilizepam.api.apiResult
import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.bilizepam.data.account.AccountManager
import com.huanli233.biliwebapi.api.interfaces.IVideoApi
import com.huanli233.biliwebapi.bean.video.Tag
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.biliwebapi.bean.video.VideoRelation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class VideoUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val videoInfo: VideoInfo? = null,
    val tags: List<Tag> = emptyList(),
    val relation: VideoRelation? = null,
    val isLiking: Boolean = false
)

sealed interface VideoEvent {
    data class LikeSuccess(val action: Int) : VideoEvent
    data class LikeFailed(val message: String?) : VideoEvent
    data object NotLoggedIn : VideoEvent
}

class VideoInfoViewModel(
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val bvid = savedStateHandle.get<String>("bvid") ?: throw IllegalArgumentException("bvid is null")

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private val _events = Channel<VideoEvent>()
    val events = _events.receiveAsFlow()

    private var likeJob: Job? = null

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            val videoInfoResult = bilibiliApi.api(IVideoApi::class) {
                getVideoInfo(aid = 0, bvid = bvid)
            }.apiResultNonNull()
            val tagsResult = bilibiliApi.api(IVideoApi::class) {
                getVideoTags(aid = 0, bvid = bvid)
            }.apiResultNonNull()
            val relationResult = if (AccountManager.loggedIn()) {
                bilibiliApi.api(IVideoApi::class) {
                    getVideoRelation(aid = 0, bvid = bvid)
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
                _events.send(VideoEvent.LikeFailed(null))
                return@launch
            }
            val currentRelation = currentUiState.relation ?: run {
                _events.send(VideoEvent.NotLoggedIn)
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
                        _events.send(VideoEvent.LikeSuccess(action))
                    }
                    result.isFailure -> {
                        _uiState.value = _uiState.value.copy(
                            videoInfo = videoInfo.copy(stat = videoInfo.stat.copy(like = originalCount)),
                            relation = previousRelation
                        )
                        _events.send(VideoEvent.LikeFailed(result.exceptionOrNull()?.message))
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
}