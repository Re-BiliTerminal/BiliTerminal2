package com.huanli233.biliterminal2.activity.base

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.listener.OnLoadMoreListener
import com.huanli233.biliterminal2.util.MsgUtil

open class RefreshListActivity : BaseActivity() {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    var emptyView: TextView? = null
    var listener: OnLoadMoreListener? = null
    var bottom = false
    var page = 1
    private var lastLoadTimestamp: Long = 0
    private var isLoading = false // 新增加载状态标志

    private var loadThreshold = 500L // 调整时间间隔

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_refresh)
        emptyView = findViewById(R.id.emptyTip)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.isEnabled = false
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = layoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (canLoadMore() && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    goOnLoad()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (canLoadMore()) {
                    goOnLoad()
                }
            }

            private fun canLoadMore(): Boolean {
                return listener != null &&
                        !isLoading &&
                        !swipeRefreshLayout.isRefreshing &&
                        !bottom &&
                        !recyclerView.canScrollVertically(1)
            }
        })
        swipeRefreshLayout.isRefreshing = true
        goOnLoad() // 触发首次加载
    }

    open val layoutManager: RecyclerView.LayoutManager
        get() = LinearLayoutManager(this)

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        recyclerView.adapter = adapter
    }

    fun setOnRefreshListener(listener: SwipeRefreshLayout.OnRefreshListener) {
        swipeRefreshLayout.setOnRefreshListener(listener)
        swipeRefreshLayout.isEnabled = true
    }

    fun showEmptyView() {
        emptyView?.let {
            it.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    fun hideEmptyView() {
        emptyView?.let {
            it.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun setRefreshing(bool: Boolean) {
        swipeRefreshLayout.isRefreshing = bool
    }

    fun setOnLoadMoreListener(loadMore: OnLoadMoreListener) {
        listener = loadMore
    }

    private fun goOnLoad() {
        if (isLoading) return
        val timeCurrent = System.currentTimeMillis()
        if (timeCurrent - lastLoadTimestamp > loadThreshold) {
            isLoading = true
            swipeRefreshLayout.isRefreshing = true
            page++
            listener?.onLoad(page)
            lastLoadTimestamp = timeCurrent
        }
    }

    fun setBottom(bool: Boolean) {
        bottom = bool
    }

    fun loadFail() {
        if (page > 1) page--
        isLoading = false
        MsgUtil.showMsgLong("加载失败")
        setRefreshing(false)
    }

    fun loadFail(e: Exception) {
        if (page > 1) page--
        isLoading = false
        report(e) // 确保 BaseActivity 有 report 方法
        setRefreshing(false)
    }
}
