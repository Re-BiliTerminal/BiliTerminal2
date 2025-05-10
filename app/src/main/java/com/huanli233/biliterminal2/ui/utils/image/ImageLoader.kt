package com.huanli233.biliterminal2.ui.utils.image

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.proto.ImageFormat
import com.huanli233.biliterminal2.data.setting.LocalData
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private val shimmerDrawable
    get() = ShimmerDrawable().apply {
        setShimmer(shimmer)
    }

private val transitionOptions
    get() = DrawableTransitionOptions.withCrossFade()

fun <T: Drawable> RequestBuilder<T>.transition(): RequestBuilder<T> = this.transition(transitionOptions)
fun <T> RequestBuilder<T>.noDiskCache(): RequestBuilder<T> = this.diskCacheStrategy(DiskCacheStrategy.NONE)
private fun <T> RequestBuilder<T>.shimmerPlaceholder(
    @DrawableRes resId: Int
): RequestBuilder<T> = if (LocalData.settings.theme.animationsEnabled) {
    this.placeholder(shimmerDrawable)
} else {
    this.placeholder(resId)
}

fun RequestManager.loadAvatar(avatarUrl: String): RequestBuilder<Drawable> =
    this.load(avatarUrl.applyUrlParams(IMAGE_QUALITY_LOW, IMAGE_MAX_WIDTH_LOW))
        .placeholder(R.drawable.placeholder_gray_circle)
        .noDiskCache()
        .transition()
        .circleCrop()

fun RequestManager.loadPicture(
    pictureUrl: String,
    quality: Int = IMAGE_QUALITY_LOW,
    maxWidth: Int = IMAGE_MAX_WIDTH_LOW
): RequestBuilder<Drawable> =
    this.load(pictureUrl.applyUrlParams(quality, maxWidth))
        .shimmerPlaceholder(R.mipmap.placeholder)
        .noDiskCache()
        .transition()

private val shimmer = Shimmer.AlphaHighlightBuilder()
    .setDuration(1800)
    .setBaseAlpha(0.7f)
    .setHighlightAlpha(0.6f)
    .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
    .setAutoStart(true)
    .build()

const val IMAGE_QUALITY_FULL = 100
const val IMAGE_QUALITY_HIGH = 80
const val IMAGE_QUALITY_LOW = 25
const val IMAGE_MAX_WIDTH_HIGH = 1024
const val IMAGE_MAX_WIDTH_LOW = 512

private fun String.applyUrlParams(
    quality: Int,
    maxWidth: Int
): String {
    val httpUrl = toHttpUrlOrNull()
    val scheme = httpUrl?.scheme
    if (httpUrl == null || httpUrl.encodedPath.contains("@") || endsWith("gif") || scheme == null || (scheme != "http" && scheme != "https")) {
        return this
    }
    val format = when(LocalData.settings.preferences.imageFormat) {
        ImageFormat.IMAGE_FORMAT_JPEG -> "jpeg"
        else -> "webp"
    }
    return httpUrl.newBuilder()
        .encodedPath(httpUrl.encodedPath.let {
            it + "@0e_${quality}q_${maxWidth}w.${format}"
        })
        .build()
        .toString()
}