package com.huanli233.biliterminal2.activity.video

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.base.InstanceActivity
import com.huanli233.biliterminal2.adapter.video.VideoCardAdapter
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.bean.VideoCard
import com.huanli233.biliterminal2.bean.toVideoCard
import com.huanli233.biliterminal2.ui.widget.recyclerView.CustomLinearManager
import com.huanli233.biliterminal2.util.ThreadManager
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.view.ImageAutoLoadScrollListener
import com.huanli233.biliwebapi.api.interfaces.IRecommendApi
import kotlinx.coroutines.launch

class PreciousActivity : InstanceActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var videoCardList = mutableListOf<VideoCard>()
    private val videoCardAdapter: VideoCardAdapter by lazy { VideoCardAdapter(this@PreciousActivity, videoCardList) }
    private var firstRefresh = true
    private var refreshing = false

    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_main_refresh)
        setMenuClick()

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    val manager =
                        checkNotNull(recyclerView.layoutManager as LinearLayoutManager?)
                    val lastItemPosition =
                        manager.findLastCompletelyVisibleItemPosition()
                    val itemCount = manager.itemCount
                    if (lastItemPosition >= (itemCount - 3) && dy > 0 && !refreshing) {
                        refreshing = true
                        ThreadManager.run { addPrecious() }
                    }
                }
            })
        }
        ImageAutoLoadScrollListener.install(recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener { this.loadPrecious() }

        val title = findViewById<TextView>(R.id.page_name)
        title.setText(R.string.precious)

        loadPrecious()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPrecious() {
        page = 1
        if (firstRefresh) {
            recyclerView.layoutManager = CustomLinearManager(this)
        } else {
            val last = videoCardList.size
            videoCardList.clear()
            videoCardAdapter.notifyItemRangeRemoved(0, last)
        }

        addPrecious()
    }

    private fun addPrecious() {
        refreshing = true
        runOnUiThread {
            swipeRefreshLayout.isRefreshing = true
        }
        lifecycleScope.launch {
            bilibiliApi.api(IRecommendApi::class) {
                getPrecious(page)
            }.apiResultNonNull().onSuccess { data ->
                page++
                runOnUiThread {
                    videoCardList.addAll(data.items.map { it.toVideoCard() })
                    swipeRefreshLayout.isRefreshing = false
                    refreshing = false
                    if (firstRefresh) {
                        firstRefresh = false
                        recyclerView.adapter = videoCardAdapter
                    } else {
                        videoCardAdapter.notifyItemRangeInserted(
                            videoCardList.size - data.items.size,
                            data.items.size
                        )
                    }
                }
            }.onFailure {
                runOnUiThread {
                    MsgUtil.error(it)
                }
            }
        }
    }
}