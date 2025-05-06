package com.huanli233.biliterminal2.ui.utils.view;

import android.content.Context;

public class DensityUtil {
    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }
}