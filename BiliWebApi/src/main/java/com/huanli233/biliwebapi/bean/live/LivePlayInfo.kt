package com.huanli233.biliwebapi.bean.live

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LivePlayInfo(
    @SerializedName("room_id")
    val roomId: Long,
    @SerializedName("short_id")
    val shortId: Long,
    val uid: Long,
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    @SerializedName("is_locked")
    val isLocked: Boolean,
    @SerializedName("is_portrait")
    val isPortrait: Boolean,
    @SerializedName("live_status")
    val liveStatus: Int,
    @SerializedName("hidden_till")
    val hiddenTill: Long,
    @SerializedName("lock_till")
    val lockTill: Long,
    val encrypted: Boolean,
    @SerializedName("pwd_verified")
    val pwdVerified: Boolean,
    @SerializedName("live_time")
    val liveTime: Long,
    @SerializedName("room_shield")
    val roomShield: Int,
    @SerializedName("all_special_types")
    val allSpecialTypes: List<Int>,
    @SerializedName("playurl_info")
    val playurlInfo: PlayurlInfo? = null,
    @SerializedName("official_type")
    val officialType: Int,
    @SerializedName("official_room_id")
    val officialRoomId: Long,
    @SerializedName("risk_with_delay")
    val riskWithDelay: Int,
    @SerializedName("multi_screen_info")
    val multiScreenInfo: String
) : Parcelable

@Parcelize
data class PlayurlInfo(
    @SerializedName("conf_json")
    val confJson: String,
    val playurl: Playurl
) : Parcelable

@Parcelize
data class Playurl(
    val cid: Long,
    @SerializedName("g_qn_desc")
    val gQnDesc: List<QnDesc>,
    val stream: List<Stream>,
    @SerializedName("p2p_data")
    val p2pData: P2pData,
    @SerializedName("dolby_qn")
    val dolbyQn: Int? = null
) : Parcelable

@Parcelize
data class QnDesc(
    val qn: Int,
    val desc: String,
    @SerializedName("hdr_desc")
    val hdrDesc: String,
    @SerializedName("attr_desc")
    val attrDesc: String? = null,
    @SerializedName("hdr_type")
    val hdrType: Int
) : Parcelable

@Parcelize
data class Stream(
    @SerializedName("protocol_name")
    val protocolName: String,
    val format: List<Format>
) : Parcelable

@Parcelize
data class Format(
    @SerializedName("format_name")
    val formatName: String,
    val codec: List<Codec>,
    @SerializedName("master_url")
    val masterUrl: String
) : Parcelable

@Parcelize
data class Codec(
    @SerializedName("codec_name")
    val codecName: String,
    @SerializedName("current_qn")
    val currentQn: Int,
    @SerializedName("accept_qn")
    val acceptQn: List<Int>,
    @SerializedName("base_url")
    val baseUrl: String,
    @SerializedName("url_info")
    val urlInfo: List<UrlInfo>,
    @SerializedName("hdr_qn")
    val hdrQn: Int? = null,
    @SerializedName("dolby_type")
    val dolbyType: Int,
    @SerializedName("attr_name")
    val attrName: String,
    @SerializedName("hdr_type")
    val hdrType: Int
) : Parcelable

@Parcelize
data class UrlInfo(
    val host: String,
    val extra: String,
    @SerializedName("stream_ttl")
    val streamTtl: Int
) : Parcelable

@Parcelize
data class P2pData(
    val p2p: Boolean,
    @SerializedName("p2p_type")
    val p2pType: Int,
    @SerializedName("m_p2p")
    val mP2p: Boolean,
    @SerializedName("m_servers")
    val mServers: List<String>? = null
) : Parcelable
