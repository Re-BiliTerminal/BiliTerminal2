package com.huanli233.bilizepam.ui.screens.comment

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.ReplyRepository
import com.huanli233.biliwebapi.bean.reply.Reply

class CommentDetailPagingSource(
    private val replyRepository: ReplyRepository,
    private val oid: Long,
    private val rootReplyId: Long,
    private val type: Int = 1
) : PagingSource<Int, Reply>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Reply> {
        return try {
            val page = params.key ?: 1
            
            if (page == 1) {
                val rootResult = getRootReplyDirectly()
                
                rootResult.fold(
                    onSuccess = { rootReply ->
                        getChildRepliesAndCombine(rootReply, 1)
                    },
                    onFailure = { error ->
                        LoadResult.Error(error)
                    }
                )
            } else {
                getChildRepliesOnly(page)
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun getRootReplyDirectly(): Result<Reply> {
        return replyRepository.getRootReply(
            oid = oid,
            rpid = rootReplyId,
            type = type
        )
    }

    private suspend fun getChildRepliesAndCombine(rootReply: Reply, page: Int): LoadResult<Int, Reply> {
        return replyRepository.getChildReplies(
            oid = oid,
            rootId = rootReplyId,
            page = page,
            pageSize = 20,
            type = type
        ).fold(
            onSuccess = { childRepliesInfo ->
                val childReplies = childRepliesInfo.replies

                val dataToReturn = listOf(rootReply) + childReplies
                LoadResult.Page(
                    data = dataToReturn,
                    prevKey = null,
                    nextKey = if (childReplies.isEmpty() || childReplies.size < 20) null else 2
                )
            },
            onFailure = { error ->
                LoadResult.Page(
                    data = listOf(rootReply),
                    prevKey = null,
                    nextKey = null
                )
            }
        )
    }

    private suspend fun getChildRepliesOnly(page: Int): LoadResult<Int, Reply> {
        
        return replyRepository.getChildReplies(
            oid = oid,
            rootId = rootReplyId,
            page = page,
            pageSize = 20,
            type = type
        ).fold(
            onSuccess = { childRepliesInfo ->
                val childReplies = childRepliesInfo.replies
                LoadResult.Page(
                    data = childReplies,
                    prevKey = if (page == 2) null else page - 1,
                    nextKey = if (childReplies.isEmpty() || childReplies.size < 20) null else page + 1
                )
            },
            onFailure = { error ->
                LoadResult.Error(error)
            }
        )
    }

    private fun findReplyById(replies: List<Reply>, targetId: Long): Reply? {
        for (reply in replies) {
            if (reply.replyId == targetId) {
                return reply
            }
            reply.replies?.let { childReplies ->
                val found = findReplyById(childReplies, targetId)
                if (found != null) return found
            }
        }
        return null
    }

    override fun getRefreshKey(state: PagingState<Int, Reply>): Int? {
        return null
    }
}
