package com.huanli233.biliterminal2.ui.fragment.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.drakeet.multitype.ItemViewDelegate
import com.huanli233.biliterminal2.data.menu.MenuItem
import com.huanli233.biliterminal2.databinding.ItemMenuBinding

class MenuItemViewDelegate(
    private val onSwitch: (MenuItem) -> Unit
): ItemViewDelegate<MenuItem, MenuItemViewDelegate.ViewHolder>() {

    override fun onCreateViewHolder(context: Context, parent: ViewGroup): ViewHolder {
        return ViewHolder(ItemMenuBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, item: MenuItem) {
        with (holder.binding.root) {
            text = context.getString(item.title)
            icon = ContextCompat.getDrawable(context, item.icon)
            setOnClickListener {
                onSwitch(item)
            }
        }
    }

    class ViewHolder(val binding: ItemMenuBinding): RecyclerView.ViewHolder(binding.root)
}