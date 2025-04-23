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

class RefreshListActivity : BaseActivity() {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    var emptyView: TextView? = null
    var listener: OnLoadMoreListener? = null
    var bottom = false
    var page = 1
    var lastLoadTimestamp: Long = 0
    
    private var loadThreshold = 100L
    
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_refresh)
        emptyView = findViewById(R.id.emptyTip)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.isRefreshing = true
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = layoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (listener != null &&
                    !recyclerView.canScrollVertically(1) &&
                    !swipeRefreshLayout.isRefreshing &&
                    newState == RecyclerView.SCROLL_STATE_DRAGGING &&
                    !bottom
                ) {
                    goOnLoad()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (listener != null) {
                    val manager = recyclerView.layoutManager as LinearLayoutManager
                    val lastItemPosition = manager.findLastCompletelyVisibleItemPosition()
                    val itemCount = manager.itemCount
                    if (lastItemPosition >= (itemCount - 3) &&
                        dy > 0 &&
                        !swipeRefreshLayout.isRefreshing &&
                        !bottom
                    ) {
                        goOnLoad()
                    }
                }
            }
        })
    }

    val layoutManager: RecyclerView.LayoutManager
        get() = LinearLayoutManager(this)

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        runOnUiThread { recyclerView.adapter = adapter }
    }

    fun setOnRefreshListener(listener: SwipeRefreshLayout.OnRefreshListener) {
        swipeRefreshLayout.setOnRefreshListener(listener)
        swipeRefreshLayout.isEnabled = true
    }

    fun showEmptyView() {
        emptyView?.let {
            runOnUiThread {
                recyclerView.visibility = View.GONE
                it.visibility = View.VISIBLE
            }
        }
    }

    fun hideEmptyView() {
        emptyView?.let {
            runOnUiThread {
                recyclerView.visibility = View.VISIBLE
                it.visibility = View.GONE
            }
        }
    }

    fun setRefreshing(bool: Boolean) {
        runOnUiThread { swipeRefreshLayout.isRefreshing = bool }
    }

    fun setOnLoadMoreListener(loadMore: OnLoadMoreListener) {
        listener = loadMore
    }

    private fun goOnLoad() {
        val timeCurrent = System.currentTimeMillis()
        if (timeCurrent - lastLoadTimestamp > loadThreshold) {
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
        page--
        MsgUtil.showMsgLong("加载失败")
        setRefreshing(false)
    }

    fun loadFail(e: Exception) {
        page--
        report(e)
        setRefreshing(false)
    }
}
