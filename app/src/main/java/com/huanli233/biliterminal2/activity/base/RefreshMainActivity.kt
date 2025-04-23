package com.huanli233.biliterminal2.activity.base

import android.os.Bundle
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.listener.OnLoadMoreListener
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.view.ImageAutoLoadScrollListener

// 扩展函数：简化滚动监听设置
private fun RecyclerView.setupLoadMore(
    swipeLayout: SwipeRefreshLayout,
    loadMoreListener: OnLoadMoreListener?,
    isRefreshing: Boolean,
    bottomReached: Boolean,
    onLoadTrigger: () -> Unit
) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (loadMoreListener != null
                && !canScrollVertically(1)
                && !swipeLayout.isRefreshing
                && newState == RecyclerView.SCROLL_STATE_DRAGGING
                && !bottomReached
            ) {
                onLoadTrigger()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            loadMoreListener?.takeIf { dy > 0 }?.let {
                (layoutManager as? LinearLayoutManager)?.run {
                    if (findLastCompletelyVisibleItemPosition() >= itemCount - 3
                        && !swipeLayout.isRefreshing
                        && !isRefreshing
                        && !bottomReached
                    ) {
                        onLoadTrigger()
                    }
                }
            }
        }
    })
}

// 扩展函数：简化失败处理
private fun RefreshMainActivity.handleLoadFail(e: Throwable? = null) {
    page--
    e?.let(::report) ?: MsgUtil.showMsgLong("加载失败")
    setRefreshing(false)
}

open class RefreshMainActivity : InstanceActivity() {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    var listener: OnLoadMoreListener? = null
    var bottomReached = false
    var page = 1
    var lastLoadTimestamp: Long = 0
    protected var isRefreshing = false

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_main_refresh)
        
        // 视图初始化
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout).apply {
            isEnabled = false
            isRefreshing = true
        }
        
        recyclerView = findViewById(R.id.recycler_view).apply {
            layoutManager = getLayoutManager()
            // 使用扩展函数设置滚动监听
            setupLoadMore(swipeRefreshLayout, listener, isRefreshing, bottomReached, ::goOnLoad)
            ImageAutoLoadScrollListener.install(this)
        }
    }

    protected open fun getLayoutManager(): RecyclerView.LayoutManager = LinearLayoutManager(this)

    fun setAdapter(adapter: RecyclerView.Adapter<*>) = runOnUiThread { recyclerView.adapter = adapter }

    fun setOnRefreshListener(listener: SwipeRefreshLayout.OnRefreshListener?) {
        swipeRefreshLayout.apply {
            setOnRefreshListener(listener)
            isEnabled = true
        }
    }

    fun setRefreshing(bool: Boolean) {
        runOnUiThread { swipeRefreshLayout.isRefreshing = bool }
        isRefreshing = bool
    }

    fun setOnLoadMoreListener(loadMore: OnLoadMoreListener?) {
        listener = loadMore
    }

    private fun goOnLoad() = synchronized(this) {
        System.currentTimeMillis().takeIf { it - lastLoadTimestamp > 100 }?.let {
            swipeRefreshLayout.isRefreshing = true
            page++
            listener?.onLoad(page)
            lastLoadTimestamp = it
        }
    }

    fun setBottomReached(bool: Boolean) { bottomReached = bool }

    fun loadFail() = handleLoadFail()
    fun loadFail(e: Throwable?) = handleLoadFail(e)
}
