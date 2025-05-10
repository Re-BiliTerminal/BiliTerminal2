/**
 * Copyright (c) 2017 Rikka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.huanli233.biliterminal2.ui.dialog.material

import android.app.ActionBar
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.AppCompatDialog

open class MaterialDialog : AppCompatDialog, TranslucentSystemBars {

    var parent: MaterialDialogParent? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (parent is TranslucentSystemBars) {
            if ((parent as TranslucentSystemBars).shouldApplyTranslucentSystemBars()) {
                (parent as TranslucentSystemBars).onApplyTranslucentSystemBars()
            }
        } else {
            if (shouldApplyTranslucentSystemBars()) {
                onApplyTranslucentSystemBars()
            }
        }
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        return when (featureId) {
            Window.FEATURE_OPTIONS_PANEL -> {
                if (onOptionsItemSelected(item)) {
                    return true
                }
                if (item.itemId == android.R.id.home && supportActionBar != null && supportActionBar.displayOptions and ActionBar.DISPLAY_HOME_AS_UP != 0) {
                    onBackPressed()
                    return true
                }
                false
            }
            Window.FEATURE_CONTEXT_MENU -> {
                onContextItemSelected(item)
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        if (parent?.onBackPressed() == true) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val hasMenu = parent?.hasDialogOptionsMenu() == true
        if (hasMenu) {
            parent!!.onCreateDialogOptionsMenu(menu, delegate.menuInflater)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val hasMenu = parent?.hasDialogOptionsMenu() == true
        if (hasMenu) {
            parent!!.onPrepareDialogOptionsMenu(menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val hasMenu = parent?.hasDialogOptionsMenu() == true
        if (hasMenu && parent!!.onDialogOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        val hasMenu = parent?.hasDialogOptionsMenu() == true
        if (hasMenu) {
            parent!!.onDialogOptionsMenuClosed(menu)
            return
        }
        super.onOptionsMenuClosed(menu)
    }
}