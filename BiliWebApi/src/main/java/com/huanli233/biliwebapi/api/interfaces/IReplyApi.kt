package com.huanli233.biliwebapi.api.interfaces

import com.huanli233.biliwebapi.bean.ApiResponse
import com.huanli233.biliwebapi.bean.CountResult
import com.huanli233.biliwebapi.bean.reply.PaginationStr
import com.huanli233.biliwebapi.bean.reply.RepliesInfo
import com.huanli233.biliwebapi.bean.reply.Reply
import com.huanli233.biliwebapi.bean.reply.ReplySendResult
import com.huanli233.biliwebapi.httplib.annotation.Csrf
import com.huanli233.biliwebapi.httplib.annotation.Fields
import com.huanli233.biliwebapi.httplib.annotation.Queries
import com.huanli233.biliwebapi.httplib.annotation.WbiSign
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

const val QUERY_KEY_SEEK_REPLY_ID = "seek_rpid"

interface IReplyApi {
    @WbiSign @GET("/x/v2/reply/wbi/main")
    @Queries(keys = ["plat", "web_location"], values = ["1", "1315875"])
    suspend fun getReplies(
        @Query("type") type: Int,
        @Query("oid") oid: Long,
        @Query("mode") mode: Int = 1,
        @Query("pagination_str") paginationStr: PaginationStr? = null,
        @QueryMap extraParams: Map<String, Any>
    ): ApiResponse<RepliesInfo>

    @Csrf @POST("/x/v2/reply/action")
    @FormUrlEncoded
    @Fields(keys = ["type", "jsonp"], values = ["1", "jsonp"])
    suspend fun likeReply(
        @Field("oid") oid: Long,
        @Field("rpid") replyId: Long,
        @Field("action") action: Int,
    ): ApiResponse<Unit>

    @GET("/x/v2/reply/count")
    suspend fun getReplyCount(
        @Query("oid") oid: Long,
        @Query("type") type: Int
    ): ApiResponse<CountResult>

    @Csrf @POST("/x/v2/reply/del")
    @FormUrlEncoded
    suspend fun deleteReply(
        @Field("type") type: Int,
        @Field("oid") oid: Long,
        @Field("rpid") replyId: Long
    ): ApiResponse<Nothing>

    @Csrf @POST("/x/v2/reply/add")
    @FormUrlEncoded
    @Fields(keys = ["jsonp"], values = ["jsonp"])
    suspend fun sendReply(
        @Field("oid") oid: Long,
        @Field("type") type: Int,
        @Field("message") content: String,
        @FieldMap extraParams: Map<String, Any>
    ): ApiResponse<ReplySendResult>
}