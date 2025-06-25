package com.huanli233.bilizepam.ui.activity.setting

import android.os.Bundle
import android.widget.CheckedTextView
import androidx.lifecycle.lifecycleScope
import com.highcapable.betterandroid.ui.extension.view.notifyDataSetChangedIgnore
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.extension.setContentView
import com.highcapable.hikage.extension.widget.bottomToParent
import com.highcapable.hikage.extension.widget.onClick
import com.highcapable.hikage.extension.widget.startToParent
import com.highcapable.hikage.extension.widget.topToParent
import com.highcapable.hikage.widget.android.widget.CheckedTextView
import com.highcapable.hikage.widget.androidx.constraintlayout.widget.ConstraintLayout
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.components.TopBar
import com.highcapable.hikage.widget.com.huanli233.bilizepam.ui.widget.scalablecontainer.AppRecyclerView
import com.huanli233.bilizepam.R
import com.huanli233.bilizepam.data.setting.LocalData
import com.huanli233.bilizepam.data.setting.edit
import com.huanli233.bilizepam.ui.activity.base.BaseActivity
import com.huanli233.bilizepam.ui.utils.recyclerview.defaultLayoutManager
import com.huanli233.bilizepam.utils.ThemeUtil
import com.huanli233.bilizepam.utils.extensions.setBackgroundCompat
import com.huanli233.bilizepam.utils.extensions.updateCompoundDrawablesRelativeWithIntrinsicBounds
import com.huanli233.bilizepam.utils.extensions.updatePaddingRelativeCompat
import com.huanli233.bilizepam.utils.getThemeDimension
import com.huanli233.bilizepam.utils.getThemeDrawable
import com.huanli233.bilizepam.utils.selectableItemBackground
import com.huanli233.hikage.recyclerview.hikageItem
import com.huanli233.hikage.recyclerview.initMultiType
import kotlinx.coroutines.launch
import splitties.views.gravityCenterVertical
import splitties.views.gravityStart

class ThemeColorActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView {
            ConstraintLayout(matchParent()) {
                TopBar(
                    lparams = widthMatchParent {
                        topToParent()
                        startToParent()
                    },
                    init = {
                        setTitle(stringRes(R.string.theme_color))
                    }
                )
                AppRecyclerView(
                    lparams = widthMatchParent(height = 0) {
                        topToBottom = R.id.top_bar
                        startToParent()
                        bottomToParent()
                    },
                    init = {
                        data class ThemeItem(
                            val colorName: String,
                            var isSelected: Boolean
                        )

                        var selectedThemeColor = LocalData.settings.theme.colorTheme
                        val themeItems: MutableList<ThemeItem> = ThemeUtil.colorThemeMap.keys.map { key ->
                            ThemeItem(key, key == selectedThemeColor)
                        }.toMutableList()

                        layoutManager = defaultLayoutManager
                        adapter = initMultiType(themeItems) {
                            +hikageItem<ThemeItem> {
                                createView {
                                    Hikageable {
                                        CheckedTextView(id = "radio_btn", lparams = widthMatchParent()) {
                                            setBackgroundCompat(selectableItemBackground)
                                            minHeight = getThemeDimension(android.R.attr.listPreferredItemHeightSmall).toInt()
                                            gravity = gravityStart or gravityCenterVertical
                                            updatePaddingRelativeCompat(
                                                start = 20.dp,
                                                end = 3.dp
                                            )
                                            updateCompoundDrawablesRelativeWithIntrinsicBounds(start = getThemeDrawable(android.R.attr.listChoiceIndicatorSingle))
                                            compoundDrawablePadding = 20.dp
                                        }
                                    }
                                }
                                bindView { hikage, item ->
                                    with(hikage.get<CheckedTextView>("radio_btn")) {
                                        text = stringRes(ThemeUtil.colorTextMap[item.colorName]!!)
                                        onClick {
                                            if (!item.isSelected) {
                                                val oldSelectedIndex = themeItems.indexOfFirst { it.isSelected }
                                                if (oldSelectedIndex != -1) {
                                                    themeItems[oldSelectedIndex].isSelected = false
                                                    adapter.notifyItemChanged(oldSelectedIndex)
                                                }

                                                item.isSelected = true
                                                val newSelectedIndex = themeItems.indexOf(item)
                                                adapter.notifyItemChanged(newSelectedIndex)

                                                selectedThemeColor = item.colorName

                                                lifecycleScope.launch {
                                                    LocalData.edit {
                                                        theme = theme.edit { colorTheme = item.colorName }
                                                    }
                                                    recreate()
                                                }
                                            }
                                        }
                                        isChecked = item.isSelected
                                    }
                                }
                            }
                        }.apply { notifyDataSetChangedIgnore() }
                    }
                )
            }
        }
    }

}