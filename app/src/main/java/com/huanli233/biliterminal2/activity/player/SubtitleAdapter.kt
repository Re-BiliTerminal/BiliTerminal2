package com.huanli233.biliterminal2.activity.player

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.listener.OnItemClickListener
import com.huanli233.biliwebapi.bean.video.SubtitleInfoItem

class SubtitleAdapter : RecyclerView.Adapter<SubtitleAdapter.Holder>() {
    private var list: List<SubtitleInfoItem> = listOf()

    var listener: OnItemClickListener? = null
    var selectedItemIndex: Int = 0
        set(value) {
            notifyItemChanged(selectedItemIndex)
            notifyItemChanged(value)
            field = value
        }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: List<SubtitleInfoItem>) {
        this.list = items
        selectedItemIndex = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_subtitle, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (listener != null) {
            holder.listener = listener
        }
        holder.bind(position, selectedItemIndex == position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        internal var listener: OnItemClickListener? = null
        private val button: Button =
            itemView.findViewById(R.id.btn)

        fun bind(currentIndex: Int, isSelected: Boolean) {
            button.text = list[currentIndex].lan
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
            button.setOnClickListener {
                selectedItemIndex = currentIndex
                listener?.onItemClick(currentIndex)
            }
        }
    }
}


