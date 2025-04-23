package com.huanli233.biliterminal2.adapter.dynamic

import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.databinding.CellDynamicBinding

class DynamicHolder(
    private val binding: CellDynamicBinding,
    val isChild: Boolean
) : RecyclerView.ViewHolder(binding.root) {


}
