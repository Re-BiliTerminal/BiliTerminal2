package com.huanli233.biliterminal2.util

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.huanli233.biliterminal2.BuildConfig
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.CopyTextActivity
import com.huanli233.biliterminal2.activity.user.info.UserInfoActivity
import com.huanli233.biliterminal2.contextNotNull
import com.huanli233.biliterminal2.util.Preferences.getBoolean
import com.huanli233.biliwebapi.bean.dynamic.MentionTarget
import com.huanli233.biliwebapi.bean.user.UserInfo
import org.jsoup.Jsoup
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

@SuppressLint("ClickableViewAccessibility")
object Utils {
    /**
     * 单位转换
     *
     * @param num 原始数字
     * @return 转换后的字符串
     */
    @JvmStatic
    fun toWan(num: Long): String {
        return if (num >= 100000000) String.format(
            Locale.CHINA,
            "%.1f",
            num.toFloat() / 100000000
        ) + "亿"
        else if (num >= 10000) String.format(
            Locale.CHINA,
            "%.1f",
            num.toFloat() / 10000
        ) + "万"
        else num.toString()
    }

    @JvmStatic
    fun toTime(progress: Int): String {
        val cghour = progress / 3600
        val cgminute = (progress % 3600) / 60
        val cgsecond = progress % 60

        val cghourStr: String = if (cghour < 10) "0$cghour"
        else cghour.toString()

        val cgminStr: String = if (cgminute < 10) "0$cgminute"
        else cgminute.toString()

        val cgsecStr: String = if (cgsecond < 10) "0$cgsecond"
        else cgsecond.toString()

        return if (cghour > 0) "$cghourStr:$cgminStr:$cgsecStr"
        else "$cgminStr:$cgsecStr"
    }

    @JvmStatic
    fun htmlToString(html: String): String {
        return html.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
            .replace("&amp;", "&").replace("&#39;", "'").replace("&#34;", "\"")
            .replace("&#38;", "&").replace("&#60;", "<").replace("&#62;", ">")
    }

    @JvmStatic
    fun htmlReString(html: String): String {
        return html.replace("<p>", "")
            .replace("</p>", "\n")
            .replace("<br>", "\n")
            .replace("<em class=\"keyword\">", "")
            .replace("</em>", "")
    }

    @JvmStatic
    fun removeHtml(html: String?): String {
        return Jsoup.parse(html).text()
    }

    @JvmStatic
    fun unEscape(str: String): String {
        return str.replace("\\\\(.)".toRegex(), "$1")
    }

