package com.huanli233.biliterminal2.ui.widget.recyclerView

import android.annotation.SuppressLint
import android.content.Context

abstract class BaseAdapter<M, VH : BaseHolder> : AbstractAdapter<VH> {
    private val dataList: MutableList<M>

    fun getViewType(): Int {
        return 0
    }

    constructor(context: Context) : super(context) {
        this.dataList = mutableListOf()
    }

    constructor(context: Context, dataList: List<M>) : super(context) {
        this.dataList = dataList.toMutableList()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun fillList(list: List<M>): Boolean {
        dataList.clear()
        val result = dataList.addAll(list)
        notifyDataSetChanged()
        return result
    }

    fun appendItem(item: M): Boolean {
        val size = dataList.size
        val result = dataList.add(item)
        notifyItemInserted(size + headerViewCount)
        return result
    }

    fun appendList(list: List<M>): Boolean {
        val size = dataList.size
        val result = dataList.addAll(list)
        notifyItemRangeInserted(size, list.size)
        return result
    }

    fun preposeItem(item: M) {
        dataList.add(0, item)
        notifyItemInserted(0)
        notifyItemRangeChanged(0, itemCount)
    }

    fun preposeList(list: List<M>) {
        dataList.addAll(0, list)
        notifyItemRangeInserted(0, list.size)
    }

    fun updateItem(position: Int, item: M) {
        dataList[position] = item
        notifyItemChanged(headerViewCount + position)
    }

    fun updateItem(originalItem: M, newItem: M) {
        val index = dataList.indexOf(originalItem)
        if (index >= 0) {
            dataList[index] = newItem
            notifyItemChanged(index)
        }
    }

    fun removeItem(position: Int) {
        if (this.headerViewCreator == null) {
            dataList.removeAt(position)
            notifyItemRemoved(position)
        } else {
            dataList.removeAt(position - 1)
            notifyItemRemoved(position - 1)
        }
    }

    fun removeItem(item: M) {
        val index = dataList.indexOf(item)
        if (index >= 0) {
            dataList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (this.headerViewCreator != null && position == 0) {
            return VIEW_TYPE_HEADER
        } else if (this.footerViewCreator != null && position == dataList.size + headerViewCount) {
            return VIEW_TYPE_FOOTER
        }
        return getViewType()
    }

    override fun getItemCount(): Int {
        return dataList.size + extraViewCount
    }

    fun getItem(i: Int): M? {
        var pos = i
        val list: List<M>
        if ((this.headerViewCreator == null || pos != 0) && pos < dataList.size + headerViewCount) {
            if (this.headerViewCreator == null) {
                list = this.dataList
            } else {
                list = this.dataList
                pos--
            }
            return list[pos]
        }
        return null
    }

    fun getItem(vh: VH): M? {
        return getItem(vh.adapterPosition)
    }

    val allData: List<M>
        get() = this.dataList
}
