package com.huanli233.biliterminal2.ui.widget.wearable

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/** How much icons should scale, at most.  */
private const val MAX_ICON_PROGRESS = 0.8f

class CustomScrollingLayoutCallback : WearableLinearLayoutManager.LayoutCallback() {

    private var progressToCenter: Float = 0f

    override fun onLayoutFinished(child: View, parent: RecyclerView) {
        child.apply {
            val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
            val yRelativeToCenterOffset = y / parent.height + centerOffset

            progressToCenter = abs(0.5f - yRelativeToCenterOffset)
            progressToCenter = progressToCenter.coerceAtMost(MAX_ICON_PROGRESS)

            scaleX = 1 - progressToCenter
            scaleY = 1 - progressToCenter
        }
    }
}