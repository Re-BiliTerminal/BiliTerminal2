package com.huanli233.biliterminal2.ui.widget.recyclerView

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class BaseHolder : RecyclerView.ViewHolder {
    private val viewArray: SparseArray<View>

    constructor(viewGroup: ViewGroup, layoutId: Int) : super(
        LayoutInflater.from(viewGroup.context).inflate(layoutId, viewGroup, false)
    ) {
        this.viewArray = SparseArray()
    }

    constructor(view: View) : super(view) {
        this.viewArray = SparseArray()
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : View?> getView(id: Int): T {
        val t: T? = viewArray[id] as T?
        if (t == null) {
            val t2 = itemView.findViewById<T>(id)
            viewArray.put(id, t2)
            return t2
        }
        return t
    }

    protected val context: Context
        get() = itemView.context
}