package com.huanli233.biliterminal2.activity.video.info

import android.os.Bundle
import android.view.View
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import com.huanli233.biliterminal2.activity.base.RefreshListFragment
import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter
import com.huanli233.biliterminal2.api.RecommendApi
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.api.toResultNonNull
import com.huanli233.biliterminal2.model.toVideoCard
import com.huanli233.biliterminal2.util.CenterThreadPool
import com.huanli233.biliterminal2.util.Result
import com.huanli233.biliwebapi.bean.video.VideoInfo
import kotlinx.coroutines.launch
import java.util.concurrent.Callable

class VideoRecommendFragment : RefreshListFragment() {
    private var aid: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            aid = requireArguments().getLong("aid")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            VideoInfo.related(bilibiliApi, aid).toResultNonNull().also {
                isRefreshing = false
            }.onSuccess { data ->
                runOnUiThread {
                    val adapter = VideoCardAdapter(requireContext(), data.map { it.toVideoCard() })
                    setAdapter(adapter)
                }
            }.onFailure {
                loadFail(it)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(aid: Long): VideoRecommendFragment {
            val fragment = VideoRecommendFragment()
            val args = Bundle()
            args.putLong("aid", aid)
            fragment.arguments = args
            return fragment
        }
    }
}