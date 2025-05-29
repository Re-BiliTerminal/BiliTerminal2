package com.huanli233.biliterminal2.utils.parser

import androidx.core.text.buildSpannedString
import com.huanli233.biliwebapi.bean.content.BIZ_ID_MENTION
import com.huanli233.biliwebapi.bean.content.BIZ_ID_TEXT
import com.huanli233.biliwebapi.bean.content.ContentElement

object ContentElementParser {

    fun parseDescription(elements: List<ContentElement>) = buildSpannedString {
        elements.forEach { element ->
            when (element.bizId) {
                BIZ_ID_TEXT -> append(element.rawText)
                BIZ_ID_MENTION -> TODO("Mention span")
            }
        }
    }

}