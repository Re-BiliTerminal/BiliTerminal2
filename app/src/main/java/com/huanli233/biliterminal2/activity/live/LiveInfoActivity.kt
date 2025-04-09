package com.huanli233.biliterminal2.activity.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.api.onApiFailure
import com.huanli233.biliterminal2.databinding.ActivityLiveInfoBinding
import com.huanli233.biliterminal2.event.Event
import com.huanli233.biliterminal2.event.event
import com.huanli233.biliterminal2.util.extensions.LoadState
import com.huanli233.biliterminal2.util.extensions.msg
import com.huanli233.biliwebapi.api.interfaces.ILiveApi
import com.huanli233.biliwebapi.api.interfaces.IUserApi
import com.huanli233.biliwebapi.bean.live.LivePlayInfo
import com.huanli233.biliwebapi.bean.live.LiveRoom
import com.huanli233.biliwebapi.bean.user.UserCardInfo
import com.huanli233.biliwebapi.bean.user.UserInfo
import kotlinx.coroutines.launch

class LiveInfoActivity : BaseActivity() {

    val roomId by lazy {
        intent.getLongExtra("room_id", 0)
    }

    private lateinit var binding: ActivityLiveInfoBinding

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        if (roomId == 0L) {
            finish()
            return
        }
        binding = ActivityLiveInfoBinding.inflate(layoutInflater)
    }
}

class LiveInfoViewModel(val liveRoomId: String): ViewModel() {

    private val _creatorInfo = MutableLiveData<LoadState<UserCardInfo>>()
    private val _liveRoom = MutableLiveData<LoadState<LiveRoom>>()
    val loadState: LiveData<LoadState<LiveInfo>> = MediatorLiveData<LoadState<LiveInfo>>().apply {
        var creatorState: LoadState<UserCardInfo>? = null
        var roomState: LoadState<LiveRoom>? = null

        fun update() {
            val creator = creatorState
            val room = roomState
            if (creator == null || room == null) return

            val newState = when {
                creator.isLoading || room.isLoading -> LoadState.Loading()
                creator.isError || room.isError -> {
                    val error = room.takeIf { it is LoadState.Error } ?: creator
                    LoadState.Error((error as LoadState.Error).error)
                }
                else -> LoadState.Success(
                    LiveInfo(
                        creatorInfo = (creator as LoadState.Success).data,
                        room = (room as LoadState.Success).data
                    )
                )
            }
            value = newState
        }

        addSource(_creatorInfo) {
            creatorState = it
            update()
        }
        addSource(_liveRoom) {
            roomState = it
            update()
        }
    }

    private val _openPlayer = MutableLiveData<Event<LivePlayInfo>>()
    val openPlayer: LiveData<Event<LivePlayInfo>> = _openPlayer

    private val _showError = MutableLiveData<Event<String>>()
    val showError: LiveData<Event<String>> = _showError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            bilibiliApi.api(ILiveApi::class) {
                getRoomInfo(liveRoomId)
            }.apiResultNonNull().onSuccess { liveRoom ->
                _liveRoom.postValue(LoadState.Success(liveRoom))
                bilibiliApi.api(IUserApi::class) {
                    getCard(liveRoom.uid.toString())
                }.apiResultNonNull().onSuccess {
                    _creatorInfo.postValue(LoadState.Success(it))
                }.onFailure {
                    _creatorInfo.postValue(LoadState.Error(it))
                }
            }.onFailure {
                _liveRoom.postValue(LoadState.Error(it))
            }
        }
    }

    fun play() {
        if (isLoading.value != true) {
            viewModelScope.launch {
                bilibiliApi.api(ILiveApi::class) {
                    getPlayInfo(roomId = liveRoomId, qn = TODO())
                }.apiResultNonNull().onSuccess {
                    _openPlayer.postValue(it.event())
                }.onFailure {
                    _showError.postValue(it.msg().event())
                }
            }
        }
    }
}

data class LiveInfo(
    val creatorInfo: UserCardInfo,
    val room: LiveRoom,
)

class LiveInfoViewModelFactory(val liveRoomId: String): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LiveInfoViewModel(liveRoomId) as T
    }
}