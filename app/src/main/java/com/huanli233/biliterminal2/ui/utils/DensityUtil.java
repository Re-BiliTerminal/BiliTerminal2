package com.huanli233.biliterminal2.ui.utils;

import android.content.Context;

public class DensityUtil {
    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }
}