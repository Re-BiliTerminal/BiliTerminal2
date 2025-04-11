package com.huanli233.biliterminal2.activity.live

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.activity.settings.SettingPlayerChooseActivity
import com.huanli233.biliterminal2.adapter.common.Choice
import com.huanli233.biliterminal2.adapter.common.ChoiceAdapter
import com.huanli233.biliterminal2.adapter.user.StaffListAdapter
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.bean.PlayerData
import com.huanli233.biliterminal2.bean.TYPE_LIVE
import com.huanli233.biliterminal2.databinding.ActivityLiveInfoBinding
import com.huanli233.biliterminal2.event.Event
import com.huanli233.biliterminal2.event.emptyEvent
import com.huanli233.biliterminal2.player.PlayerManager
import com.huanli233.biliterminal2.ui.bindTopBar
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomLinearManager
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.MsgUtil.showMsgLong
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.copyable
import com.huanli233.biliterminal2.util.extensions.LoadState
import com.huanli233.biliterminal2.util.extensions.combineLoadStates
import com.huanli233.biliterminal2.util.runOnUi
import com.huanli233.biliwebapi.api.interfaces.ILiveApi
import com.huanli233.biliwebapi.api.interfaces.IUserApi
import com.huanli233.biliwebapi.bean.live.DEFAULT_QN
import com.huanli233.biliwebapi.bean.live.LivePlayInfo
import com.huanli233.biliwebapi.bean.live.LiveRoom
import com.huanli233.biliwebapi.bean.user.UserCardInfo
import kotlinx.coroutines.launch


val QUALITY_MAP = mapOf(
    R.string.quality_80 to 80,
    R.string.quality_150 to 150,
    R.string.quality_250 to 250,
    R.string.quality_400 to 400,
    R.string.quality_10000 to 10000
)

class LiveInfoActivity : BaseActivity() {

    val roomId by lazy {
        intent.getLongExtra("room_id", 0)
    }

    val viewModel by viewModels<LiveInfoViewModel> {
        LiveInfoViewModelFactory(roomId.toString())
    }

    private lateinit var binding: ActivityLiveInfoBinding

