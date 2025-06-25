package com.huanli233.biliterminal2.ui.fragment.menu

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.highcapable.betterandroid.ui.extension.view.updateMargins
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.widget.com.google.android.material.button.MaterialButton
import com.huanli233.biliterminal2.data.menu.MenuItem
import com.huanli233.biliterminal2.databinding.ItemMenuBinding
import com.huanli233.hikage.recyclerview.ItemHikageDelegate

class MenuItemDelegate(
    private val onSwitch: (MenuItem) -> Unit
): ItemHikageDelegate<MenuItem>() {

    override fun createView(): Hikage.Delegate<*> = Hikageable<ViewGroup.MarginLayoutParams> {
        MaterialButton(
            id = "menu_btn",
            lparams = widthMatchParent {
                updateMargins(horizontal = 12.dp, vertical = 2.dp)
            }
        )
    }

    override fun bindView(hikage: Hikage, item: MenuItem) {
        hikage.get<MaterialButton>("menu_btn").apply {
            text = context.getString(item.title)
            icon = ContextCompat.getDrawable(context, item.icon)
            setOnClickListener {
                onSwitch(item)
            }
        }
    }

}