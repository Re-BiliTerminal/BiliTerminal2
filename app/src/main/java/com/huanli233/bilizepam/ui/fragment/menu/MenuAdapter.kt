package com.huanli233.bilizepam.ui.fragment.menu

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.highcapable.betterandroid.ui.extension.view.updateMargins
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.extension.widget.vertical
import com.highcapable.hikage.widget.android.widget.ImageView
import com.highcapable.hikage.widget.android.widget.LinearLayout
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.com.google.android.material.button.MaterialButton
import com.highcapable.hikage.widget.com.google.android.material.card.MaterialCardView
import com.huanli233.bilizepam.data.menu.MenuItem
import com.huanli233.bilizepam.utils.extensions.setBackgroundCompat
import com.huanli233.bilizepam.utils.selectableItemBackground
import com.huanli233.hikage.recyclerview.ItemHikageDelegate
import splitties.views.appcompat.imgTintList
import splitties.views.padding

class MenuItemDelegate(
    private val onSwitch: (MenuItem, View) -> Unit
): ItemHikageDelegate<MenuItem>() {

    override fun createView(): Hikage.Delegate<*> = Hikageable<ViewGroup.MarginLayoutParams> {
        LinearLayout(
            lparams = widthMatchParent {
                updateMargins(horizontal = 10.dp)
            },
            init = {
                padding = 8.dp
                vertical()
                setBackgroundCompat(selectableItemBackground)
            }
        ) {
            MaterialCardView(
                id = "card",
                lparams = LayoutParams(64.dp, 48.dp) {
                    gravity = Gravity.CENTER
                },
                init = {
                    radius = 24.dp.toFloat()
                    isClickable = false
                }
            ) {
                ImageView(
                    id = "icon",
                    lparams = LayoutParams(24.dp, 24.dp) {
                        gravity = Gravity.CENTER
                    }
                ) {
                    imgTintList = ColorStateList.valueOf(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface))
                }
            }
            TextView(
                id = "text",
                lparams = LayoutParams {
                    gravity = Gravity.CENTER
                    updateMargins(top = 4.dp)
                }
            ) {
                textSize = 14f
            }
        }
    }

    override fun bindView(hikage: Hikage, item: MenuItem) {
        hikage.root.setOnClickListener {
            onSwitch(item, hikage["card"])
        }
        hikage.get<TextView>("text").apply {
            text = context.getString(item.title)
        }
        hikage.get<ImageView>("icon").apply {
            setImageDrawable(ContextCompat.getDrawable(context, item.icon))
        }
    }

}