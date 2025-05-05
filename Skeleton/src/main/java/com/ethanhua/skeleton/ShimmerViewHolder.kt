package com.ethanhua.skeleton

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.huanli233.skeleton.R

/**
 * Created by ethanhua on 2017/7/29.
 */
class ShimmerViewHolder(inflater: LayoutInflater, parent: ViewGroup?, innerViewResId: Int)
    : ViewHolder(inflater.inflate(R.layout.layout_shimmer, parent, false)) {
    init {
        val layout = itemView as ViewGroup
        val view = inflater.inflate(innerViewResId, layout, false)
        view.layoutParams?.let {
            layout.layoutParams = it
        }
        layout.addView(view)
    }
}