package com.huanli233.biliterminal2.adapter.video

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.listener.OnItemLongClickListener
import com.huanli233.biliterminal2.model.VideoCard
import com.huanli233.biliterminal2.util.TerminalContext

class VideoCardAdapter(val context: Context, val videoCardList: List<VideoCard>) :
    RecyclerView.Adapter<VideoCardHolder>() {
    var longClickListener: OnItemLongClickListener? = null

    fun setOnLongClickListener(listener: OnItemLongClickListener?) {
        this.longClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoCardHolder {
        val view =
            LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false)
        return VideoCardHolder(view)
    }

    override fun onBindViewHolder(holder: VideoCardHolder, position: Int) {
        val videoCard = videoCardList[position]
        holder.showVideoCard(videoCard, context)

        holder.itemView.setOnClickListener {
            when (videoCard.type) {
                "video" -> TerminalContext.getInstance().enterVideoDetailPage(
                    context, videoCard.aid, videoCard.bvid, "video"
                )

                "media_bangumi" -> TerminalContext.getInstance()
                    .enterVideoDetailPage(context, videoCard.aid, null, "media")
            }
        }

        holder.itemView.setOnLongClickListener {
            if (longClickListener != null) {
                longClickListener!!.onItemLongClick(position)
                return@setOnLongClickListener true
            } else return@setOnLongClickListener false
        }
    }

    override fun getItemCount(): Int {
        return videoCardList.size
    }
}
