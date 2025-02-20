package com.huanli233.biliterminal2.activity.video

import android.annotation.SuppressLint
import android.os.Bundle
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.RefreshMainActivity
import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter
import com.huanli233.biliterminal2.api.RecommendApi
import com.huanli233.biliterminal2.helper.TutorialHelper
import com.huanli233.biliterminal2.model.VideoCard
import com.huanli233.biliterminal2.util.CenterThreadPool

class RecommendActivity : RefreshMainActivity() {
    private var videoCardList: MutableList<VideoCard>? = null
    private var videoCardAdapter: VideoCardAdapter? = null
    private var firstRefresh = true

    @SuppressLint("MissingInflatedId")
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

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshRecommend() {
        if (firstRefresh) {
            videoCardList = ArrayList()
        } else {
            val last = videoCardList!!.size
            videoCardList!!.clear()
            videoCardAdapter!!.notifyItemRangeRemoved(0, last)
        }

        addRecommend()
    }

    private fun addRecommend() {
        CenterThreadPool.run {
            try {
                val list: List<VideoCard> = ArrayList()
                RecommendApi.getRecommend(list)
                setRefreshing(false)

                runOnUiThread {
                    videoCardList!!.addAll(list)
                    if (firstRefresh) {
                        firstRefresh = false
                        videoCardAdapter = VideoCardAdapter(this, videoCardList)
                        setAdapter(videoCardAdapter)
                    } else {
                        videoCardAdapter!!.notifyItemRangeInserted(
                            videoCardList!!.size - list.size,
                            list.size
                        )
                    }
                }
            } catch (e: Exception) {
                loadFail(e)
            }
        }
    }
}