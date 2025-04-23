package com.huanli233.biliterminal2.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

const val TYPE_VIDEO: Int = 0
const val TYPE_BANGUMI: Int = 1
const val TYPE_LIVE: Int = 2
const val TYPE_LOCAL: Int = 4

@Parcelize
class PlayerData @JvmOverloads constructor(
    val type: Int = TYPE_VIDEO,
    var title: String = "",
    var urlVideo: String = "",
    var urlDanmaku: String = "",
    var urlSubtitle: String = "",
    var qn: Int = -1,
    var qnStrList: List<String> = emptyList(),
    var qnValueList: List<Int> = emptyList(),
    var aid: Long = -1,
    var bvid: String = "",
    var cid: Long = -1,
    var mid: Long = -1,
    var progress: Int = -1
) : Parcelable
