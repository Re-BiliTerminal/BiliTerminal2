package com.huanli233.biliterminal2.utils.diff

import androidx.recyclerview.widget.DiffUtil
import com.huanli233.biliwebapi.bean.video.VideoInfo

class VideoInfoDiffCallback : DiffUtil.ItemCallback<VideoInfo>() {
    override fun areItemsTheSame(oldItem: VideoInfo, newItem: VideoInfo): Boolean {
        return oldItem.aid == newItem.aid
    }

    override fun areContentsTheSame(oldItem: VideoInfo, newItem: VideoInfo): Boolean {
        return oldItem == newItem
    }
}