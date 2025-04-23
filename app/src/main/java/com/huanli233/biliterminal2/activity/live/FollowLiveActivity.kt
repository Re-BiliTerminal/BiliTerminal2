package com.huanli233.biliterminal2.activity.live

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.huanli233.biliterminal2.activity.base.RefreshListActivity
import com.huanli233.biliterminal2.adapter.LiveCardAdapter
import com.huanli233.biliterminal2.api.LiveApi
import com.huanli233.biliterminal2.bean.LiveRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowLiveActivity : RefreshListActivity() {
    private val roomList = mutableListOf<LiveRoom>()
    private lateinit var adapter: LiveCardAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setPageName("我关注的直播")
        recyclerView.setHasFixedSize(true)

        loadInitialData()
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { 
                    LiveApi.getFollowed(page) 
                }
                
                roomList.clear()
                roomList.addAll(result)

                adapter = LiveCardAdapter(this@FollowLiveActivity, roomList)
                setAdapter(adapter)
                
                if (roomList.isEmpty()) {
                    showEmptyView()
                } else {
                    hideEmptyView()
                }
                
                setOnLoadMoreListener { page -> loadMoreData(page) }
                setRefreshing(false)
            } catch (e: Exception) {
                loadFail(e)
            }
        }
    }

    private fun loadMoreData(page: Int) {
        lifecycleScope.launch {
            try {
                val list = withContext(Dispatchers.IO) { 
                    LiveApi.getFollowed(page) 
                }
                
                if (!list.isNullOrEmpty()) {
                    val startPos = roomList.size
                    roomList.addAll(list)
                    adapter.notifyItemRangeInserted(startPos, list.size)
                }
                
                setRefreshing(false)
                setBottom(list.isNullOrEmpty())
            } catch (e: Exception) {
                loadFail(e)
            }
        }
    }
}
