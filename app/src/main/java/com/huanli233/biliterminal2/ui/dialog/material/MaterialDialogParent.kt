package com.huanli233.biliterminal2.ui.dialog.material

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MainThread

interface MaterialDialogParent : BackFragment {

    fun hasDialogOptionsMenu(): Boolean

    fun setHasDialogOptionsMenu(hasMenu: Boolean)

    @MainThread
    fun onCreateDialogOptionsMenu(menu: Menu, inflater: MenuInflater) {
    }

    @MainThread
    fun onPrepareDialogOptionsMenu(menu: Menu) {
    }

    @MainThread
    fun onDialogOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    @MainThread
    fun onDialogOptionsMenuClosed(menu: Menu) {
    }

    @MainThread
    fun openDialogOptionsMenu() {
    }

    @MainThread
    fun closeDialogOptionsMenu() {
    }

    @MainThread
    fun invalidateDialogOptionsMenu() {
    }
}