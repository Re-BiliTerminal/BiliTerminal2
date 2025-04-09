package com.huanli233.biliterminal2.event

import com.huanli233.biliwebapi.bean.reply.Reply

data class ReplyEvent(
    var type: Int,
    var message: Reply,
    var oid: Long,
    var pos: Int = 0,
)