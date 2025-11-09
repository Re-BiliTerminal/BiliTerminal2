package com.huanli233.bilizepam.ui.screens.comment

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.huanli233.bilizepam.data.repository.ReplyRepository
import com.huanli233.biliwebapi.bean.reply.Reply
import com.huanli233.biliwebapi.bean.reply.PaginationStr

class CommentPagingSource(
    private val replyRepository: ReplyRepository,
    private val oid: Long,
    private val type: Int = 1,
    private val mode: Int = 3,
    private val onTopRepliesLoaded: (Set<Long>) -> Unit = {}
) : PagingSource<String, Reply>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Reply> {
        return try {
            val offset = params.key
            
            val paginationStr = if (offset != null) {
                PaginationStr(offset = offset)
            } else {
                null
            }
            
            val result = replyRepository.getReplies(
                type = type,
                oid = oid,
                mode = mode,
                paginationStr = paginationStr
            )
            
            result.fold(
                onSuccess = { repliesInfo ->
                    if (offset == null) {
                        val topReplyIds = mutableSetOf<Long>()
                        repliesInfo.topReplies.forEach { topReplyIds.add(it.replyId) }
                        onTopRepliesLoaded(topReplyIds)
                    }
                    
                    val allReplies = buildList {
                        if (offset == null) {
                            addAll(repliesInfo.topReplies)
                            repliesInfo.hots?.let { addAll(it) }
                        }
                        repliesInfo.replies?.let { addAll(it) }
                    }

                    val nextOffset = repliesInfo.cursor.paginationReply.nextOffset
                    
                    LoadResult.Page(
                        data = allReplies,
                        prevKey = null,
                        nextKey = if (!repliesInfo.cursor.isEnd && nextOffset.isNotEmpty()) nextOffset else null
                    )
                },
                onFailure = { error ->
                    LoadResult.Error(error)
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Reply>): String? {
        return null // 刷新时总是从头开始
    }
}
