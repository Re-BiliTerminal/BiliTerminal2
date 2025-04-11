package com.huanli233.biliterminal2.adapter.common

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.adapter.common.ChoiceAdapter.ChoiceHolder

class ChoiceAdapter : RecyclerView.Adapter<ChoiceHolder> {
    private var dataList: MutableList<Choice> = mutableListOf()

    var listener: OnSelectListener? = null
    private var _selectedItemIndex: Int = 0
    val selectedItemIndex
        get() = _selectedItemIndex
    private var useVerticalLayout = false

    constructor()

    constructor(useVerticalLayout: Boolean) {
        this.useVerticalLayout = useVerticalLayout
    }

    fun setOnItemClickListener(listener: OnSelectListener) {
        this.listener = listener
    }

    fun setSelectedItemIndex(selectedItemIndex: Int) {
        // Cancel previous selected item and set current item
        val previousSelectedIndex = this.selectedItemIndex
        this._selectedItemIndex = selectedItemIndex
        notifyItemChanged(previousSelectedIndex)
        notifyItemChanged(selectedItemIndex)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<Choice>) {
        this.dataList = dataList.toMutableList()
        _selectedItemIndex = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            if (useVerticalLayout) R.layout.cell_item_vertical else R.layout.item_choice_button,
            parent,
            false
        )
        return ChoiceHolder(view)
    }

    override fun onBindViewHolder(holder: ChoiceHolder, position: Int) {
        if (listener != null) {
            holder.listener = listener
        }
        holder.bind(position, selectedItemIndex == position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ChoiceHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var listener: OnSelectListener? = null
        private val button: MaterialButton = itemView.findViewById<MaterialButton>(R.id.btn)

        fun bind(currentIndex: Int, isSelected: Boolean) {
            button.text = dataList[currentIndex].name
            if (isSelected) {
                button.setTextColor(-0x33d9d9da)
                ViewCompat.setBackgroundTintList(
                    button,
                    AppCompatResources.getColorStateList(
                        itemView.context,
                        R.color.background_button_selected
                    )
                )
            } else {
                button.setTextColor(-0x141f1e)
                ViewCompat.setBackgroundTintList(
                    button,
                    AppCompatResources.getColorStateList(
                        itemView.context,
                        R.color.background_button
                    )
                )
            }
            button.setOnClickListener(View.OnClickListener { v: View? ->
                setSelectedItemIndex(currentIndex)
                listener?.invoke(dataList[currentIndex].apply {
                    index = currentIndex
                })
            })
        }
    }
}

typealias OnSelectListener = (choice: Choice) -> Unit

data class Choice @JvmOverloads constructor(
    val name: String,
    val value: String,
    var index: Int = -1
)