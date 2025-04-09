package com.huanli233.biliterminal2.util

import android.content.Intent
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.activity.ImageViewerActivity
import com.huanli233.biliterminal2.activity.user.info.UserInfoActivity
import com.huanli233.biliterminal2.util.Preferences.getBoolean


object GlideUtil {
    const val QUALITY_HIGH: Int = 80
    const val QUALITY_LOW: Int = 15
    const val MAX_H_HIGH: Int = 4096
    const val MAX_W_HIGH: Int = 4096
    const val MAX_H_LOW: Int = 1024
    const val MAX_W_LOW: Int = 1024

    @JvmStatic
    @JvmOverloads
    fun ImageView.loadPicture(
        vararg url: Any,
        setClick: Boolean = false,
        option: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { this }
    ) {
        Glide.with(context).asDrawable().load(if (url[0] is String) url(url[0].toString()) else url[0])
            .transition(transitionOptions)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(Utils.dp2px(5f))))
            .placeholder(R.mipmap.placeholder)
            .run { option(this) }
            .format(DecodeFormat.PREFER_RGB_565)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(this)
        if (setClick) {
            setOnClickListener {
                context.startActivity(
                    Intent(
                        context,
                        ImageViewerActivity::class.java
                    ).putExtra(
                        "imageList", arrayListOf(*url)
                    )
                )
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun ImageView.load(
        url: String,
        option: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { this }
    ) {
        Glide.with(this).load(url)
            .transition(transitionOptions)
            .run { option(this) }
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(this)
    }

    @JvmStatic
    @JvmOverloads
    fun ImageView.loadResource(
        id: Int,
        option: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { this }
    ) {
        Glide.with(this).load(id)
            .transition(transitionOptions)
            .run { option(this) }
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(this)
    }

    @JvmStatic
    fun ImageView.loadFace(
        url: String,
        mid: Long? = null,
        option: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { this }
    ) = load(url(url)) {
        apply(RequestOptions.circleCropTransform())
            .placeholder(R.mipmap.akari)
            .run { option(this) }
    }.also {
        mid?.let { id ->
            setOnClickListener {
                context.startActivity(Intent(context, UserInfoActivity::class.java).apply {
                    putExtra("mid", id)
                })
            }
        }
    }

    @JvmStatic
    fun url(url: String): String {
        if (!url.startsWith("http") || url.endsWith("gif") || url.contains("@") || url.contains("afdiancdn.com")) return url
        if (getBoolean("image_request_jpg", false)) {
            if (url.endsWith("jpeg") || url.endsWith("jpg")) return url
            return (url + "@0e_"
                    + QUALITY_LOW + "q_" //+ MAX_H_LOW + "h_"
                    + MAX_W_LOW + "w.jpeg")
        } else {
            if (url.endsWith("webp")) return url
            return (url + "@0e_"
                    + QUALITY_LOW + "q_" //+ MAX_H_LOW + "h_"
                    + MAX_W_LOW + "w.webp")
        }
    }

    @JvmStatic
    fun urlHq(url: String): String {
        if (!url.startsWith("http") || url.endsWith("gif") || url.contains("@") || url.contains("afdiancdn.com")) return url
        if (getBoolean("image_request_jpg", false)) {
            if (url.endsWith("jpeg") || url.endsWith("jpg")) return url
            return (url + "@0e_"
                    + QUALITY_HIGH + "q_" //+ MAX_H_HIGH + "h_"
                    + MAX_W_HIGH + "w.jpeg")
        } else {
            if (url.endsWith("webp")) return url
            return (url + "@0e_"
                    + QUALITY_HIGH + "q_" //+ MAX_H_HIGH + "h_"
                    + MAX_W_HIGH + "w.webp")
        }
    }

    fun request(view: ImageView, url: String, placeholder: Int) {
        Glide.with(view).asDrawable().load(url(url))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .format(DecodeFormat.PREFER_RGB_565)
            .transition(transitionOptions)
            .placeholder(placeholder)
            .into(view)
    }

    fun requestRound(view: ImageView, url: String, placeholder: Int) {
        Glide.with(view).asDrawable().load(url(url))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .format(DecodeFormat.PREFER_RGB_565)
            .transition(transitionOptions)
            .placeholder(placeholder)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }

    fun request(view: ImageView, url: String, roundCorners: Int, placeholder: Int) {
        Glide.with(view).asDrawable().load(url(url))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .format(DecodeFormat.PREFER_RGB_565)
            .transition(transitionOptions)
            .placeholder(placeholder)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(Utils.dp2px(roundCorners.toFloat()))))
            .into(view)
    }

    @JvmStatic
    val transitionOptions: TransitionOptions<*, in Drawable?>
        get() = if (getBoolean(Preferences.LOAD_TRANSITION, true)) {
            DrawableTransitionOptions.with(
                DrawableCrossFadeFactory.Builder(300).setCrossFadeEnabled(true).build()
            )
        } else {
            DrawableTransitionOptions()
        }
}
