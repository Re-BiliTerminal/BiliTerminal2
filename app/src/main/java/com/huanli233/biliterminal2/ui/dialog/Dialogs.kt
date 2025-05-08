package com.huanli233.biliterminal2.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.DataStore
import splitties.views.backgroundColor

object Dialogs {

    fun text(context: Context, content: CharSequence) {
        context.dialogBuilder()
            .setMessage(content)
            .setNegativeButton(R.string.cancel) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    fun textAction(
        context: Context,
        content: CharSequence,
        action: () -> Unit
    ) {
        context.dialogBuilder()
            .setMessage(content)
            .setNegativeButton(R.string.cancel) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.open) { dialog, which ->
                dialog.dismiss()
                action()
            }
            .show()
    }

    private fun Context.dialogBuilder() = if (DataStore.appSettings.fullScreenDialogDisabled) {
        AppMaterialDialogBuilder(this)
    } else {
        AppMaterialDialogBuilder(this, R.style.Theme_BiliTerminal2_Material3_BaseFullScreenDialog)
    }

}

class AppMaterialDialogBuilder(
    context: Context,
    themeResId: Int
) : MaterialAlertDialogBuilder(context, themeResId) {
    constructor(context: Context) : this(context, 0)

    override fun create(): AlertDialog {
        return super.create().apply {
            if (!DataStore.appSettings.fullScreenDialogDisabled) {
                window?.apply {
                    requestFeature(Window.FEATURE_NO_TITLE)
                    setBackgroundDrawableResource(android.R.color.transparent)
                    setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                    decorView.apply {
                        backgroundColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceContainerHigh, ResourcesCompat.getColor(resources, android.R.color.transparent, context.theme))
                        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                            insets
                        }
                    }
                }
            }
        }
    }

    override fun show(): AlertDialog? {
        return super.show().apply {
            if (!DataStore.appSettings.fullScreenDialogDisabled) {
                window?.decorView?.rootView?.findViewById<View>(androidx.appcompat.R.id.parentPanel)?.let {
                    it.updateLayoutParams<FrameLayout.LayoutParams> {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                }
            }
        }
    }

}