    @JvmStatic
    fun dp2px(dpValue: Float): Int {
        val scale = contextNotNull.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    @Suppress("DEPRECATION")
    fun sp2px(spValue: Float): Int {
        val fontScale = contextNotNull.resources
            .displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    @JvmStatic
    fun copyable(textView: TextView, customText: String?) {
        if (getBoolean("copy_enable", true)) {
            textView.setOnLongClickListener {
                val intent = Intent(
                    textView.context,
                    CopyTextActivity::class.java
                )
                intent.putExtra("content", customText ?: textView.text.toString())
                textView.context.startActivity(intent)
                true
            }
        }
    }

    fun copyable(textView: TextView) {
        copyable(textView, null)
    }

    @JvmStatic
    fun copyable(vararg textViews: TextView) {
        for (textView: TextView in textViews) copyable(textView)
    }

    @JvmStatic
    fun copyText(context: Context, str: String?) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("Label", str)
        cm.setPrimaryClip(mClipData)
    }

    @JvmStatic
    fun setLink(vararg textViews: TextView) {
        if (!getBoolean(Preferences.LINK_ENABLE, true)) return
        for (textView: TextView in textViews) {
            if (TextUtils.isEmpty(textView.text)) continue
            val text = textView.text.toString()
            val spannableString = SpannableString(textView.text)

            val urlPattern =
                Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]")
            val urlMatcher = urlPattern.matcher(text)
            while (urlMatcher.find()) {
                val start = urlMatcher.start()
                val end = urlMatcher.end()
                spannableString.setSpan(
                    LinkClickableSpan(text.substring(start, end), LinkUrlUtil.TYPE_WEB_URL),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            var matcher: Matcher = LinkUrlUtil.BV_PATTERN.matcher(text)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                spannableString.setSpan(
                    LinkClickableSpan(text.substring(start, end), LinkUrlUtil.TYPE_BVID),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            matcher = LinkUrlUtil.AV_PATTERN.matcher(text)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                spannableString.setSpan(
                    LinkClickableSpan(text.substring(start, end), LinkUrlUtil.TYPE_AVID),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            matcher = LinkUrlUtil.CV_PATTERN.matcher(text)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                spannableString.setSpan(
                    LinkClickableSpan(text.substring(start, end), LinkUrlUtil.TYPE_CVID),
                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            textView.text = spannableString
            textView.setOnTouchListener(ClickableSpanTouchListener())
        }
    }

    fun setMentionLink(atUserUids: Map<String, Long>?, vararg textViews: TextView) {
        if (atUserUids.isNullOrEmpty()) return
        for (textView: TextView in textViews) {
            if (TextUtils.isEmpty(textView.text)) continue
            val text = textView.text.toString()
            val spannableString = SpannableString(textView.text)

            for (entry: Map.Entry<String, Long> in atUserUids.entries) {
                val pattern = Pattern.compile("@${entry.key}")
                val matcher = pattern.matcher(text)
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    spannableString.setSpan(
                        LinkClickableSpan(
                            text.substring(start, end),
                            LinkUrlUtil.TYPE_USER,
                            entry.value.toString()
                        ),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            textView.text = spannableString
            textView.setOnTouchListener(ClickableSpanTouchListener())
        }
    }

    fun setMentionLink(ats: List<MentionTarget>?, vararg textViews: TextView) {
        if (ats.isNullOrEmpty()) return
        for (textView: TextView in textViews) {
            if (TextUtils.isEmpty(textView.text)) continue
            val text = textView.text.toString()
            val spannableString = SpannableString(textView.text)

            for (at: MentionTarget in ats) {
                spannableString.setSpan(
                    LinkClickableSpan(
                        text.substring(at.second, at.third),
                        LinkUrlUtil.TYPE_USER,
                        at.first.toString()
                    ),
                    at.second, at.third, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            textView.text = spannableString
            textView.setOnTouchListener(ClickableSpanTouchListener())
        }
    }


    val isDebugBuild: Boolean
        get() = BuildConfig.BETA

    val levelBadges: IntArray = intArrayOf(
        R.mipmap.level_0,
        R.mipmap.level_1,
        R.mipmap.level_2,
        R.mipmap.level_3,
        R.mipmap.level_4,
        R.mipmap.level_5,
        R.mipmap.level_6,
        R.mipmap.level_h
    )

    @JvmStatic
    fun getLevelBadge(context: Context, userInfo: UserInfo): ImageSpan {
        var level = userInfo.level

        if (level <= -1 || level >= 7) level = 0
        if (userInfo.isSeniorMember == 1) level = 7

        val drawable = getDrawable(context, levelBadges[level])

        val lineHeight = getTextHeightWithSize(context)
        var lineWidth = lineHeight * 1.56f
        if (userInfo.isSeniorMember == 1) lineWidth = lineHeight * 1.96f
        drawable.setBounds(0, 0, lineWidth.toInt(), lineHeight.toInt())
        return ImageSpan(drawable)
    }

    fun getTextHeightWithSize(context: Context): Float {
        val paint = Paint()
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            12f,
            context.resources.displayMetrics
        )
        val fontMetrics = paint.fontMetrics
        return fontMetrics.descent - fontMetrics.ascent
    }

    fun getRgb888(color: Int): Int {
        val stringBuilder = StringBuilder()
        stringBuilder.append((color shr 16) and 0xff)
        stringBuilder.append((color shr 8) and 0xff)
        stringBuilder.append((color) and 0xff)
        Log.e("颜色", stringBuilder.toString())
        return stringBuilder.toString().toInt()
    }

    @JvmStatic @Suppress("DEPRECATION")
    fun getDrawable(context: Context, @DrawableRes res: Int): Drawable {
        return ResourcesCompat.getDrawable(context.resources, res, context.theme)
            ?: BitmapDrawable()
    }

    class LinkClickableSpan @JvmOverloads constructor(
        private val text: String, private val type: Int,
        /**
         * 真实值
         */
        private val `val`: String? = null
    ) :
        ClickableSpan() {
        override fun onClick(widget: View) {
            when (type) {
                LinkUrlUtil.TYPE_USER -> widget.context.startActivity(
                    Intent(
                        widget.context,
                        UserInfoActivity::class.java
                    ).putExtra("mid", `val`!!.toLong())
                )

                LinkUrlUtil.TYPE_WEB_URL -> LinkUrlUtil.handleWebURL(widget.context, text)
                LinkUrlUtil.TYPE_BVID -> TerminalContext.getInstance().enterVideoDetailPage(
                    widget.context,
                    text
                )

                LinkUrlUtil.TYPE_AVID -> TerminalContext.getInstance()
                    .enterVideoDetailPage(widget.context, text.replace("av", "").toLong())

                LinkUrlUtil.TYPE_CVID -> TerminalContext.getInstance()
                    .enterArticleDetailPage(widget.context, text.replace("cv", "").toLong())
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            ds.color = Color.rgb(0x66, 0xcc, 0xff)
        }
    }

    // 查到的一种LinkMovementMethod问题的解决方法
    class ClickableSpanTouchListener : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (v !is TextView) {
                return false
            }
            val text = v.text as? Spanned ?: return false
            val action = event.action
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                var x = event.x.toInt()
                var y = event.y.toInt()

                x -= v.totalPaddingLeft
                y -= v.totalPaddingTop

                x += v.scrollX
                y += v.scrollY

                val layout = v.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())

                val links = text.getSpans(
                    off, off,
                    ClickableSpan::class.java
                )

                if (links.isNotEmpty()) {
                    val link = links[0]
                    if (action == MotionEvent.ACTION_UP) {
                        link.onClick(v)
                    }
                    return true
                }
            }
            return false
        }
    }
}

fun String.htmlToString(): String {
    return Utils.htmlToString(this)
}