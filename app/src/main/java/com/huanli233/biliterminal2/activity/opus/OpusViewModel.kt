package com.huanli233.biliterminal2.activity.opus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.api.apiResult
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.api.toResult
import com.huanli233.biliterminal2.api.toResultNonNull
import com.huanli233.biliterminal2.event.Event
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.extensions.LoadState
import com.huanli233.biliterminal2.util.extensions.msg
import com.huanli233.biliwebapi.api.interfaces.ICommonApi
import com.huanli233.biliwebapi.api.interfaces.IDynamicApi
import com.huanli233.biliwebapi.api.interfaces.IOpusApi
import com.huanli233.biliwebapi.bean.common.SimpleAction
import com.huanli233.biliwebapi.bean.opus.Opus
import kotlinx.coroutines.launch

class OpusViewModel(
    private val opusId: String
) : ViewModel() {

    private val _opusState = MutableLiveData<LoadState<Opus>>(LoadState.Loading())
    val opusState: LiveData<LoadState<Opus>> get() = _opusState

    private val _toastEvent = MutableLiveData<Event<String>>()
    val toastEvent: LiveData<Event<String>> get() = _toastEvent

    val data: LiveData<Opus> get() = opusState.switchMap { state ->
        when (state) {
            is LoadState.Success -> MutableLiveData(state.data)
            else -> MutableLiveData()
        }
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            bilibiliApi.api(IOpusApi::class) { getOpus(opusId) }.apiResultNonNull()
                .onSuccess {
                    _opusState.postValue(LoadState.Success(it.item))
                }.onFailure {
                    _opusState.postValue(LoadState.Error(it))
                }
        }
    }

    fun like() {
        viewModelScope.launch {
            val currentLike = data.value?.modules?.moduleStat?.like
            val currentStatus = currentLike?.status == true
            val action = if (currentStatus) 2 else 1

            bilibiliApi.api(IDynamicApi::class) { like(opusId, action) }
                .apiResult()
                .onSuccess {
                    data.value?.let { opus ->
                        val delta = if (currentStatus) -1 else 1
                        _opusState.postValue(
                            LoadState.Success(
                                opus.updateLikeStatus(
                                    newStatus = !currentStatus,
                                    delta = delta
                                )
                            )
                        )
                    }
                }
                .onFailure {
                    _toastEvent.postValue(Event(it.msg()))
                }
        }
    }


    fun favorite() {
        viewModelScope.launch {
            val currentFav = data.value?.modules?.moduleStat?.favourite
            val currentStatus = currentFav?.status == true

            bilibiliApi.api(ICommonApi::class) {
                simpleAction(
                    SimpleAction(
                        entity = SimpleAction.Entity(
                            objectIdStr = opusId,
                            type = SimpleAction.EntityType(2)
                        ),
                        action = if (currentStatus) 4 else 3
                    )
                )
            }.apiResult()
                .onSuccess {
                    data.value?.let { opus ->
                        val delta = if (currentStatus) -1 else 1
                        _opusState.postValue(
                            LoadState.Success(
                                opus.updateFavouriteStatus(
                                    newStatus = !currentStatus,
                                    delta = delta
                                )
                            )
                        )
                    }
                }
                .onFailure {
                    _toastEvent.postValue(Event(it.msg()))
                }
        }
    }

    private fun Opus.updateFavouriteStatus(newStatus: Boolean, delta: Int): Opus {
        return this.copy(
            modules = modules.copy(
                moduleStat = modules.moduleStat.copy(
                    favourite = modules.moduleStat.favourite.copy(
                        count = modules.moduleStat.favourite.count + delta,
                        status = newStatus
                    )
                )
            )
        )
    }

    private fun Opus.updateLikeStatus(newStatus: Boolean, delta: Int): Opus {
        return this.copy(
            modules = modules.copy(
                moduleStat = modules.moduleStat.copy(
                    like = modules.moduleStat.like.copy(
                        count = modules.moduleStat.like.count + delta,
                        status = newStatus
                    )
                )
            )
        )
    }
}

class OpusViewModelFactory(private val opusId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OpusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OpusViewModel(opusId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}