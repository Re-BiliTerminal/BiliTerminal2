package com.huanli233.biliterminal2.adapter.video

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.elvishew.xlog.XLog
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.bean.VideoCardKt
import com.huanli233.biliterminal2.ui.widget.recyclerView.BaseHolder
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture
import com.huanli233.biliterminal2.util.Utils

class VideoCardHolder(itemView: View) : BaseHolder(itemView) {
    var title: TextView =
        itemView.findViewById(R.id.text_title)
    var upName: TextView =
        itemView.findViewById(R.id.text_upname)
    var viewCount: TextView =
        itemView.findViewById(R.id.text_viewcount)
    var cover: ImageView =
        itemView.findViewById(R.id.img_cover)

    fun bindData(videoCard: VideoCardKt, context: Context) {
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
                    coverUrl?.let {
                        url -> cover.loadPicture(url) {
                            apply(RequestOptions.bitmapTransform(RoundedCorners(Utils.dp2px(3f)))
                                .sizeMultiplier(0.85f))
                        }
                    }
                }
            }
        }.onFailure {
            XLog.e("load video cover failed", it)
        }

        when (videoCard.type) {
            "live" -> {
                title.text = SpannableString(context.getString(R.string.prefix_live) + Utils.htmlToString(videoCard.title.orEmpty())).apply {
                    setSpan(
                        ForegroundColorSpan(Color.rgb(207, 75, 95)),
                        0,
                        4,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }

            "series" -> {
                title.text = SpannableString(context.getString(R.string.prefix_series) + Utils.htmlToString(videoCard.title.orEmpty())).apply {
                    setSpan(
                        ForegroundColorSpan(Color.rgb(207, 75, 95)),
                        0,
                        4,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }

            else -> title.text = Utils.htmlToString(videoCard.title.orEmpty())
        }
    }
}
