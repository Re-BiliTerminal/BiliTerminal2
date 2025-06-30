package com.huanli233.bilizepam.ui.widget.components

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.highcapable.betterandroid.ui.extension.view.updateMargins
import com.highcapable.hikage.extension.widget.bottomToParent
import com.highcapable.hikage.extension.widget.endToParent
import com.highcapable.hikage.extension.widget.startToParent
import com.highcapable.hikage.extension.widget.textRes
import com.highcapable.hikage.extension.widget.topToParent
import com.highcapable.hikage.widget.android.widget.FrameLayout
import com.highcapable.hikage.widget.android.widget.ImageView
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.androidx.constraintlayout.widget.ConstraintLayout
import com.highcapable.hikage.widget.com.google.android.material.loadingindicator.LoadingIndicator
import com.highcapable.hikage.widget.com.google.android.material.progressindicator.LinearProgressIndicator
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.scalablecontainer.AppScrollView
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.ui.utils.hikage.extension.attach
import com.huanli233.bilizepam.utils.extensions.invisible
import com.huanli233.bilizepam.utils.extensions.visible
import splitties.views.gravityCenter

enum class LoadingState {
    LOADING,
    HIDDEN,
    ERROR,
    EMPTY
}

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private fun newLayoutEnabled() = LocalData.settings.theme.newLoadingWidgetEnabled
    }

    private val hikage = attach<LayoutParams> {
        AppScrollView(
            lparams = widthMatchParent {
                gravity = gravityCenter
            },
            init = {
                setAnimScale(false)
                isSpringEnableStart = false
                isSpringEnableEnd = false
            }
        ) {
            FrameLayout(widthMatchParent()) {
                ConstraintLayout(
                    lparams = widthMatchParent {
                        gravity = gravityCenter
                        updateMargins(horizontal = 6.dp)
                    }
                ) {
                    ImageView(
                        id = "loading_image",
                        lparams = LayoutParams(0, 0) {
                            updateMargins(bottom = 23.dp)
                            startToParent()
                            endToParent()
                            matchConstraintPercentWidth = 0.5f
                            dimensionRatio = "1:1"
                            topToParent()
                        }
                    )
                    TextView(
                        id = "loading_text",
                        lparams = LayoutParams {
                            startToParent()
                            endToParent()
                            topToBottom = viewId("loading_image")

                            updateMargins(horizontal = 6.dp)
                            updateMargins(bottom = 4.dp)
                        }
                    ) {
                        ellipsize = TextUtils.TruncateAt.END
                    }
                    if (newLayoutEnabled()) {
                        LoadingIndicator(
                            attr = R.layout.style_view_material_loading_indicator_contained,
                            id = "loading_progress",
                            lparams = LayoutParams(0, 0) {
                                bottomToParent()
                                startToParent()
                                endToParent()
                                topToParent()
                                matchConstraintPercentWidth = 0.5f
                                matchConstraintDefaultWidth =
                                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_PERCENT
                                dimensionRatio = "1:1"
                            }
                        )
                    } else {
                        LinearProgressIndicator(
                            id = "loading_progress",
                            lparams = LayoutParams(width = 0) {
                                bottomToParent()
                                startToParent()
                                endToParent()
                                topToBottom = viewId("loading_image")
                                matchConstraintPercentWidth = 0.5f
                            }
                        ) {
                            isIndeterminate = true
                        }
                    }
                }
            }
        }
    }

    val loadingImage by lazy { hikage.get<ImageView>("loading_image") }
    val loadingText by lazy { hikage.get<TextView>("loading_text") }
    val loadingProgress by lazy { hikage["loading_progress"] }

    private var onRetry: (() -> Unit)? = null
    var state: LoadingState = LoadingState.LOADING
        private set

    init {
        layoutParams = ViewGroup.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        loadingImage.setOnClickListener {
            if (state == LoadingState.ERROR) {
                onRetry?.invoke()
            }
        }
        if (!isInEditMode) {
            loading()
        }
    }

    fun loading() {
        state = LoadingState.LOADING
        show()
        if (newLayoutEnabled()) {
            loadingProgress.visible()
            loadingImage.invisible()
            loadingText.invisible()
        } else {
            loadingImage.setImageResource(R.drawable.loading_2233)
            loadingProgress.visible()
            loadingText.invisible()
        }
    }

    fun error(message: String? = null) {
        state = LoadingState.ERROR
        show()
        loadingImage.setImageResource(R.drawable.loading_2233_error)
        loadingImage.visible()
        loadingProgress.invisible()
        if (message != null) {
            loadingText.text = message
            loadingText.visible()
        } else {
            loadingText.invisible()
        }
    }

    fun empty() {
        state = LoadingState.EMPTY
        show()
        loadingImage.setImageResource(R.drawable.loading_2233_empty)
        loadingProgress.invisible()
        loadingImage.visible()
        loadingText.textRes = R.string.empty_tip
        loadingText.visible()
    }

    fun show() {
        visible()
    }

    fun hide() {
        state = LoadingState.HIDDEN
        invisible()
    }

    fun onRetry(retry: () -> Unit) {
        this.onRetry = retry
    }

}