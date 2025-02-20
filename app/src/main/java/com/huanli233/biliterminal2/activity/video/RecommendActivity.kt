package com.huanli233.biliterminal2.activity.video

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.RefreshMainActivity
import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.helper.TutorialHelper
import com.huanli233.biliterminal2.model.VideoCard
import com.huanli233.biliterminal2.model.toVideoCard
import com.huanli233.biliterminal2.util.uniqId
import com.huanli233.biliwebapi.bean.recommend.home.HomeRecommend
import kotlinx.coroutines.launch

class RecommendActivity : RefreshMainActivity() {
    private var videoCardList = mutableListOf<VideoCard>()
    private val videoCardAdapter: VideoCardAdapter by lazy { VideoCardAdapter(this, videoCardList) }
    private var firstRefresh = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMenuClick()

        setOnRefreshListener { this.refreshRecommend() }
        setOnLoadMoreListener { addRecommend() }

        setPageName(getString(R.string.recommend))
        recyclerView.setHasFixedSize(true)

        TutorialHelper.showTutorialList(this, R.array.tutorial_recommend, 0)

        refreshRecommend()
    }

    private fun refreshRecommend() {
        val last = videoCardList.size
        videoCardList.clear()
        videoCardAdapter.notifyItemRangeRemoved(0, last)
        addRecommend()
    }

    private fun addRecommend() {
        lifecycleScope.launch {
            runCatching {
                val recommend = HomeRecommend.request(bilibiliApi, uniqId.toString())
                setRefreshing(false)
                val items = recommend.data!!.item.map { it.toVideoCard() }

                runOnUiThread {
                    videoCardList.addAll(items)
                    if (firstRefresh) {
                        firstRefresh = false
                        setAdapter(videoCardAdapter)
                    } else {
                        videoCardAdapter.notifyItemRangeInserted(
                            videoCardList.size - items.size,
                            items.size
                        )
                    }
                }
            }.onFailure {
                loadFail(it)
            }
        }
    }
}