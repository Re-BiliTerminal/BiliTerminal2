package com.ethanhua.skeleton

import androidx.annotation.ArrayRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.skeleton.R

/**
 * Created by ethanhua on 2017/7/29.
 */
class RecyclerViewSkeletonScreen private constructor(builder: Builder) : SkeletonScreen {
    private val mRecyclerView = builder.mRecyclerView
    private val mActualAdapter = builder.mActualAdapter
    private val mSkeletonAdapter = SkeletonAdapter().apply {
        useAlpha = builder.mUseAlpha
        itemCount = builder.mItemCount
        layoutReference = builder.mSkeletonLayoutResID
        layoutArrayReferences = builder.mItemsResIDArray
        shimmer = builder.mShimmer
        alpha = builder.mShimmerAlpha
        baseAlpha = builder.mShimmerBaseAlpha
        color = builder.mShimmerColor
        baseColor = builder.mShimmerBaseColor
        shimmerAngle = builder.mShimmerAngle
        shimmerDuration = builder.mShimmerDuration
        direction = builder.mDirection
        shape = builder.mShimmerShape
        repeatCount = builder.mRepeatCount
        repeatDelay = builder.mRepeatDelay
        repeatMode = builder.mRepeatMode
    }
    private val mRecyclerViewFrozen = builder.mFrozen
    override fun show(): RecyclerViewSkeletonScreen {
        mRecyclerView.adapter = mSkeletonAdapter
        if (!mRecyclerView.isComputingLayout && mRecyclerViewFrozen) {
            mRecyclerView.suppressLayout(true)
        }
        return this
    }

    override fun hide() {
        mRecyclerView.adapter = mActualAdapter
    }

    class Builder(val mRecyclerView: RecyclerView) : SkeletonBuilder(mRecyclerView) {
        internal var mActualAdapter: RecyclerView.Adapter<*>? = null
        override var mSkeletonLayoutResID = R.layout.layout_default_item_skeleton
        internal var mDirection = Direction.LEFT_TO_RIGHT
        internal var mItemCount = 10
        internal var mItemsResIDArray: IntArray = IntArray(0)
        internal var mFrozen = true

        /**
         * @param adapter the target recyclerView actual adapter
         */
        fun adapter(adapter: RecyclerView.Adapter<*>?): Builder {
            mActualAdapter = adapter
            return this
        }

        /**
         * @param itemCount the child item count in recyclerView
         */
        fun count(itemCount: Int): Builder {
            mItemCount = itemCount
            return this
        }

        /**
         * @param skeletonLayoutResIDs the loading array of skeleton layoutResID
         */
        fun loadArrayOfLayouts(@ArrayRes skeletonLayoutResIDs: IntArray): Builder {
            mItemsResIDArray = skeletonLayoutResIDs
            return this
        }

        /**
         * @param frozen whether frozen recyclerView during skeleton showing
         * @return
         */
        fun frozen(frozen: Boolean): Builder {
            mFrozen = frozen
            return this
        }

        override fun build(): RecyclerViewSkeletonScreen {
            return RecyclerViewSkeletonScreen(this)
        }

        /**
         * build and show skeleton screen
         */
        @Deprecated("use build() then show()")
        fun show(): RecyclerViewSkeletonScreen {
            val recyclerViewSkeleton = RecyclerViewSkeletonScreen(this)
            recyclerViewSkeleton.show()
            return recyclerViewSkeleton
        }

        init {
            mShimmerColor = ContextCompat.getColor(mRecyclerView.context, R.color.shimmer_color)
            mShimmerBaseColor = ContextCompat.getColor(mRecyclerView.context, R.color.shimmer_base_color)
        }
    }
}