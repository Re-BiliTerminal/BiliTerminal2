package com.huanli233.biliterminal2.ui.recyclerview.adapters

import android.util.Log
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.huanli233.biliterminal2.ui.activity.video.VideoInfoActivity
import com.huanli233.biliterminal2.ui.widget.components.VideoCard
import com.huanli233.biliterminal2.utils.diff.VideoInfoDiffCallback
import com.huanli233.biliterminal2.utils.extensions.formatNumber
import com.huanli233.biliwebapi.bean.video.VideoInfo
import splitties.activities.start

class VideoViewHolder(val card: VideoCard) : RecyclerView.ViewHolder(card)

class VideoPagingAdapter: PagingDataAdapter<VideoInfo, VideoViewHolder>(VideoInfoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(VideoCard(parent.context))
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        getItem(position)?.let { info ->
            with(holder.card) {
                setVideoTitle(info.title)
                setVideoCover(info.pic)
                setViews(info.stat.view.formatNumber())
                setUploader(info.owner.name)
                binding.root.setOnClickListener {
                    context.start<VideoInfoActivity> {
                        putExtra("bvid", info.bvid)
                    }
                }
            }
        }
    }

}