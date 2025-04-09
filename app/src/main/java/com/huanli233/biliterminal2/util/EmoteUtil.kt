package com.huanli233.biliterminal2.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import com.bumptech.glide.Glide
import com.huanli233.biliwebapi.bean.content.EmoteContent
import java.util.concurrent.ExecutionException

object EmoteUtil {
    @Throws(ExecutionException::class, InterruptedException::class)
    fun textReplaceEmote(
        text: String,
        emotes: Map<String, EmoteContent>,
        scale: Float,
        context: Context,
        source: CharSequence?
    ): SpannableString {
        val result = if ((source !is SpannableString)) SpannableString(text) else source
        emotes.forEach { (emoteName, emoteContent) ->
            replaceSingle(
                origText = text,
                spannableString = result,
                name = emoteName,
                url = emoteContent.url,
                size = emoteContent.meta.size,
                scale = scale,
                context = context
            )
        }
        return result
    }

    @JvmStatic
    fun replaceSingle(
        origText: String,
        spannableString: SpannableString,
        name: String,
        url: String?,
        size: Int,
        scale: Float,
        context: Context
    ) {
        val drawable: Drawable?
        try {
            drawable = Glide.with(context).asDrawable().load(url).submit().get()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        drawable.setBounds(
            0,
            0,
            (size * Utils.sp2px(18f) * scale).toInt(),
            (size * Utils.sp2px(18f) * scale).toInt()
        )

        var start = origText.indexOf(name)
        while (start >= 0) {
            val end = start + name.length
            val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
            spannableString.setSpan(
                imageSpan,
                start,
                end,
                SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE
            )
            start = origText.indexOf(name, end)
        }
    }
}
