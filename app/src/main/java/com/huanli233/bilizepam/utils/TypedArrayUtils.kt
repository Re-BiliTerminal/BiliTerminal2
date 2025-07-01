package com.huanli233.bilizepam.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.highcapable.betterandroid.ui.extension.component.base.getThemeAttrsBoolean
import com.highcapable.betterandroid.ui.extension.component.base.getThemeAttrsColor
import com.highcapable.betterandroid.ui.extension.component.base.getThemeAttrsDimension
import com.highcapable.betterandroid.ui.extension.component.base.getThemeAttrsDrawable
import com.highcapable.betterandroid.ui.extension.component.base.getThemeAttrsId
import com.highcapable.betterandroid.ui.extension.component.base.getThemeAttrsString
import android.R as AndroidR

fun View.getThemeId(@AttrRes attrResId: Int): Int = context.getThemeAttrsId(attrResId)

@ColorInt
fun View.getThemeColor(@AttrRes attrResId: Int): Int = context.getThemeAttrsColor(attrResId)

fun View.getThemeDimension(@AttrRes attrResId: Int): Float = context.getThemeAttrsDimension(attrResId)

fun View.getThemeDrawable(@AttrRes attrResId: Int): Drawable? = context.getThemeAttrsDrawable(attrResId)

fun View.getThemeString(@AttrRes attrResId: Int): String? = context.getThemeAttrsString(attrResId)

fun View.getThemeBoolean(@AttrRes attrResId: Int, defaultValue: Boolean = false): Boolean =
    context.getThemeAttrsBoolean(attrResId)

val View.selectableItemBackground
    get() = getThemeDrawable(AndroidR.attr.selectableItemBackground)