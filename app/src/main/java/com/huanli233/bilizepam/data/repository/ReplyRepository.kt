package com.huanli233.bilizepam.data.repository

import android.util.Log
import com.huanli233.bilizepam.api.apiResultNonNull
import com.huanli233.bilizepam.api.bilibiliApi
import com.huanli233.biliwebapi.api.interfaces.IReplyApi
import com.huanli233.biliwebapi.bean.reply.PaginationStr
import com.huanli233.biliwebapi.bean.reply.RepliesInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReplyRepository @Inject constructor() {
    
    suspend fun getReplies(
        type: Int,
        oid: Long,
        mode: Int = 3,
        paginationStr: PaginationStr? = null,
        extraParams: Map<String, String> = emptyMap()
    ): Result<RepliesInfo> {
        val paginationJson = let {
            val offset = paginationStr?.offset.orEmpty()
            """{"offset":"$offset"}"""
        }
        
        android.util.Log.d("ReplyRepository", "getReplies - type: $type, oid: $oid, mode: $mode")
        android.util.Log.d("ReplyRepository", "paginationStr: $paginationStr")
        android.util.Log.d("ReplyRepository", "paginationJson: $paginationJson")
        android.util.Log.d("ReplyRepository", "extraParams: $extraParams")
        
        val result = bilibiliApi.api(IReplyApi::class) {
            getReplies(type, oid, mode, paginationJson, extraParams)
        }.apiResultNonNull()
        
        return result
    }

    suspend fun likeReply(
        oid: Long,
        replyId: Long,
        action: Int
    ): Result<Unit> {
        return bilibiliApi.api(IReplyApi::class) {
            likeReply(oid, replyId, action)
        }.apiResultNonNull()
    }

    suspend fun getReplyCount(oid: Long, type: Int): Result<Int> {
        return bilibiliApi.api(IReplyApi::class) {
            getReplyCount(oid, type)
        }.apiResultNonNull().map { it.count }
    }

    suspend fun sendReply(
        oid: Long,
        rpid: Long,
        parent: Long,
        message: String,
        type: Int = 1
    ): Result<com.huanli233.biliwebapi.bean.reply.Reply> {
        val extraParams = mutableMapOf<String, String>()
        if (rpid != 0L) {
            extraParams["root"] = rpid.toString()
            extraParams["parent"] = parent.toString()
        }
        
        return bilibiliApi.api(IReplyApi::class) {
            sendReply(oid, type, message, extraParams)
        }.apiResultNonNull().map { it.reply!! }
    }

    suspend fun getRootReply(
        oid: Long,
        rpid: Long,
        type: Int = 1
    ): Result<com.huanli233.biliwebapi.bean.reply.Reply> {
        // 按照BiliClient的方式，使用/x/v2/reply/reply接口获取根评论
        return bilibiliApi.api(IReplyApi::class) {
            getRootReply(type, oid, rpid)
        }.apiResultNonNull().mapCatching { childRepliesInfo ->
            // BiliClient从data.root字段获取根评论
            childRepliesInfo.root
        }
    }
    
    private fun findTargetReply(
        repliesInfo: RepliesInfo, 
        targetId: Long
    ): com.huanli233.biliwebapi.bean.reply.Reply? {
        // 1. 从replies中查找
        repliesInfo.replies?.firstOrNull { it.replyId == targetId }?.let { return it }
        
        // 2. 从topReplies中查找（如果是置顶评论）
        repliesInfo.topReplies?.firstOrNull { it.replyId == targetId }?.let { return it }
        
        // 3. 从hots中查找（如果是热门评论）
        repliesInfo.hots?.firstOrNull { it.replyId == targetId }?.let { return it }
        
        // 4. 递归查找所有评论及其子评论
        val allReplies = buildList {
            repliesInfo.replies?.let { addAll(it) }
            repliesInfo.topReplies?.let { addAll(it) }
            repliesInfo.hots?.let { addAll(it) }
        }
        
        return findReplyRecursively(allReplies, targetId)
    }
    
    private fun findReplyRecursively(replies: List<com.huanli233.biliwebapi.bean.reply.Reply>, targetId: Long): com.huanli233.biliwebapi.bean.reply.Reply? {
        for (reply in replies) {
            if (reply.replyId == targetId) {
                return reply
            }
            reply.replies?.let { childReplies ->
                val found = findReplyRecursively(childReplies, targetId)
                if (found != null) return found
            }
        }
        return null
    }

    suspend fun getChildReplies(
        oid: Long,
        rootId: Long,
        page: Int = 1,
        pageSize: Int = 20,
        type: Int = 1
    ): Result<com.huanli233.biliwebapi.bean.reply.ChildRepliesInfo> {
        // 按照BiliClient的方式，使用/x/v2/reply/reply接口获取子评论
        return bilibiliApi.api(IReplyApi::class) {
            getChildReplies(type, oid, rootId, page, pageSize, 0)
        }.apiResultNonNull()
    }
}
