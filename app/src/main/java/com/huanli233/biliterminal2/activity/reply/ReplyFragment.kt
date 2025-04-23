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

    private data class ReplyState(
        var isInitialLoadingDisabled: Boolean = false,
        var contentId: Long = 0,
        var authorId: Long = -1,
        var sortMode: Int = DEFAULT_SORT_MODE,
        var replyType: Int = ReplyApi.REPLY_TYPE_VIDEO,
        var seekPosition: Long = -1,
        var paginationToken: String = ""
    )

    private val state = ReplyState()
    private val replies = mutableListOf<Reply>()
    private var replyAdapter: ReplyAdapter? = null
    private var contentSource: Any? = null

    companion object {
        private const val DEFAULT_SORT_MODE = 3
        private const val KEY_CONTENT_ID = "content_id"
        private const val KEY_REPLY_TYPE = "reply_type"
        private const val KEY_DISABLE_INITIAL_LOAD = "disable_initial_load"
        private const val KEY_SEEK_POSITION = "seek_reply"
        private const val KEY_AUTHOR_ID = "author_id"

        @JvmStatic
        fun newInstance(
            oid: Long,
            replyType: Int,
            disableInitialLoad: Boolean = false,
            seekReply: Long = -1,
            authorId: Long = -1
        ) = ReplyFragment().apply {
            arguments = bundleOf(
                KEY_CONTENT_ID to oid,
                KEY_REPLY_TYPE to replyType,
                KEY_DISABLE_INITIAL_LOAD to disableInitialLoad.takeIf { it },
                KEY_SEEK_POSITION to seekReply.takeIf { it != -1L },
                KEY_AUTHOR_ID to authorId.takeIf { it != -1L }
            ).apply { removeNullValues() }
        }

        private fun Bundle.removeNullValues() {
            keySet().filter { get(it) == null }.forEach { remove(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeState()
    }

    private fun initializeState() {
        arguments?.run {
            state.contentId = getLong(KEY_CONTENT_ID)
            state.replyType = getInt(KEY_REPLY_TYPE, ReplyApi.REPLY_TYPE_VIDEO)
            state.isInitialLoadingDisabled = getBoolean(KEY_DISABLE_INITIAL_LOAD, false)
            state.seekPosition = getLong(KEY_SEEK_POSITION, -1)
            state.authorId = getLong(KEY_AUTHOR_ID, -1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        forceSingleColumn()
        super.onViewCreated(view, savedInstanceState)

        setupLandscapePadding(view.context)
        setupRefreshListeners()
        initializeReplyList()
    }

    @Suppress("DEPRECATION")
    private fun setupLandscapePadding(context: Context) {
        if (!Preferences.getBoolean("ui_landscape", false)) return

        val metrics = DisplayMetrics().also { displayMetrics ->
            (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    getRealMetrics(displayMetrics)
                } else {
                    getMetrics(displayMetrics)
                }
            }
        }

        val horizontalPadding = metrics.widthPixels / 6
        recyclerView.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    private fun setupRefreshListeners() {
        onRefresh { refresh(state.contentId) }
        onLoadMore(::loadMoreReplies)
    }

    private fun initializeReplyList() {
        if (!state.isInitialLoadingDisabled) {
            ThreadManager.run(::loadInitialReplies)
        }
    }

    private fun loadInitialReplies() = lifecycleScope.launch {
        loadReplies(isInitialLoad = true)
    }

    private suspend fun loadReplies(isInitialLoad: Boolean = false) {
        bilibiliApi.api(IReplyApi::class) {
            getReplies(
                type = state.replyType,
                oid = state.contentId,
                paginationStr = PaginationStr(state.paginationToken),
                mode = state.sortMode,
                extraParams = if (isInitialLoad && state.seekPosition > 0) {
                    mapOf("seek_rpid" to state.seekPosition)
                } else {
                    emptyMap()
                }
            )
        }.apiResultNonNull().onSuccess { response ->
            handleReplyResponse(response, isInitialLoad)
        }
    }

    private fun handleReplyResponse(response: RepliesInfo, isInitialLoad: Boolean) {
        if (!isAdded) return

        state.paginationToken = response.cursor.paginationReply.nextOffset
        refreshing = false

        if (isInitialLoad) {
            if (response.cursor.isBegin) {
                replies.addAll(response.topReplies)
            }
            response.replies?.let { replies.addAll(it) }

            if (response.cursor.isEnd || response.cursor.paginationReply.nextOffset.isEmpty()) {
                bottomReached = true
            }

            initializeAdapter()
        } else {
            val startPosition = replies.size
            response.replies?.let { replies.addAll(it) }

            if (response.cursor.isEnd || response.cursor.paginationReply.nextOffset.isEmpty()) {
                bottomReached = true
            }

            replyAdapter?.notifyItemRangeInserted(startPosition, response.replies?.size ?: 0)
        }
    }

    private fun initializeAdapter() {
        replyAdapter = createReplyAdapter().apply {
            this.source = this@ReplyFragment.contentSource
            setOnSortSwitch()
            setAdapter(this)
        }
    }

    fun setContentSource(source: Any?) {
        this.contentSource = source
        replyAdapter?.source = source
    }

    private fun createReplyAdapter() = ReplyAdapter(
        context = requireContext(),
        lifecycleOwner = requireActivity(),
        replyList = replies,
        oid = state.contentId,
        root = 0,
        type = state.replyType,
        sort = state.sortMode,
        exitDetail = { activity?.finish() },
        source = contentSource,
        upMid = state.authorId
    )

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoreReplies(page: Int) = lifecycleScope.launch {
        loadReplies(isInitialLoad = false)
    }

    fun notifyReplyInserted(replyEvent: ReplyEvent) {
        if (replyEvent.oid != state.contentId) return

        replyEvent.message.let { reply ->
            when {
                reply.root == 0L -> processRootReplyAddEvent(reply)
                replyEvent.pos >= 0 -> processChildReplyAddEvent(replyEvent)
            }
        }
    }

    private fun processRootReplyAddEvent(reply: Reply) {
        (recyclerView.layoutManager as? LinearLayoutManager)?.let { layoutManager ->
            val insertPosition = max(layoutManager.findFirstCompletelyVisibleItemPosition(), 0)
            replies.add(insertPosition, reply)
            runOnUiThread {
                replyAdapter?.notifyItemInserted(insertPosition)
                replyAdapter?.notifyItemRangeChanged(insertPosition, replies.size - insertPosition + 1)
                layoutManager.scrollToPositionWithOffset(insertPosition + 1, 0)
            }
        }
    }

    private fun processChildReplyAddEvent(replyEvent: ReplyEvent) {
        replies[replyEvent.pos] = replies[replyEvent.pos].run {
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
    fun refresh(contentId: Long) {
        state.paginationToken = ""
        state.contentId = contentId
        refreshing = true

        lifecycleScope.launch {
            loadReplies(isInitialLoad = true)
        }
    }

    private fun setOnSortSwitch() {
        replyAdapter?.onSortChangeListener = {
            state.sortMode = if (state.sortMode == 2) 3 else 2
            replyAdapter?.sort = state.sortMode
            refresh(state.contentId)
        }
    }
}