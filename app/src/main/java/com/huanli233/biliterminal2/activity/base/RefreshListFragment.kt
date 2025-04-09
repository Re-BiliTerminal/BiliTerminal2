package com.huanli233.biliterminal2.activity.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.listener.OnLoadMoreListener
import com.huanli233.biliterminal2.ui.widget.recycler.CustomGridManager
import com.huanli233.biliterminal2.ui.widget.recycler.CustomLinearManager
import com.huanli233.biliterminal2.util.MsgUtil
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliterminal2.util.view.ImageAutoLoadScrollListener

open class RefreshListFragment : Fragment() {
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    private var emptyView: TextView? = null
    private var onLoadMoreListener: OnLoadMoreListener? = null
    var bottomReached = false
    var page = 1
    var lastLoadTimestamp = 0L
    var forceSingleColumn = false
    var refreshing: Boolean = false
        get() {
            return swipeRefreshLayout.isRefreshing
        }
        set(value) {
            field = value
            runOnUiThread { swipeRefreshLayout.isRefreshing = value }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_simple_refresh, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            emptyView = findViewById(R.id.emptyTip)
            swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).apply {
                isEnabled = false
                isRefreshing = true
            }
            recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
                layoutManager = this@RefreshListFragment.getLayoutManager()
                addOnScrollListener(createScrollListener())
                ImageAutoLoadScrollListener.install(this)
            }
        }
    }

    private fun createScrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (shouldTriggerLoadMore(newState)) {
                goOnLoad()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            (recyclerView.layoutManager as? LinearLayoutManager)?.let { manager ->
                val lastVisible = manager.findLastVisibleItemPosition()
                val itemCount = manager.itemCount
                if (lastVisible >= itemCount - 3 && dy > 0 && shouldTriggerLoadMore()) {
                    goOnLoad()
                }
            }
        }

        private fun shouldTriggerLoadMore(newState: Int = RecyclerView.SCROLL_STATE_IDLE) =
            onLoadMoreListener != null &&
                    !recyclerView.canScrollVertically(1) &&
                    !swipeRefreshLayout.isRefreshing &&
                    newState == RecyclerView.SCROLL_STATE_DRAGGING &&
                    !bottomReached
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        runOnUiThread { recyclerView.adapter = adapter }
    }

    fun onRefresh(listener: SwipeRefreshLayout.OnRefreshListener) {
        swipeRefreshLayout.apply {
            setOnRefreshListener(listener)
            isEnabled = true
        }
    }

    fun onLoadMore(loadMore: OnLoadMoreListener) {
        onLoadMoreListener = loadMore
    }

    private fun goOnLoad() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLoadTimestamp > 100) {
            swipeRefreshLayout.isRefreshing = true
            page++
            onLoadMoreListener?.onLoad(page)
            lastLoadTimestamp = currentTime
        }
    }

    fun runOnUiThread(runnable: Runnable) {
        takeIf { isAdded }?.activity?.runOnUiThread(runnable)
    }

    fun showEmptyView() {
        emptyView?.let {
            runOnUiThread {
                recyclerView.visibility = View.GONE
                it.visibility = View.VISIBLE
            }
        }
    }

    fun report(e: Throwable) = MsgUtil.error(e)

    fun loadFail() {
        page--
        MsgUtil.showMsgLong(getString(R.string.load_failed))
        refreshing = false
    }

    fun loadFail(e: Throwable) {
        page--
        report(e)
        refreshing = false
    }

    open fun getLayoutManager() = if (shouldUseGridLayout()) {
        CustomGridManager(requireContext(), 3)
    } else {
        CustomLinearManager(requireContext())
    }

    private fun shouldUseGridLayout() =
        Preferences.getBoolean("ui_landscape", false) && !forceSingleColumn

    fun forceSingleColumn() {
        forceSingleColumn = true
    }
}