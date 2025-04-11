package com.huanli233.biliterminal2.adapter.article

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.adapter.article.OpusCardAdapter.OpusHolder
import com.huanli233.biliterminal2.databinding.CardOpusBinding
import com.huanli233.biliterminal2.bean.OpusCard
import com.huanli233.biliterminal2.util.GlideUtil.loadPicture

class OpusCardAdapter(
    val lifecycleOwner: LifecycleOwner,
    var opusList: ArrayList<OpusCard>
) : RecyclerView.Adapter<OpusHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpusHolder {
        return OpusHolder(CardOpusBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: OpusHolder, position: Int) {
        val opusCard = opusList[position]
        holder.binding.textFavTime.text = opusCard.timeText
        holder.binding.textTitle.text = opusCard.title
        holder.binding.imgCover.loadPicture(opusCard.cover)

        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return opusList.size
    }

    class OpusHolder(val binding: CardOpusBinding) : RecyclerView.ViewHolder(binding.root)
}