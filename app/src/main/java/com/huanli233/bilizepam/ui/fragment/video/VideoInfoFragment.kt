package com.huanli233.bilizepam.ui.fragment.video

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.databinding.FragmentVideoInfoBinding
import com.huanli233.bilizepam.ui.fragment.base.BaseFragment
import com.huanli233.bilizepam.ui.utils.beginDelayedFade
import com.huanli233.bilizepam.ui.utils.image.loadPicture
import com.huanli233.bilizepam.ui.utils.playAnimation
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.bilizepam.utils.extensions.formatNumber
import com.huanli233.bilizepam.utils.extensions.formatToDate
import com.huanli233.bilizepam.utils.extensions.invisible
import com.huanli233.bilizepam.utils.extensions.toTime
import com.huanli233.bilizepam.utils.extensions.visible
import kotlinx.coroutines.launch
import net.cachapa.expandablelayout.ExpandableLayout

const val ARG_KEY_BVID = "bvid"

class VideoInfoFragment: BaseFragment() {

    private lateinit var binding: FragmentVideoInfoBinding
    private val viewModel: VideoInfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingView.onRetry(viewModel::fetchData)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    updateUi(uiState)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    handleEvent(event)
                }
            }
        }

        binding.tagsLayout.setOnExpansionUpdateListener { _, state ->
            binding.tagsIcon.setImageResource(when (state) {
                ExpandableLayout.State.EXPANDING, ExpandableLayout.State.EXPANDED -> R.drawable.icon_keyboard_arrow_up
                else -> R.drawable.icon_keyboard_arrow_down
            })
        }
        binding.tagsIcon.setOnClickListener { binding.tagsLayout.toggle() }
        binding.tagsTip.setOnClickListener { binding.tagsLayout.toggle() }

        binding.like.setOnClickListener {
            if (!viewModel.uiState.value.isLiking) {
                viewModel.like()
            }
        }
        binding.coin.setOnClickListener {
            // TODO show coin selection
        }
        binding.favorite.setOnClickListener {
            // TODO jump to favorite list
        }
    }

    private fun updateUi(uiState: VideoUiState) {
        with (binding) {
            playAnimation {
                root.beginDelayedFade()
            }
            when {
                uiState.isLoading -> {
                    contentLayout.invisible()
                    binding.loadingView.loading()
                }
                uiState.error != null -> {
                    contentLayout.invisible()
                    binding.loadingView.error(uiState.error)
                }
                uiState.videoInfo != null -> {
                    loadingView.hide()
                    contentLayout.visible()

                    val info = uiState.videoInfo
                    title.text = info.title
                    Glide.with(this@VideoInfoFragment)
                        .loadPicture(info.pic)
                        .centerCrop()
                        .into(cover)
                    duration.text = toTime(info.duration)
                    views.text = info.stat.view.formatNumber()
                    danmakus.text = info.stat.danmaku.formatNumber()
                    publishTime.text = info.pubDate.formatToDate()
                    like.text = info.stat.like.formatNumber()
                    coin.text = info.stat.coin.formatNumber()
                    favorite.text = info.stat.favorite.formatNumber()
                    bvid.text = info.bvid
                    // TODO desc_v2 parser
                    desc.text = info.desc

                    tags.removeAllViews()
                    uiState.tags.forEach { tag ->
                        binding.tags.addView(
                            Chip(context).apply {
                                text = tag.tagName
                                setEnsureMinTouchTargetSize(false)
                                setOnClickListener {
                                    // TODO go to search page
                                }
                            },
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        )
                    }

                    val relation = uiState.relation
                    val colorActivate = MaterialColors.getColor(requireContext(), androidx.appcompat.R.attr.colorPrimary, 0)
                    val colorDeactivate = MaterialColors.getColor(requireContext(), androidx.appcompat.R.attr.colorAccent, 0)
                    like.isEnabled = relation != null
                    coin.isEnabled = relation != null
                    favorite.isEnabled = relation != null

                    like.iconTint = ColorStateList.valueOf(if (relation?.like == true) colorActivate else colorDeactivate)
                    coin.iconTint = ColorStateList.valueOf(if (relation?.coin?.let { it > 0 } == true) colorActivate else colorDeactivate)
                    favorite.iconTint = ColorStateList.valueOf(if (relation?.favorite == true) colorActivate else colorDeactivate)
                }
                else -> {
                    contentLayout.invisible()
                    loadingView.error("No data available.")
                }
            }
        }
    }

    private fun handleEvent(event: VideoEvent) {
        when (event) {
            is VideoEvent.LikeSuccess -> {
                MsgUtil.showMsg(
                    if (event.action == 1) getString(R.string.like_success)
                    else getString(R.string.cancel_success)
                )
            }
            is VideoEvent.LikeFailed -> {
                MsgUtil.showMsg(event.message.toString())
            }
            is VideoEvent.NotLoggedIn -> {
                MsgUtil.showMsg(getString(R.string.not_logged_in))
            }
        }
    }

}