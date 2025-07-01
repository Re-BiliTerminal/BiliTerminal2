package com.huanli233.bilizepam.ui.fragment.video

import com.huanli233.bilizepam.ui.widget.views.ExpandableTextView
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.updateMargins
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.highcapable.betterandroid.ui.extension.view.updateMargins
import com.highcapable.betterandroid.ui.extension.view.updatePadding
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.core.runtime.collectAsHikageState
import com.highcapable.hikage.extension.widget.endToParent
import com.highcapable.hikage.extension.widget.onClick
import com.highcapable.hikage.extension.widget.startToParent
import com.highcapable.hikage.extension.widget.textRes
import com.highcapable.hikage.extension.widget.topToParent
import com.highcapable.hikage.widget.android.widget.HorizontalScrollView
import com.highcapable.hikage.widget.android.widget.ImageView
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.androidx.constraintlayout.utils.widget.ImageFilterView
import com.highcapable.hikage.widget.androidx.constraintlayout.widget.ConstraintLayout
import com.highcapable.hikage.widget.com.google.android.material.button.MaterialButton
import com.highcapable.hikage.widget.com.google.android.material.chip.Chip
import com.highcapable.hikage.widget.com.google.android.material.chip.ChipGroup
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.components.LoadingView
import com.highcapable.hikage.widget.net.cachapa.expandablelayout.ExpandableLayout
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.ui.fragment.base.BaseHikageFragment
import com.huanli233.bilizepam.ui.span.setupLink
import com.huanli233.bilizepam.ui.utils.beginDelayedFade
import com.huanli233.bilizepam.ui.utils.hikage.extension.AppScrollViewWithBoxInset
import com.huanli233.bilizepam.ui.utils.hikage.extension.FullFrameLayout
import com.huanli233.bilizepam.ui.utils.hikage.extension.boldTypeFace
import com.huanli233.bilizepam.ui.utils.image.loadPicture
import com.huanli233.bilizepam.ui.utils.playAnimation
import com.huanli233.bilizepam.ui.widget.views.NoSpaceTextView
import com.huanli233.bilizepam.utils.MsgUtil
import com.huanli233.bilizepam.utils.extensions.formatNumber
import com.huanli233.bilizepam.utils.extensions.formatToDate
import com.huanli233.bilizepam.utils.extensions.invisible
import com.huanli233.bilizepam.utils.extensions.setBackgroundCompat
import com.huanli233.bilizepam.utils.extensions.toTime
import com.huanli233.bilizepam.utils.extensions.updateCompoundDrawablesRelativeWithIntrinsicBounds
import com.huanli233.bilizepam.utils.extensions.updateMarginsRelativeCompat
import com.huanli233.bilizepam.utils.extensions.updatePaddingRelativeCompat
import com.huanli233.bilizepam.utils.extensions.visible
import com.huanli233.bilizepam.utils.getThemeId
import com.huanli233.bilizepam.utils.parser.ContentElementParser
import com.huanli233.bilizepam.utils.selectableItemBackground
import kotlinx.coroutines.launch
import net.cachapa.expandablelayout.ExpandableLayout

const val ARG_KEY_AVID = "avid"
const val ARG_KEY_BVID = "bvid"

class VideoInfoFragment: BaseHikageFragment() {

    private val viewModel: VideoInfoViewModel by viewModels()

