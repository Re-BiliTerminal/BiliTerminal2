package com.huanli233.biliterminal2.ui.recyclerview.adapters

import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.widget.com.huanli233.biliterminal2.ui.widget.components.VideoCard
import com.huanli233.biliterminal2.ui.activity.video.VideoInfoActivity
import com.huanli233.biliterminal2.ui.widget.components.VideoCard
import com.huanli233.biliterminal2.utils.diff.VideoInfoDiffCallback
import com.huanli233.biliterminal2.utils.extensions.formatNumber
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.hikage.recyclerview.HikagePagingAdapter
import splitties.activities.start

class VideoPagingAdapter: HikagePagingAdapter<VideoInfo>(VideoInfoDiffCallback()) {

    override fun createView(): Hikage.Delegate<*> = Hikageable {
        VideoCard(id = "card", lparams = widthMatchParent())
    }

    override fun bindView(hikage: Hikage, item: VideoInfo) {
        hikage.get<VideoCard>("card").apply {
            setVideoTitle(item.title)
            setVideoCover(item.pic)
            setViews(item.stat.view.formatNumber())
            setUploader(item.owner.name)
            binding.root.setOnClickListener {
                context.start<VideoInfoActivity> {
                    putExtra("bvid", item.bvid)
                }
            }
        }
    }

}