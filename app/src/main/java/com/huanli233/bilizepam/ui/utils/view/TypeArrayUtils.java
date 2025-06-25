package com.huanli233.bilizepam.ui.utils.view;

import android.content.res.TypedArray;

public class TypeArrayUtils {
    public static boolean optBoolean(TypedArray typedArray, int i, boolean z) {
        return typedArray == null ? z : typedArray.getBoolean(i, z);
    }
}