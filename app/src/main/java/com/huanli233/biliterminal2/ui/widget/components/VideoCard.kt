package com.huanli233.biliterminal2.ui.widget.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.huanli233.biliterminal2.databinding.ItemVideoCardBinding
import com.huanli233.biliterminal2.ui.utils.image.loadPicture

class VideoCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var binding: ItemVideoCardBinding =
        ItemVideoCardBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        layoutParams = ViewGroup.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    fun setVideoTitle(title: String) {
        binding.videoTitle.text = title
    }

    fun setVideoCover(url: String) {
        Glide.with(this)
            .loadPicture(url)
            .into(binding.videoCover)
    }

    fun setUploader(name: String) {
        binding.uploaderName.text = name
    }

    fun setViews(views: String) {
        binding.videoViews.text = views
    }

}