package com.huanli233.biliterminal2.adapter.video

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.elvishew.xlog.XLog
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.model.VideoCard
import com.huanli233.biliterminal2.util.GlideUtil
import com.huanli233.biliterminal2.util.ToolsUtil

class VideoCardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var title: TextView =
        itemView.findViewById(R.id.text_title)
    var upName: TextView =
        itemView.findViewById(R.id.text_upname)
    var viewCount: TextView =
        itemView.findViewById(R.id.text_viewcount)
    var cover: ImageView =
        itemView.findViewById(R.id.img_cover)

    fun showVideoCard(videoCard: VideoCard, context: Context) {
        videoCard.uploader?.let {
            if (it.isNotEmpty()) {
                upName.text = it
                return@let Unit
            } else {
                null
            }
        } ?: run {
            upName.visibility = View.GONE
        }

        videoCard.view?.let {
            if (it.isNotEmpty()) {
                viewCount.text = it
                return@let Unit
            } else {
                null
            }
        } ?: run {
            viewCount.visibility = View.GONE
        }

        runCatching {
            val coverUrl = videoCard.cover
            videoCard.cover?.let {
                if (it.isNotEmpty()) {
                    Glide.with(context).asDrawable().load(GlideUtil.url(coverUrl))
                        .transition(GlideUtil.getTransitionOptions())
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(256, 144)
                        .apply(
                            RequestOptions.bitmapTransform(RoundedCorners(ToolsUtil.dp2px(3f)))
                                .sizeMultiplier(0.85f)
                        )
                        .into(cover)
                }
            }
        }.onFailure {
            XLog.e("load video cover failed", it)
        }

        when (videoCard.type) {
            "live" -> {
                title.text = SpannableString(context.getString(R.string.prefix_live) + ToolsUtil.htmlToString(videoCard.title)).apply {
                    setSpan(
                        ForegroundColorSpan(Color.rgb(207, 75, 95)),
                        0,
                        4,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }

            "series" -> {
                title.text = SpannableString(context.getString(R.string.prefix_series) + ToolsUtil.htmlToString(videoCard.title)).apply {
                    setSpan(
                        ForegroundColorSpan(Color.rgb(207, 75, 95)),
                        0,
                        4,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }

            else -> title.text = ToolsUtil.htmlToString(videoCard.title)
        }
    }
}
