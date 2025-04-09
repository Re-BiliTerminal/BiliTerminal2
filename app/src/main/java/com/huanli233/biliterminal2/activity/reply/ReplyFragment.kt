package com.huanli233.biliterminal2.activity.reply

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.huanli233.biliterminal2.activity.base.RefreshListFragment
import com.huanli233.biliterminal2.adapter.ReplyAdapter
import com.huanli233.biliterminal2.api.ReplyApi
import com.huanli233.biliterminal2.api.apiResultNonNull
import com.huanli233.biliterminal2.api.bilibiliApi
import com.huanli233.biliterminal2.api.toResultNonNull
import com.huanli233.biliterminal2.event.ReplyEvent
import com.huanli233.biliterminal2.util.ThreadManager
import com.huanli233.biliterminal2.util.Preferences
import com.huanli233.biliwebapi.api.interfaces.IReplyApi
import com.huanli233.biliwebapi.bean.reply.PaginationStr
import com.huanli233.biliwebapi.bean.reply.RepliesInfo
import com.huanli233.biliwebapi.bean.reply.Reply
import kotlinx.coroutines.launch
import kotlin.math.max

open class ReplyFragment : RefreshListFragment() {

    private var dontLoad = false
    protected var aid: Long = 0
    protected var mid: Long = -1
    protected var sort = 3
    protected var type: Int = ReplyApi.REPLY_TYPE_VIDEO
    protected var replyList = mutableListOf<Reply>()
    protected var replyAdapter: ReplyAdapter? = null
    private var source: Any? = null
    private var seek: Long = -1
    private var pagination = ""

