package com.huanli233.bilizepam.ui.span

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.highcapable.betterandroid.ui.extension.component.base.getThemeAttrsColor
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.applicationContext
import com.huanli233.bilizepam.ui.activity.video.VideoInfoActivity
import com.huanli233.bilizepam.utils.Patterns
import com.huanli233.bilizepam.utils.extensions.appendWithSpan
import splitties.activities.start
import splitties.intents.start

@Suppress("NOTHING_TO_INLINE")
inline fun SpannableStringBuilder.appendClickableSpan(
    text: String,
    noinline onClick: (View) -> Unit
) {
    this.appendWithSpan(text) {
        LinkClickableSpan(onClick)
    }
}

class LinkClickableSpan(
    val onClick: (View) -> Unit
): ClickableSpan() {

    override fun onClick(widget: View) {
        onClick.invoke(widget)
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false;
        ds.setColor(applicationContext.getThemeAttrsColor(com.google.android.material.R.attr.colorPrimaryFixedDim))
    }

}

class ClickableSpanTouchListener(private val origin: View.OnTouchListener? = null) : View.OnTouchListener {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (performTouch(v, event)) return true
        return origin?.onTouch(v, event) ?: false
    }

    private fun performTouch(v: View, event: MotionEvent): Boolean {
        if (v !is TextView) {
            return false
        }
        val widget = v
        val text = widget.getText()
        if (text !is Spanned) {
            return false
        }
        val buffer = text
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x
            var y = event.y

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line: Int = layout.getLineForVertical(y.toInt())
            val off: Int = layout.getOffsetForHorizontal(line, x)

            val links = buffer.getSpans(off, off, ClickableSpan::class.java)

            if (links.size != 0) {
                val link = links[0]
                if (action == MotionEvent.ACTION_UP) {
                    link.onClick(widget)
                }
                return true
            }
        }
        return false
    }
}

@SuppressLint("ClickableViewAccessibility")
fun TextView.linkable() = setOnTouchListener(ClickableSpanTouchListener())

fun TextView.setupLink(
    activity: Activity
) = apply {
    linkable()
    val spannable = SpannableString(text)

    val avMatcher = Patterns.AV_PATTERN.toRegex().toPattern().matcher(spannable)
    while (avMatcher.find()) {
        spannable.setSpan(
            LinkClickableSpan {
                context.start<VideoInfoActivity> {
                    putExtra("avid", avMatcher.group().substring(2).toIntOrNull() ?: 0)
                }
            },
            avMatcher.start(),
            avMatcher.end(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    val bvMatcher = Patterns.BV_PATTERN.toRegex().toPattern().matcher(spannable)
    while (bvMatcher.find()) {
        spannable.setSpan(
            LinkClickableSpan {
                context.start<VideoInfoActivity> {
                    putExtra("bvid", bvMatcher.group())
                }
            },
            bvMatcher.start(),
            bvMatcher.end(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    val cvMatcher = Patterns.CV_PATTERN.toRegex().toPattern().matcher(spannable)
    while (cvMatcher.find()) {
        spannable.setSpan(
            LinkClickableSpan {
                // TODO jump to article page
            },
            cvMatcher.start(),
            cvMatcher.end(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    val urlMatcher = Patterns.URL_PATTERN.toRegex().toPattern().matcher(spannable)
    while (urlMatcher.find()) {
        spannable.setSpan(
            LinkClickableSpan {
                // TODO Confirm dialog
            },
            urlMatcher.start(),
            urlMatcher.end(),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}