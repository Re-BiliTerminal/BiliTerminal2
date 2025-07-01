package com.huanli233.bilizepam.ui.activity.video

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.databinding.ActivityCommonViewpagerBinding
import com.huanli233.bilizepam.ui.activity.base.BaseActivity
import com.huanli233.bilizepam.ui.fragment.video.ARG_KEY_AVID
import com.huanli233.bilizepam.ui.fragment.video.ARG_KEY_BVID
import com.huanli233.bilizepam.ui.fragment.video.VideoInfoFragment
import com.huanli233.bilizepam.ui.utils.viewpager2.pagerFragmentAdapter
import com.huanli233.bilizepam.utils.extensions.putArgument

class VideoInfoActivity: BaseActivity() {

    private lateinit var binding: ActivityCommonViewpagerBinding

    override val transitionEnabled = true

    private val avid by lazy { intent.getLongExtra("avid", 0) }
    private val bvid by lazy { intent.getStringExtra("bvid") }
    private val transitionName by lazy { intent.getStringExtra("transition_name") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonViewpagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.apply {
            adapter = pagerFragmentAdapter(
                listOf(
                    VideoInfoFragment().putArgument {
                        putLong(ARG_KEY_AVID, avid)
                        putString(ARG_KEY_BVID, bvid)
                    },
                )
            )
            binding.dotsIndicator.attachTo(this)
        }

        pageName = getString(R.string.video_detail)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun configTransition() {
        contentTransitionName = transitionName
        setupSharedElementTransitionEnter()
    }

}