    companion object {
        @JvmStatic
        fun newInstance(
            aid: Long,
            type: Int,
            dontload: Boolean = false,
            seek: Long = -1,
            mid: Long = -1
        ) = ReplyFragment().apply {
            arguments = bundleOf(
                "aid" to aid,
                "type" to type,
                "dontload" to dontload.takeIf { it },
                "seek" to seek.takeIf { it != -1L },
                "mid" to mid.takeIf { it != -1L }
            ).apply { clearNullValues() }
        }

        private fun Bundle.clearNullValues() = keySet().filter { get(it) == null }.forEach { remove(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            aid = getLong("aid")
            type = getInt("type", ReplyApi.REPLY_TYPE_VIDEO)
            dontLoad = getBoolean("dontload", false)
            seek = getLong("seek", -1)
            mid = getLong("mid", -1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        forceSingleColumn()
        super.onViewCreated(view, savedInstanceState)

        setupLandscapePadding(view.context)
        setupRefreshListeners()
        initReplyList()
    }

    @Suppress("DEPRECATION")
    private fun setupLandscapePadding(context: Context) {
        if (Preferences.getBoolean("ui_landscape", false)) {
            val metrics = DisplayMetrics().also {
                (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) getRealMetrics(it)
                    else getMetrics(it)
                }
            }
            (metrics.widthPixels / 6).let { paddings ->
                recyclerView.setPadding(paddings, 0, paddings, 0)
            }
        }
    }

    private fun setupRefreshListeners() {
        onRefresh {
            refresh(aid)
        }
        onLoadMore(::continueLoading)
    }

    private fun initReplyList() {
        if (!dontLoad) ThreadManager.run(::loadInitialReplies)
    }

    private fun loadInitialReplies() = lifecycleScope.launch {
        bilibiliApi.api(IReplyApi::class) {
            getReplies(
                type = type,
                oid = aid,
                paginationStr = PaginationStr(pagination),
                mode = sort,
                extraParams = let {
                    if (seek > 0) mapOf("seek_rpid" to seek)
                    else emptyMap()
                }
            )
        }.apiResultNonNull().onSuccess {
            pagination = it.cursor.paginationReply.nextOffset
            refreshing = false
            if (isAdded) {
                if (it.cursor.isBegin) replyList.addAll(it.topReplies)
                it.replies?.let { replies -> replyList.addAll(replies) }
                if (it.cursor.isEnd || it.cursor.paginationReply.nextOffset.isEmpty()) bottomReached = true
                replyAdapter = createReplyAdapter().apply {
                    this.source = this@ReplyFragment.source
                    setOnSortSwitch()
                    setAdapter(this)
                }
            }
        }
    }

    fun setSource(source: Any?) {
        this.source = source
        replyAdapter?.source = source
    }

    private fun createReplyAdapter() = ReplyAdapter(
        context = requireContext(),
        lifecycleOwner = requireActivity(),
        replyList = replyList,
        oid = aid,
        root = 0,
        type = type,
        sort = sort,
        exitDetail = {
            activity?.finish()
        },
        source = source,
        upMid = mid
    )

    @SuppressLint("NotifyDataSetChanged")
    private fun continueLoading(page: Int) = lifecycleScope.launch {
        bilibiliApi.api(IReplyApi::class) {
            getReplies(
                type = type,
                oid = aid,
                mode = sort,
                paginationStr = PaginationStr(pagination),
                extraParams = emptyMap()
            )
        }.apiResultNonNull().onSuccess {
            pagination = it.cursor.paginationReply.nextOffset
            refreshing = false
            if (isAdded) {
                val positionStart = replyList.size
                it.replies?.let { replies -> replyList.addAll(replies) }
                if (it.cursor.isEnd || it.cursor.paginationReply.nextOffset.isEmpty()) bottomReached = true
                replyAdapter?.notifyItemRangeInserted(positionStart, it.replies?.size ?: 0)
            }
        }
    }

    fun notifyReplyInserted(replyEvent: ReplyEvent) {
        if (replyEvent.oid != aid) return

        replyEvent.message.let { reply ->
            when {
                reply.root == 0L -> handleRootReply(reply)
                replyEvent.pos >= 0 -> handleChildReply(replyEvent)
            }
        }
    }

    private fun handleRootReply(reply: Reply) {
        (recyclerView.layoutManager as? LinearLayoutManager)?.let { layoutManager ->
            val pos = max(layoutManager.findFirstCompletelyVisibleItemPosition(), 0)
            replyList.add(pos, reply)
            runOnUiThread {
                replyAdapter?.notifyItemInserted(pos)
                replyAdapter?.notifyItemRangeChanged(pos, replyList.size - pos + 1)
                layoutManager.scrollToPositionWithOffset(pos + 1, 0)
            }
        }
    }

    private fun handleChildReply(replyEvent: ReplyEvent) {
        replyList[replyEvent.pos] = replyList[replyEvent.pos].run {
            copy(
                replies = replies.orEmpty().toMutableList().apply {
                    add(replyEvent.message)
                },
                rcount = rcount + 1
            )
        }
        runOnUiThread {
            replyAdapter?.notifyItemChanged(replyEvent.pos + 1)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh(aid: Long) {
        pagination = ""
        this.aid = aid
        refreshing = true

        lifecycleScope.launch {
            bilibiliApi.api(IReplyApi::class) {
                getReplies(
                    type = type,
                    oid = aid,
                    mode = sort,
                    paginationStr = PaginationStr(pagination),
                    extraParams = emptyMap()
                )
            }.apiResultNonNull().onSuccess {
                pagination = it.cursor.paginationReply.nextOffset
                refreshing = false
                if (isAdded) {
                    val positionStart = replyList.size
                    replyList.clear()
                    if (it.cursor.isBegin) replyList.addAll(it.topReplies)
                    it.replies?.let { replies -> replyList.addAll(replies) }
                    if (it.cursor.isEnd || it.cursor.paginationReply.nextOffset.isEmpty()) bottomReached = true
                    replyAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setOnSortSwitch() {
        replyAdapter?.onSortChangeListener = {
            sort = if (sort == 2) 3 else 2
            replyAdapter?.sort = sort
            refresh(aid)
        }
    }
}