package com.huanli233.biliterminal2.ui.activity.video

import android.os.Bundle
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.databinding.ActivityCommonViewpagerBinding
import com.huanli233.biliterminal2.ui.activity.base.BaseActivity
import com.huanli233.biliterminal2.ui.activity.login.EXTRA_NAME_FROM_SETUP
import com.huanli233.biliterminal2.ui.fragment.login.ImportLoginFragment
import com.huanli233.biliterminal2.ui.fragment.login.QrCodeLoginFragment
import com.huanli233.biliterminal2.ui.fragment.video.ARG_KEY_BVID
import com.huanli233.biliterminal2.ui.fragment.video.VideoInfoFragment
import com.huanli233.biliterminal2.ui.utils.viewpager2.pagerFragmentAdapter
import com.huanli233.biliterminal2.ui.widget.pager.setupWithIndicator
import com.huanli233.biliterminal2.utils.extensions.putArgument

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
                        putString(ARG_KEY_BVID, intent.getStringExtra("bvid"))
                    },
                )
            )
            binding.dotsIndicator.attachTo(this)
        }

        pageName = getString(R.string.video_detail)
    }

}