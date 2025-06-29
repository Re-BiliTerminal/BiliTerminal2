@file:Suppress("unused")

package com.huanli233.bilizepam.ui.widget

import android.widget.LinearLayout
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.Guideline
import com.google.android.material.button.MaterialButtonGroup
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.loadingindicator.LoadingIndicator
import com.highcapable.hikage.annotation.HikageViewDeclaration
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppNestedScrollView
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppRecyclerView
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppScrollView
import com.huanli233.bilizepam.ui.widget.views.AnimateBoundsTextSwitcher
import com.huanli233.bilizepam.ui.widget.views.MarqueeTextView
import com.huanli233.bilizepam.ui.widget.views.TextClock
import com.huanli233.bilizepam.ui.widget.wearable.BoxInsetLayout

@HikageViewDeclaration(
    TextClock::class,
    alias = "AppTextClock"
)
object TextClockDeclaration

@HikageViewDeclaration(LoadingIndicator::class)
object LoadingIndicatorDeclaration

@HikageViewDeclaration(Guideline::class)
object GuidelineDeclaration
@HikageViewDeclaration(Flow::class)
object FlowDeclaration

@HikageViewDeclaration(MaterialButtonGroup::class, lparams = LinearLayout.LayoutParams::class)
object MaterialButtonGroupDeclaration

@HikageViewDeclaration(AppScrollView::class)
object AppScrollViewDeclaration
@HikageViewDeclaration(AppNestedScrollView::class)
object AppNestedScrollViewDeclaration
@HikageViewDeclaration(AppRecyclerView::class)
object AppRecyclerViewDeclaration

@HikageViewDeclaration(BoxInsetLayout::class)
object BoxInsetLayoutDeclaration

@HikageViewDeclaration(MarqueeTextView::class)
object MarqueeTextViewDeclaration
@HikageViewDeclaration(AnimateBoundsTextSwitcher::class)
object AnimateBoundsTextSwitcherDeclaration