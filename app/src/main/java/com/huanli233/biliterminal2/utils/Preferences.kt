package com.huanli233.biliterminal2.utils

import android.content.SharedPreferences
import androidx.core.content.edit

object Preferences {

    var sharedPreferences: SharedPreferences? = null

    @JvmStatic
    fun getString(key: String, def: String): String {
        return sharedPreferences!!.getString(key, def) ?: def
    }

    @JvmStatic
    fun putString(key: String, value: String) {
        sharedPreferences!!.edit { putString(key, value) }
    }

    @JvmStatic
    fun getInt(key: String, def: Int): Int {
        return sharedPreferences!!.getInt(key, def)
    }

    @JvmStatic
    fun putInt(key: String, value: Int) {
        sharedPreferences!!.edit { putInt(key, value) }
    }

    @JvmStatic
    fun getLong(key: String, def: Long): Long {
        return sharedPreferences!!.getLong(key, def)
    }

    @JvmStatic
    fun putLong(key: String, value: Long) {
        sharedPreferences!!.edit { putLong(key, value) }
    }

    @JvmStatic
    fun getBoolean(key: String, def: Boolean): Boolean {
        return sharedPreferences!!.getBoolean(key, def)
    }

    @JvmStatic
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences!!.edit { putBoolean(key, value) }
    }

    @JvmStatic
    fun putFloat(key: String, value: Float) {
        sharedPreferences!!.edit { putFloat(key, value) }
    }

    @JvmStatic
    fun getFloat(key: String, def: Float): Float {
        return sharedPreferences!!.getFloat(key, def)
    }

    @JvmStatic
    fun removeValue(key: String) {
        sharedPreferences!!.edit { remove(key) }
    }
}
