package com.ethanhua.skeleton

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout


/**
 * Created by ethanhua on 2017/7/29.
 */
class SkeletonAdapter : RecyclerView.Adapter<ViewHolder>() {
    var shape = Shape.LINEAR
    var repeatCount = -1
    var repeatDelay = 0L
    var repeatMode = RepeatMode.RESTART
    var layoutReference = 0
    var layoutArrayReferences: IntArray = IntArray(0)
    var useAlpha = true
    var direction = Direction.LEFT_TO_RIGHT
    var alpha = 1f
    var baseAlpha = 0.3f
    var color = 0
    var baseColor = 0
    var shimmer = false
    var shimmerDuration = 0L
    var mItemCount = 0
    @IntRange(from = 0, to = 30)
    var shimmerAngle = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (doesArrayOfLayoutsExist()) {
            layoutReference = viewType
        }
        return if (shimmer) {
            ShimmerViewHolder(inflater, parent, layoutReference)
        } else object : ViewHolder(inflater.inflate(layoutReference, parent, false)) {}
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (shimmer) {
            val shimmer = if (useAlpha) {
                Shimmer.AlphaHighlightBuilder()
                        .setBaseAlpha(baseAlpha)
                        .setHighlightAlpha(alpha)
            } else {
                Shimmer.ColorHighlightBuilder()
                        .setBaseColor(baseColor)
                        .setHighlightColor(color)
            }.apply {
                setTilt(shimmerAngle.toFloat())
                setDirection(direction.value)
                setDuration(shimmerDuration)
                setShape(shape.value)
                setRepeatCount(repeatCount)
                setRepeatMode(repeatMode.value)
                setRepeatDelay(repeatDelay)
            }.build()
            (holder.itemView as ShimmerFrameLayout).setShimmer(shimmer)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (doesArrayOfLayoutsExist()) {
            getCorrectLayoutItem(position)
        } else super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getCorrectLayoutItem(position: Int): Int {
        return if (doesArrayOfLayoutsExist()) {
            layoutArrayReferences[position % layoutArrayReferences.size]
        } else layoutReference
    }

    private fun doesArrayOfLayoutsExist(): Boolean {
        return layoutArrayReferences.isNotEmpty()
    }

    override fun getItemCount() = mItemCount

    fun setItemCount(itemCount: Int) {
        mItemCount = itemCount
    }
}