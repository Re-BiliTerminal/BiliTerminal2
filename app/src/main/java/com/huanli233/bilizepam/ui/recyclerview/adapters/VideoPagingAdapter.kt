package com.huanli233.bilizepam.ui.recyclerview.adapters

import android.app.Activity
import android.content.Intent
import androidx.core.view.ViewCompat
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.components.VideoCard
import com.huanli233.bilizepam.ui.activity.video.VideoInfoActivity
import com.huanli233.bilizepam.ui.widget.components.VideoCard
import com.huanli233.bilizepam.utils.diff.VideoInfoDiffCallback
import com.huanli233.bilizepam.utils.extensions.formatNumber
import com.huanli233.biliwebapi.bean.video.VideoInfo
import com.huanli233.bilizepam.ui.utils.animationsEnabled
import com.huanli233.bilizepam.ui.utils.hikage.extension.start
import com.huanli233.bilizepam.ui.utils.hikage.extension.transitionNameCompat
import com.huanli233.bilizepam.ui.utils.makeSceneTransitionAnimation
import com.huanli233.bilizepam.ui.utils.playAnimation
import com.huanli233.hikage.recyclerview.HikagePagingAdapter
import splitties.activities.start

class VideoPagingAdapter(
    private val activity: Activity? = null
): HikagePagingAdapter<VideoInfo>(VideoInfoDiffCallback()) {

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
                binding.root.transitionNameCompat = "video_card"
                val config: Intent.() -> Unit = {
                    putExtra("bvid", item.bvid)
                    putExtra("transition_name", "video_card")
                }
                if (animationsEnabled && activity != null) {
                    context.start<VideoInfoActivity>(activity.makeSceneTransitionAnimation(binding.root, "video_card").toBundle(), config)
                } else {
                    context.start<VideoInfoActivity>(config)
                }
            }
        }
    }

}