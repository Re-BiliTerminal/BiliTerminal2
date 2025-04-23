package com.huanli233.biliterminal2.activity.opus

import android.os.Bundle
import androidx.activity.viewModels
import com.huanli233.biliterminal2.activity.base.BaseActivity
import com.huanli233.biliterminal2.activity.reply.ReplyFragment
import com.huanli233.biliterminal2.databinding.ActivitySimpleViewpagerBinding
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.TerminalContext
import com.huanli233.biliterminal2.util.extensions.invisible
import com.huanli233.biliterminal2.util.extensions.setupFragments
import com.huanli233.biliterminal2.util.extensions.showError
import com.huanli233.biliterminal2.util.extensions.visible

class OpusActivity: BaseActivity() {

    val opusId by lazy {
        intent.getStringExtra("opusId") ?: ""
    }

    private lateinit var binding: ActivitySimpleViewpagerBinding
    val viewModel: OpusViewModel by viewModels {
        OpusViewModelFactory(opusId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val seekReply = intent.getLongExtra("seekReply", -1)
        viewModel.opusState.observe(this) {
            it.onLoading {
                binding.viewPager.invisible()
                binding.loading.visible()
            }.onError {
                binding.viewPager.invisible()
                binding.loading.showError()
                MsgUtil.error(it)
            }.onSuccess {
                val opus = it
                if (opus.basic.commentType == "11") {
                    opus.basic.ridStr.toLongOrNull()?.let { id -> TerminalContext.getInstance().enterDynamicDetailPage(this, id) }
                    finish()
                    return@observe
                }
                binding.loading.invisible()
                binding.viewPager.visible()
                with(binding.viewPager) {
                    visible()
                    setupFragments(
                        fragmentManager = supportFragmentManager,
                        OpusFragment.newInstance(opusId),
                        ReplyFragment.newInstance(
                            oid = opus.basic.commentIdStr.toLongOrNull() ?: -1,
                            replyType = opus.basic.commentType.toIntOrNull() ?: -1,
                            seekReply = seekReply
                        )
                    )
                    if (seekReply != -1L) {
                        setCurrentItem(/* item = */ 1, /* smoothScroll = */ true)
                    }
                }
            }
        }
    }
}