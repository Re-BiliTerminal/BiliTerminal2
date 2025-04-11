package com.huanli233.biliterminal2.ui.widget.recyclerView

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractAdapter<VH : BaseHolder>(protected var mContext: Context) :
    RecyclerView.Adapter<BaseHolder>() {
    protected open var footerViewCreator: ((parent: ViewGroup) -> View)? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            if (footerViewInitialized && field != value) {
                field = value
                notifyDataSetChanged()
            } else {
                field = value
            }
        }
    private var footerViewInitialized = false
    protected open var headerViewCreator: ((parent: ViewGroup) -> View)? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            if (headerViewInitialized && field != value) {
                field = value
                notifyDataSetChanged()
            } else {
                field = value
            }
        }
    private var headerViewInitialized = false

    abstract fun doBindViewHolder(viewHolder: VH, position: Int)

    abstract fun doCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    open fun bindHeaderView(viewHolder: BaseHolder) = Unit

    open fun bindFooterView(viewHolder: BaseHolder) = Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        if (viewType == VIEW_TYPE_HEADER) {
            headerViewInitialized = true
            headerViewCreator?.invoke(parent)?.let { return BaseHolder(it) }
        } else if (viewType == VIEW_TYPE_FOOTER) {
            footerViewInitialized = true
            footerViewCreator?.invoke(parent)?.let { return BaseHolder(it) }
        }
        return doCreateViewHolder(parent, viewType)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        val viewType = holder.itemViewType
        if (viewType == VIEW_TYPE_HEADER) {
            bindHeaderView(holder)
            return
        } else if (viewType == VIEW_TYPE_FOOTER) {
            bindFooterView(holder)
            return
        }
        var realPosition = position
        if (headerViewCreator != null) {
            realPosition--
        }
        if (footerViewCreator != null) {
            realPosition--
        }
        (holder as? VH)?.let {
            doBindViewHolder(it, realPosition)
        }
    }

    val extraViewCount: Int
        get() {
            val i = if (this.headerViewCreator != null) 1 else 0
            return if (this.footerViewCreator != null) i + 1 else i
        }

    val headerViewCount: Int
        get() = if (this.headerViewCreator == null) 0 else 1

    val footerViewCount: Int
        get() = if (this.footerViewCreator == null) 0 else 1

    companion object {
        const val VIEW_TYPE_FOOTER: Int = 1025
        const val VIEW_TYPE_HEADER: Int = 1024
    }
}
