@file:Suppress("unused")

package com.huanli233.bilizepam.ui.widget

import androidx.constraintlayout.widget.Guideline
import com.highcapable.hikage.annotation.HikageViewDeclaration
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppNestedScrollView
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppRecyclerView
import com.huanli233.bilizepam.ui.widget.scalablecontainer.AppScrollView
import com.huanli233.bilizepam.ui.widget.views.MarqueeTextView
import com.huanli233.bilizepam.ui.widget.views.TextClock
import com.huanli233.bilizepam.ui.widget.wearable.BoxInsetLayout

@HikageViewDeclaration(
    TextClock::class,
    alias = "AppTextClock"
)
object TextClockDeclaration

@HikageViewDeclaration(Guideline::class)
object GuidelineDeclaration

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