    override fun onCreateHikage(): Hikage.Delegate<*> = Hikageable {
        val uiState = viewModel.uiState.collectAsHikageState(viewLifecycleOwner)
        FullFrameLayout(
            init = {
                uiState.observe {
                    playAnimation {
                        beginDelayedFade()
                    }
                }
            }
        ) {
            AppScrollViewWithBoxInset(matchParent(), init = {
                uiState.observe { state ->
                    if (state.isLoading) invisible()
                    else if (state.error != null) invisible()
                    else if (state.videoInfo != null) {
                        visible()
                    }
                }
            }) {
                ConstraintLayout(
                    lparams = widthMatchParent(),
                    init = {
                        updatePadding(horizontal = dimenRes(R.dimen.page_padding_horizontal).toInt())
                        updatePaddingRelativeCompat(bottom = dimenRes(R.dimen.page_bottom_padding).toInt())
                    }
                ) {
                    ImageFilterView(
                        id = "cover",
                        lparams = widthMatchParent(height = 0) {
                            updateMargins(horizontal = 5.dp)
                            updateMargins(top = 8.dp)
                            topToParent()
                            startToParent()
                            dimensionRatio = "16:9"
                        }
                    ) {
                        setBackgroundCompat(selectableItemBackground)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            round = 5.dp.toFloat()
                        }
                        uiState.observe {
                            it.videoInfo?.pic?.let { pic ->
                                Glide.with(this@VideoInfoFragment)
                                    .loadPicture(pic)
                                    .centerCrop()
                                    .into(this@ImageFilterView)
                            }
                        }
                    }
                    TextView(
                        id = "duration",
                        lparams = LayoutParams {
                            bottomToBottom = viewId("cover")
                            endToEnd = viewId("cover")
                        }
                    ) {
                        alpha = 0.9f
                        setShadowLayer(
                            5.dp.toFloat(),
                            3F, 3F,
                            Color.BLACK
                        )
                        boldTypeFace()
                        updatePaddingRelativeCompat(end = 4.dp, bottom = 2.dp)
                        uiState.observe { state ->
                            state.videoInfo?.duration?.let { text = toTime(it) }
                        }
                    }
                    TextView(
                        id = "title",
                        lparams = widthMatchParent {
                            topToBottom = viewId("cover")
                            startToParent()

                            updateMargins(horizontal = 3.dp)
                        }
                    ) {
                        boldTypeFace()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            setTextAppearance(getThemeId(com.google.android.material.R.attr.textAppearanceBodySmall))
                        }
                        uiState.observe { state ->
                            state.videoInfo?.title?.let { text = it }
                        }
                    }
                    TextView(
                        id = "views",
                        lparams = LayoutParams {
                            updateMargins(top = 5.dp)
                            topToBottom = viewId("title")
                            startToParent()
                        }
                    ) {
                        updateCompoundDrawablesRelativeWithIntrinsicBounds(
                            start = drawableRes(R.drawable.icon_play_16)
                        )
                        compoundDrawablePadding = 2.dp
                        uiState.observe { state ->
                            state.videoInfo?.stat?.view?.let { text = it.formatNumber() }
                        }
                    }
                    TextView(
                        id = "danmakus",
                        lparams = LayoutParams {
                            topToTop = viewId("views")
                            startToEnd = viewId("views")
                            updateMarginsRelativeCompat(start = 5.dp)
                        }
                    ) {
                        updateCompoundDrawablesRelativeWithIntrinsicBounds(
                            start = drawableRes(R.drawable.icon_danmaku)
                        )
                        compoundDrawablePadding = 2.dp
                        uiState.observe { state ->
                            state.videoInfo?.stat?.danmaku?.let { text = it.formatNumber() }
                        }
                    }
                    TextView(
                        id = "publish_time",
                        lparams = LayoutParams {
                            topToBottom = viewId("views")
                            startToParent()
                            updateMargins(top = 1.dp)
                        }
                    ) {
                        updateCompoundDrawablesRelativeWithIntrinsicBounds(
                            start = drawableRes(R.drawable.icon_schedule_16)
                        )
                        compoundDrawablePadding = 2.dp
                        uiState.observe { state ->
                            state.videoInfo?.ctime?.let { text = it.formatToDate() }
                        }
                    }
                    TextView(
                        id = "bvid",
                        lparams = LayoutParams {
                            topToBottom = viewId("publish_time")
                            startToParent()
                            updateMargins(top = 1.dp)
                        }
                    ) {
                        updateCompoundDrawablesRelativeWithIntrinsicBounds(
                            start = drawableRes(R.drawable.icon_movie_16)
                        )
                        compoundDrawablePadding = 2.dp
                        uiState.observe { state ->
                            state.videoInfo?.bvid?.let { text = it }
                        }
                    }
                    ViewGroup<ExpandableTextView, LinearLayout.LayoutParams>(
                        id = "desc",
                        lparams = widthMatchParent {
                            startToParent()
                            endToParent()
                            topToBottom = viewId("bvid")
                            updateMargins(top = 5.dp)
                        },
                        init = {
                            uiState.observe { state ->
                                state.videoInfo?.let { info ->
                                    setText(
                                        info.descV2?.let {
                                            ContentElementParser.parseDescription(it)
                                        } ?: info.desc.orEmpty()
                                    )
                                }
                            }
                            textView.setupLink(requireActivity())
                        }
                    )
                    val tagsIcon = ImageView(
                        id = "tags_icon",
                        lparams = LayoutParams {
                            topToBottom = viewId("desc")
                            startToParent()
                            updateMargins(top = 5.dp)
                        }
                    ) {
                        setImageResource(R.drawable.icon_keyboard_arrow_down)
                        whenAvailableTyped<ExpandableLayout>("tags_layout") { tagsLayout ->
                            onClick {
                                tagsLayout.toggle()
                            }
                        }
                    }
                    View<NoSpaceTextView>(
                        id = "tags_tip",
                        lparams = widthMatchParent {
                            startToEnd = viewId("tags_icon")
                            endToParent()
                            constrainedWidth = true
                            topToTop = viewId("tags_icon")
                            bottomToBottom = viewId("tags_icon")
                        }
                    ) {
                        textRes = R.string.tag
                        setTextColor(ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary)))
                        whenAvailableTyped<ExpandableLayout>("tags_layout") { tagsLayout ->
                            onClick {
                                tagsLayout.toggle()
                            }
                        }
                    }
                    ExpandableLayout(
                        id = "tags_layout",
                        attr = R.layout.style_view_expandable_layout,
                        lparams = widthMatchParent {
                            startToParent()
                            endToParent()
                            topToBottom = viewId("tags_tip")
                        },
                        init = {
                            clipChildren = false
                            setOnExpansionUpdateListener { _, state ->
                                tagsIcon.setImageResource(when (state) {
                                    ExpandableLayout.State.EXPANDING, ExpandableLayout.State.EXPANDED -> R.drawable.icon_keyboard_arrow_up
                                    else -> R.drawable.icon_keyboard_arrow_down
                                })
                            }
                        }
                    ) {
                        ChipGroup(
                            id = "tags",
                            lparams = widthMatchParent(),
                            init = {
                                chipSpacingHorizontal = 3.dp
                                chipSpacingVertical = 2.dp
                            }
                        ) {
                            this.UpdateScope(uiState, diff = { previousValue.tags == currentValue.tags }) {
                                value.tags.forEach { tag ->
                                    Chip {
                                        text = tag.tagName
                                        onClick {
                                            // TODO go to search page
                                        }
                                    }
                                }
                            }
                        }
                    }
                    MaterialButton(
                        id = "play",
                        lparams = LayoutParams {
                            topToBottom = viewId("tags_layout")
                            startToParent()
                            endToParent()

                            updateMargins(top = 10.dp)
                        }
                    ) {
                        iconPadding = 5.dp
                        icon = drawableRes(R.drawable.icon_play_circle)
                        textRes = R.string.play
                    }
                    HorizontalScrollView(
                        id = "toolbar1",
                        lparams = LayoutParams {
                            topToBottom = viewId("play")
                            startToParent()
                            endToParent()

                            updateMargins(top = 5.dp)
                        }
                    ) {
                        ConstraintLayout(heightMatchParent()) {
                            val like = MaterialButton(
                                id = "like",
                                lparams = LayoutParams(width = 0) {
                                    topToParent()
                                    startToParent()
                                    dimensionRatio = "1:1"
                                },
                                attr = R.layout.style_view_material_icon_button_outlined
                            ) {
                                textRes = R.string.like
                                icon = drawableRes(R.drawable.icon_thumb_up_20)
                                iconPadding = 3.dp
                                iconGravity = MaterialButton.ICON_GRAVITY_TEXT_TOP
                                iconTint = ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorAccent))
                                onClick {
                                    if (!viewModel.uiState.value.isLiking) {
                                        viewModel.like()
                                    }
                                }
                                uiState.observe { state ->
                                    state.videoInfo?.stat?.like?.formatNumber()?.let { text = it }
                                }
                            }
                            val coin = MaterialButton(
                                id = "coin",
                                lparams = LayoutParams(width = 0) {
                                    topToParent()
                                    startToEnd = viewId("like")
                                    dimensionRatio = "1:1"
                                },
                                attr = R.layout.style_view_material_icon_button_outlined
                            ) {
                                textRes = R.string.coin
                                icon = drawableRes(R.drawable.icon_coin)
                                iconPadding = 3.dp
                                iconGravity = MaterialButton.ICON_GRAVITY_TEXT_TOP
                                iconTint = ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorAccent))
                                onClick {
                                    // TODO show coin selection
                                }
                                uiState.observe { state ->
                                    state.videoInfo?.stat?.coin?.formatNumber()?.let { text = it }
                                }
                            }
                            val favorite = MaterialButton(
                                id = "favorite",
                                lparams = LayoutParams(width = 0) {
                                    topToParent()
                                    startToEnd = viewId("coin")
                                    dimensionRatio = "1:1"
                                },
                                attr = R.layout.style_view_material_icon_button_outlined
                            ) {
                                textRes = R.string.favorite
                                icon = drawableRes(R.drawable.icon_star_20)
                                iconPadding = 3.dp
                                iconGravity = MaterialButton.ICON_GRAVITY_TEXT_TOP
                                iconTint = ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorAccent))
                                onClick {
                                    // TODO jump to favourite list
                                }
                                uiState.observe { state ->
                                    state.videoInfo?.stat?.favorite?.formatNumber()?.let { text = it }
                                }
                            }
                            uiState.observe { state ->
                                val relation = state.relation
                                val colorActivate = MaterialColors.getColor(requireContext(), androidx.appcompat.R.attr.colorPrimary, 0)
                                val colorDeactivate = MaterialColors.getColor(requireContext(), androidx.appcompat.R.attr.colorAccent, 0)
                                like.isEnabled = relation != null
                                coin.isEnabled = relation != null
                                favorite.isEnabled = relation != null

                                like.iconTint = ColorStateList.valueOf(if (relation?.like == true) colorActivate else colorDeactivate)
                                coin.iconTint = ColorStateList.valueOf(if (relation?.coin?.let { it > 0 } == true) colorActivate else colorDeactivate)
                                favorite.iconTint = ColorStateList.valueOf(if (relation?.favorite == true) colorActivate else colorDeactivate)
                            }
                        }
                    }
                }
            }
            LoadingView(
                matchParent(),
                init = {
                    onRetry(viewModel::fetchData)
                    uiState.observe { state ->
                        if (state.isLoading) loading()
                        else if (state.error != null) error(state.error)
                        else if (state.videoInfo != null) {
                            hide()
                        }
                    }
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    handleEvent(event)
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