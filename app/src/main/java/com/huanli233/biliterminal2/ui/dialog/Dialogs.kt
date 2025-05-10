package com.huanli233.biliterminal2.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListAdapter
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.color.MaterialColors
import com.huanli233.biliterminal2.R
import com.huanli233.biliterminal2.data.setting.LocalData
import com.huanli233.biliterminal2.ui.dialog.app.AlertDialog
import com.huanli233.biliterminal2.ui.dialog.app.MaterialAlertDialogBuilder
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

    fun Context.dialogBuilder() = if (LocalData.settings.theme.fullScreenDialogDisabled) {
        AppMaterialDialogBuilder(this, R.style.Theme_BiliTerminal2_Material3_AlertDialog)
    } else {
        AppMaterialDialogBuilder(this, R.style.Theme_BiliTerminal2_Material3_AlertDialog_FullScreen)
    }

}

class AppMaterialDialogBuilder(
    context: Context,
    themeResId: Int
) : MaterialAlertDialogBuilder(context, themeResId) {
    constructor(context: Context) : this(context, 0)

    override fun create(): AlertDialog {
        return super.create().apply {
            if (!LocalData.settings.theme.fullScreenDialogDisabled) {
                window?.apply {
                    runCatching {
                        requestFeature(Window.FEATURE_NO_TITLE)
                    }
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
            if (!LocalData.settings.theme.fullScreenDialogDisabled) {
                window?.decorView?.rootView?.findViewById<View>(androidx.appcompat.R.id.parentPanel)?.let {
                    it.updateLayoutParams<FrameLayout.LayoutParams> {
                        gravity = Gravity.CENTER_VERTICAL
                    }
                }
            }
        }
    }

    override fun setTitle(@StringRes titleId: Int): AppMaterialDialogBuilder {
        return super.setTitle(titleId) as AppMaterialDialogBuilder
    }

    override fun setTitle(title: CharSequence?): AppMaterialDialogBuilder {
        return super.setTitle(title) as AppMaterialDialogBuilder
    }

    override fun setCustomTitle(customTitleView: View?): AppMaterialDialogBuilder {
        return super.setCustomTitle(customTitleView) as AppMaterialDialogBuilder
    }

    override fun setMessage(@StringRes messageId: Int): AppMaterialDialogBuilder {
        return super.setMessage(messageId) as AppMaterialDialogBuilder
    }

    override fun setMessage(message: CharSequence?): AppMaterialDialogBuilder {
        return super.setMessage(message) as AppMaterialDialogBuilder
    }

    override fun setIcon(@DrawableRes iconId: Int): AppMaterialDialogBuilder {
        return super.setIcon(iconId) as AppMaterialDialogBuilder
    }

    override fun setIcon(icon: Drawable?): AppMaterialDialogBuilder {
        return super.setIcon(icon) as AppMaterialDialogBuilder
    }

    override fun setIconAttribute(@AttrRes attrId: Int): AppMaterialDialogBuilder {
        return super.setIconAttribute(attrId) as AppMaterialDialogBuilder
    }

    override fun setPositiveButton(
        @StringRes textId: Int,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setPositiveButton(textId, listener) as AppMaterialDialogBuilder
    }

    override fun setPositiveButton(
        text: CharSequence?,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setPositiveButton(text, listener) as AppMaterialDialogBuilder
    }

    override fun setPositiveButtonIcon(icon: Drawable?): AppMaterialDialogBuilder {
        return super.setPositiveButtonIcon(icon) as AppMaterialDialogBuilder
    }

    override fun setNegativeButton(
        @StringRes textId: Int,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setNegativeButton(textId, listener) as AppMaterialDialogBuilder
    }

    override fun setNegativeButton(
        text: CharSequence?,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setNegativeButton(text, listener) as AppMaterialDialogBuilder
    }

    override fun setNegativeButtonIcon(icon: Drawable?): AppMaterialDialogBuilder {
        return super.setNegativeButtonIcon(icon) as AppMaterialDialogBuilder
    }

    override fun setNeutralButton(
        @StringRes textId: Int,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setNeutralButton(textId, listener) as AppMaterialDialogBuilder
    }

    override fun setNeutralButton(
        text: CharSequence?,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setNeutralButton(text, listener) as AppMaterialDialogBuilder
    }

    override fun setNeutralButtonIcon(icon: Drawable?): AppMaterialDialogBuilder {
        return super.setNeutralButtonIcon(icon) as AppMaterialDialogBuilder
    }

    override fun setCancelable(cancelable: Boolean): AppMaterialDialogBuilder {
        return super.setCancelable(cancelable) as AppMaterialDialogBuilder
    }

    override fun setOnCancelListener(
        onCancelListener: DialogInterface.OnCancelListener?
    ): AppMaterialDialogBuilder {
        return super.setOnCancelListener(onCancelListener) as AppMaterialDialogBuilder
    }

    override fun setOnDismissListener(
        onDismissListener: DialogInterface.OnDismissListener?
    ): AppMaterialDialogBuilder {
        return super.setOnDismissListener(onDismissListener) as AppMaterialDialogBuilder
    }

    override fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?): AppMaterialDialogBuilder {
        return super.setOnKeyListener(onKeyListener) as AppMaterialDialogBuilder
    }

    override fun setItems(
        @ArrayRes itemsId: Int,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setItems(itemsId, listener) as AppMaterialDialogBuilder
    }

    override fun setItems(
        items: Array<CharSequence>?,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setItems(items, listener) as AppMaterialDialogBuilder
    }

    override fun setAdapter(
        adapter: ListAdapter?,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setAdapter(adapter, listener) as AppMaterialDialogBuilder
    }

    override fun setCursor(
        cursor: Cursor?,
        listener: DialogInterface.OnClickListener?,
        labelColumn: String
    ): AppMaterialDialogBuilder {
        return super.setCursor(cursor, listener, labelColumn) as AppMaterialDialogBuilder
    }

    override fun setMultiChoiceItems(
        @ArrayRes itemsId: Int,
        checkedItems: BooleanArray?,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ): AppMaterialDialogBuilder {
        return super.setMultiChoiceItems(itemsId, checkedItems, listener) as AppMaterialDialogBuilder
    }

    override fun setMultiChoiceItems(
        items: Array<CharSequence>?,
        checkedItems: BooleanArray?,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ): AppMaterialDialogBuilder {
        return super.setMultiChoiceItems(items, checkedItems, listener) as AppMaterialDialogBuilder
    }

    override fun setMultiChoiceItems(
        cursor: Cursor?,
        isCheckedColumn: String,
        labelColumn: String,
        listener: DialogInterface.OnMultiChoiceClickListener?
    ): AppMaterialDialogBuilder {
        return super.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener) as AppMaterialDialogBuilder
    }

    override fun setSingleChoiceItems(
        @ArrayRes itemsId: Int,
        checkedItem: Int,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setSingleChoiceItems(itemsId, checkedItem, listener) as AppMaterialDialogBuilder
    }

    override fun setSingleChoiceItems(
        cursor: Cursor?,
        checkedItem: Int,
        labelColumn: String,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener) as AppMaterialDialogBuilder
    }

    override fun setSingleChoiceItems(
        items: Array<CharSequence>?,
        checkedItem: Int,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setSingleChoiceItems(items, checkedItem, listener) as AppMaterialDialogBuilder
    }

    override fun setSingleChoiceItems(
        adapter: ListAdapter?,
        checkedItem: Int,
        listener: DialogInterface.OnClickListener?
    ): AppMaterialDialogBuilder {
        return super.setSingleChoiceItems(adapter, checkedItem, listener) as AppMaterialDialogBuilder
    }

    override fun setOnItemSelectedListener(
        listener: AdapterView.OnItemSelectedListener?
    ): AppMaterialDialogBuilder {
        return super.setOnItemSelectedListener(listener) as AppMaterialDialogBuilder
    }

    override fun setView(@LayoutRes layoutResId: Int): AppMaterialDialogBuilder {
        return super.setView(layoutResId) as AppMaterialDialogBuilder
    }

    override fun setView(view: View?): AppMaterialDialogBuilder {
        return super.setView(view) as AppMaterialDialogBuilder
    }

}