package com.huanli233.bilizepam.utils.parser

import androidx.core.text.buildSpannedString
import com.huanli233.biliwebapi.bean.content.BIZ_ID_MENTION
import com.huanli233.biliwebapi.bean.content.BIZ_ID_TEXT
import com.huanli233.biliwebapi.bean.content.ContentElement
import com.huanli233.bilizepam.ui.span.LinkClickableSpan
import com.huanli233.bilizepam.ui.span.appendClickableSpan

object ContentElementParser {

    fun parseDescription(elements: List<ContentElement>) = buildSpannedString {
        elements.forEach { element ->
            when (element.bizId) {
                BIZ_ID_TEXT -> append(element.rawText)
                BIZ_ID_MENTION -> appendClickableSpan("@${element.rawText}") {
                    // TODO jump to user page
                }
                else -> append(element.rawText)
            }
        }
    }

}