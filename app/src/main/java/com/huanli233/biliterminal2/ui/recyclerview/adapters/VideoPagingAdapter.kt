package com.huanli233.biliterminal2.ui.recyclerview.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.ui.widget.components.VideoCard
import com.huanli233.biliterminal2.utils.diff.VideoInfoDiffCallback
import com.huanli233.biliterminal2.utils.extensions.formatNumber
import com.huanli233.biliwebapi.bean.video.VideoInfo

class VideoViewHolder(val card: VideoCard) : RecyclerView.ViewHolder(card)

class VideoPagingAdapter: PagingDataAdapter<VideoInfo, VideoViewHolder>(VideoInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(VideoCard(parent.context))
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        getItem(position)?.let {
            with(holder.card) {
                setVideoTitle(it.title)
                setVideoCover(it.pic)
                setViews(it.stat.view.formatNumber())
                setUploader(it.owner.name)
            }
        }
    }

}