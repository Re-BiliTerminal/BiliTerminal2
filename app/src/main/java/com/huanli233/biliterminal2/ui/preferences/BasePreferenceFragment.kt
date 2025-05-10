@file:Suppress("DEPRECATION")

package com.huanli233.biliterminal2.ui.preferences

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceRecyclerViewAccessibilityDelegate
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.ui.fragment.setting.SettingsDataStore

abstract class BasePreferenceFragment: PreferenceFragmentCompat() {

    @SuppressLint("RestrictedApi")
    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        return (inflater.inflate(R.layout.widget_settings_recyclerview, parent, false) as RecyclerView).apply {
            layoutManager = onCreateLayoutManager()
            setAccessibilityDelegateCompat(PreferenceRecyclerViewAccessibilityDelegate(this))
        }
    }

    @CallSuper
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsDataStore()
    }

}

class DividerDecoration(private val mList: RecyclerView) : ItemDecoration() {

    private var mDivider: Drawable? = null
    private var mDividerHeight: Int = 0
    private var mAllowDividerAfterLastItem: Boolean = true

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mDivider == null) {
            return
        }
        val childCount = parent.childCount
        val width = parent.width
        for (childViewIndex in 0 until childCount) {
            val view = parent.getChildAt(childViewIndex) ?: continue
            if (shouldDrawDividerBelow(view, parent)) {
                val top = view.y.toInt() + view.height
                mDivider?.setBounds(0, top, width, top + mDividerHeight)
                mDivider?.draw(c)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (shouldDrawDividerBelow(view, parent)) {
            outRect.bottom = mDividerHeight
        } else {
            outRect.bottom = 0
        }
    }

    private fun shouldDrawDividerBelow(view: View, parent: RecyclerView): Boolean {
        val holder = parent.getChildViewHolder(view)
        val dividerAllowedBelow = (holder as? PreferenceViewHolder)?.isDividerAllowedBelow == true

        if (!dividerAllowedBelow) {
            return false
        }

        var nextAllowed = mAllowDividerAfterLastItem
        val index = parent.indexOfChild(view)

        if (index < parent.childCount - 1) {
            val nextView = parent.getChildAt(index + 1)
            if (nextView != null) {
                val nextHolder = parent.getChildViewHolder(nextView)
                nextAllowed = (nextHolder as? PreferenceViewHolder)?.isDividerAllowedAbove == true
            }
        }
        return nextAllowed
    }

    fun setDivider(divider: Drawable?) {
        mDivider = divider
        mDividerHeight = divider?.intrinsicHeight ?: 0
        mList.invalidateItemDecorations()
    }

    fun setDividerHeight(dividerHeight: Int) {
        mDividerHeight = dividerHeight
        mList.invalidateItemDecorations()
    }

    fun setAllowDividerAfterLastItem(allowDividerAfterLastItem: Boolean) {
        mAllowDividerAfterLastItem = allowDividerAfterLastItem
    }
}