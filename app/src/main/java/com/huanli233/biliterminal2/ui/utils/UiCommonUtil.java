package com.huanli233.biliterminal2.ui.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

public class UiCommonUtil {

    public static int getColor(Context context, int i) {
        if (context == null || i == 0) {
            return -1;
        }
        return ResourcesCompat.getColor(context.getResources(), i, context.getTheme());
    }

    public static int[] getColorArray(Context context, int[] iArr) {
        int[] iArr2 = iArr.clone();
        if (context != null) {
            for (int i = 0; i < iArr2.length; i++) {
                iArr2[i] = getColor(context, iArr2[i]);
            }
        }
        return iArr2;
    }

    public static int dp2Px(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static float[] dp2Px(Context context, float[] fArr) {
        float[] fArr2 = fArr.clone();
        if (context != null) {
            for (int i = 0; i < fArr2.length; i++) {
                fArr2[i] = dp2Px(context, fArr2[i]);
            }
        }
        return fArr2;
    }

    public static int px2Dp(Context context, float f) {
        return (int) ((f / context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static float[] px2Dp(Context context, float[] fArr) {
        float[] fArr2 = fArr.clone();
        if (context != null) {
            for (int i = 0; i < fArr2.length; i++) {
                fArr2[i] = px2Dp(context, fArr2[i]);
            }
        }
        return fArr2;
    }

    public static int getFontHeight(String str) {
        if (TextUtils.isEmpty(str)) {
            return -1;
        }
        Rect rect = new Rect();
        new Paint().getTextBounds(str, 0, str.length(), rect);
        return rect.height();
    }

    public static int getFontWidth(String str) {
        if (TextUtils.isEmpty(str)) {
            return -1;
        }
        Rect rect = new Rect();
        new Paint().getTextBounds(str, 0, str.length(), rect);
        return rect.width();
    }

    public static void showView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void hideView(View view) {
        if (view.getVisibility() != View.GONE) {
            view.setVisibility(View.GONE);
        }
    }
}