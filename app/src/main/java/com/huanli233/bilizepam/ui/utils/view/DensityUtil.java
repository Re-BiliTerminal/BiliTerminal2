package com.huanli233.bilizepam.ui.utils.view;

import android.content.Context;

public class DensityUtil {
    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }
}