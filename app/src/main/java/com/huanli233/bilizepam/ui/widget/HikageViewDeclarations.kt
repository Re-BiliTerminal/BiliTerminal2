@file:Suppress("unused")

package com.huanli233.bilizepam.ui.widget

import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.Guideline
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonGroup
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.loadingindicator.LoadingIndicator
import com.highcapable.hikage.annotation.HikageViewDeclaration
import com.huanli233.bilizepam.ui.widget.components.LoadingView
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppNestedScrollView
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppRecyclerView
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppScrollView
import com.huanli233.bilizepam.ui.widget.views.AnimateBoundsTextSwitcher
import com.huanli233.bilizepam.ui.widget.views.MarqueeTextView
import com.huanli233.bilizepam.ui.widget.views.TextClock
import com.huanli233.bilizepam.ui.widget.wearable.BoxInsetLayout
import net.cachapa.expandablelayout.ExpandableLayout

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
@HikageViewDeclaration(ImageFilterView::class)
object ImageFilterViewDeclaration

@HikageViewDeclaration(MaterialButtonGroup::class, lparams = LinearLayout.LayoutParams::class)
object MaterialButtonGroupDeclaration

@HikageViewDeclaration(AppScrollView::class, lparams = FrameLayout.LayoutParams::class)
object AppScrollViewDeclaration
@HikageViewDeclaration(AppNestedScrollView::class, lparams = FrameLayout.LayoutParams::class)
object AppNestedScrollViewDeclaration
@HikageViewDeclaration(AppRecyclerView::class, lparams = RecyclerView.LayoutParams::class)
object AppRecyclerViewDeclaration
@HikageViewDeclaration(ExpandableLayout::class, lparams = FrameLayout.LayoutParams::class)
object ExpandableLayoutDeclaration
@HikageViewDeclaration(LoadingView::class)
object LoadingViewDeclaration

@HikageViewDeclaration(BoxInsetLayout::class, lparams = BoxInsetLayout.LayoutParams::class)
object BoxInsetLayoutDeclaration

@HikageViewDeclaration(MarqueeTextView::class)
object MarqueeTextViewDeclaration
@HikageViewDeclaration(AnimateBoundsTextSwitcher::class)
object AnimateBoundsTextSwitcherDeclaration