    private val staffAdapter by lazy { StaffListAdapter(this) }
    private val qualityAdapter by lazy {
        ChoiceAdapter().apply {
            setOnItemClickListener {
                viewModel.refreshHosts(it.value.toInt())
            }
        }
    }
    private val hostAdapter by lazy {
        ChoiceAdapter().apply {
            setOnItemClickListener {
                viewModel.selectHost(it.index)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        if (roomId == 0L) {
            finish()
            return
        }
        binding = ActivityLiveInfoBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        with(binding) {
            scrollView.bindTopBar(binding.topBar)
            play.setOnLongClickListener {
                if (Preferences.getString("player", "null") != "terminalPlayer") showMsgLong(getString(R.string.tip_live_player))
                val intent = Intent()
                intent.setClass(this@LiveInfoActivity, SettingPlayerChooseActivity::class.java)
                startActivity(intent)
                true
            }
            uploaderList.apply {
                layoutManager = CustomLinearManager(this@LiveInfoActivity)
                adapter = staffAdapter
            }
            qualityList.apply {
                layoutManager = CustomLinearManager(this@LiveInfoActivity)
                adapter = qualityAdapter
            }
            hostList.apply {
                layoutManager = CustomLinearManager(this@LiveInfoActivity)
                adapter = hostAdapter
            }
            copyable(idText, tags, textTitle)
        }
        viewModel.loadState.observe(this) {
            it.onSuccess {
                if (staffAdapter.data.firstOrNull()?.equals(it.creatorInfo) != true) {
                    staffAdapter.data.apply {
                        clear()
                        add(it.creatorInfo.toUserInfo())
                    }
                    staffAdapter.notifyDataSetChanged()
                }
                qualityAdapter.setData(QUALITY_MAP.map { Choice(getString(it.key), it.value.toString()) })
            }
        }
        viewModel.playInfo.observe(this) {
            it.onSuccess {
                val choices = it.playurlInfo?.playurl?.stream?.firstOrNull()?.format?.firstOrNull()?.codec?.firstOrNull()?.urlInfo?.mapIndexed { index, _ -> Choice("路线$index", index.toString()) }
                hostAdapter.setData(choices.orEmpty())
            }
        }
        viewModel.openPlayer.observe(this) {
            it.handle {
                viewModel.playInfo.value?.toSuccessOrNull()?.data?.let { playInfo ->
                    val codec = playInfo.playurlInfo?.playurl?.stream?.firstOrNull()?.format?.firstOrNull()?.codec?.firstOrNull() ?: return@handle
                    val urlInfo = codec.urlInfo.getOrNull(viewModel.selectedHost.value ?: 0) ?: return@handle
                    val playUrl = "${urlInfo.host}${codec.baseUrl}${urlInfo.extra}"

                    val playerData = PlayerData(TYPE_LIVE)
                    playerData.urlVideo = playUrl
                    playerData.title = "直播·" + viewModel.loadState.value?.toSuccessOrNull()?.data?.room?.title
                    playerData.aid = roomId
                    playerData.mid = Preferences.getLong("mid", 0)

                    runOnUi {
                        runCatching {
                            startActivity(PlayerManager.playerIntent(this, playerData))
                        }.onFailure {
                            when (it) {
                                is ActivityNotFoundException -> MsgUtil.showMsg(getString(R.string.player_not_found))
                                else -> MsgUtil.error(it)
                            }
                        }
                    }
                }
            }
        }
    }
}

class LiveInfoViewModel(val liveRoomId: String): ViewModel() {

    private val _creatorInfo = MutableLiveData<LoadState<UserCardInfo>>()
    private val _liveRoom = MutableLiveData<LoadState<LiveRoom>>()
    val loadState: LiveData<LoadState<LiveInfo>> = combineLoadStates(_creatorInfo, _liveRoom) { creatorInfo, liveRoom ->
        LiveInfo(creatorInfo, liveRoom)
    }

    private val _playInfo = MutableLiveData<LoadState<LivePlayInfo>>()
    val playInfo: LiveData<LoadState<LivePlayInfo>> = _playInfo

    private val _loadingHost = MutableLiveData<Boolean>()
    val loadingHost: LiveData<Boolean> = _loadingHost

    private val _selectedHost = MutableLiveData<Int>()
    val selectedHost: LiveData<Int> = _selectedHost

    private val _openPlayer = MutableLiveData<Event<Unit>>()
    val openPlayer: LiveData<Event<Unit>> = _openPlayer

    init {
        fetchData()
        fetchPlayInfo()
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

    fun fetchPlayInfo(
        qn: Int = DEFAULT_QN
    ) {
        viewModelScope.launch {
            bilibiliApi.api(ILiveApi::class) {
                getPlayInfo(roomId = liveRoomId, qn = qn)
            }.apiResultNonNull().onSuccess {
                _playInfo.postValue(LoadState.Success(it))
                selectHost(0)
                _loadingHost.postValue(false)
            }.onFailure {
                _playInfo.postValue(LoadState.Error(it))
                selectHost(0)
                _loadingHost.postValue(false)
            }
        }
    }

    fun play() {
        if (_playInfo.value != null) {
            _openPlayer.postValue(emptyEvent())
        }
    }

    fun refreshHosts(qn: Int) {
        if (_loadingHost.value != true) {
            _loadingHost.postValue(true)
            fetchPlayInfo(qn)
        }
    }

    fun selectHost(selectedHost: Int) {
        _selectedHost.postValue(selectedHost)
    }
}

data class LiveInfo(
    val creatorInfo: UserCardInfo,
    val room: LiveRoom
)

class LiveInfoViewModelFactory(val liveRoomId: String): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LiveInfoViewModel(liveRoomId) as T
    }
}