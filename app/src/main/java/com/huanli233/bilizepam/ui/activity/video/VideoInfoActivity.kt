package com.huanli233.bilizepam.ui.activity.video

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonViewpagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.apply {
            adapter = pagerFragmentAdapter(
                listOf(
                    VideoInfoFragment().putArgument {
                        putLong(ARG_KEY_AVID, intent.getLongExtra("avid", 0))
                        putString(ARG_KEY_BVID, intent.getStringExtra("bvid"))
                    },
                )
            )
            binding.dotsIndicator.attachTo(this)
        }

        pageName = getString(R.string.video_detail)
    }

}