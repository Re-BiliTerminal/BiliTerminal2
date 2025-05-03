package com.huanli233.biliterminal2.activity.dynamic

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.activity.reply.ReplyFragment
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.databinding.ActivitySimpleViewpagerBinding
import com.huanli233.biliterminal2.event.ReplyEvent
import com.huanli233.biliterminal2.helper.TutorialHelper
import com.huanli233.biliterminal2.ui.TopBarBinder
import com.huanli233.biliterminal2.ui.bindTopBar
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.extensions.LoadState
import com.huanli233.biliterminal2.util.extensions.invisible
import com.huanli233.biliterminal2.util.extensions.setupFragments
import com.huanli233.biliterminal2.util.extensions.showError
import com.huanli233.biliterminal2.util.extensions.visible
import com.huanli233.biliwebapi.api.interfaces.IDynamicApi
import com.huanli233.biliwebapi.bean.dynamic.Dynamic
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DynamicInfoActivity : BaseActivity(), TopBarBinder {
    private lateinit var binding: ActivitySimpleViewpagerBinding
    lateinit var replyFragment: ReplyFragment
    private val seekReply by lazy { intent.getLongExtra("seekReply", -1) }
    private val dynamicId by lazy { intent.getLongExtra("id", -1) }

    private val viewModel by viewModels<DynamicInfoViewModel> {
        DynamicInfoViewModelFactory(dynamicId.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleViewpagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTopbarExit()
        setPageName(getString(R.string.dynamic_info))

        TutorialHelper.showTutorialList(this, R.array.tutorial_dynamic_info, 6)

        viewModel.state.observe(this) {
            when (it) {
                is LoadState.Loading -> {
                    binding.loading.visible()
                    binding.viewPager.invisible()
                }

                is LoadState.Error -> {
                    binding.loading.showError()
                    binding.viewPager.invisible()
                    MsgUtil.error(it.error)
                }

                is LoadState.Success -> {
                    binding.loading.invisible()

                    with(binding.viewPager) {
                        visible()
                        setupFragments(
                            fragmentManager = supportFragmentManager,
                            DynamicInfoFragment.newInstance(),
                            ReplyFragment.newInstance(
                                oid = it.data.basic.commentIdStr.toLongOrNull() ?: -1,
                                replyType = it.data.basic.commentType.toIntOrNull() ?: -1,
                                seekReply = seekReply
                            )
                        )
                    }
                }
            }
        }
    }

    override fun eventBusEnabled(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true, priority = 1)
    fun onEvent(event: ReplyEvent) {
        replyFragment.notifyReplyInserted(event)
    }

    override fun bindToTopBar(scrollableView: View) {
        scrollableView.bindTopBar(binding.topBar)
    }
}

class DynamicInfoViewModel(
    val dynamicId: String
): ViewModel() {

    private val _state: MutableLiveData<LoadState<Dynamic>> = MutableLiveData()
    val state: LiveData<LoadState<Dynamic>> = _state

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            bilibiliApi.api(IDynamicApi::class) {
                getDynamic(dynamicId)
            }.apiResultNonNull()
                .onSuccess {
                    _state.postValue(LoadState.Success(it.item))
                }
                .onFailure {
                    _state.postValue(LoadState.Error(it))
                }
        }
    }

}

class DynamicInfoViewModelFactory(
    val dynamicId: String
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DynamicInfoViewModel(dynamicId) as T
    }
}
