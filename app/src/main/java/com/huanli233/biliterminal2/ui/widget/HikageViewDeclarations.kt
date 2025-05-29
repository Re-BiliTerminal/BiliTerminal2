package com.huanli233.biliterminal2.ui.widget

import androidx.constraintlayout.widget.Guideline
import com.highcapable.hikage.annotation.HikageViewDeclaration
import com.huanli233.biliterminal2.ui.widget.views.TextClock

@HikageViewDeclaration(
    TextClock::class,
    alias = "AppTextClock"
)
object TextClockDeclaration

@HikageViewDeclaration(Guideline::class)
object GuidelineDeclaration