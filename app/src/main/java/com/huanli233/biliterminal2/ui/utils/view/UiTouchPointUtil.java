package com.huanli233.biliterminal2.ui.utils.view;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

public class UiTouchPointUtil {

    public static boolean isTouchPointInView(View view, Point[] pointArr) {
        if (view == null) {
            return false;
        }
        for (Point point : pointArr) {
            if (isTouchPointInView(view, point)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTouchPointInView(View view, Point point) {
        if (view == null || view.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        int i = iArr[0];
        int i2 = iArr[1];
        return point.x >= i && point.x <= view.getMeasuredWidth() + i && point.y >= i2 && point.y <= view.getMeasuredHeight() + i2;
    }

    public static boolean isTouchPointInRect(Point point, View view, int i, int i2, int i3, int i4) {
        int measuredHeight;
        int i5;
        if (view == null || view.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        int i6 = iArr[0] - i;
        int measuredWidth = iArr[0] + view.getMeasuredWidth() + i2;
        if (i3 == -1 || i4 == -1) {
            int[] iArr2 = new int[2];
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            viewGroup.getLocationOnScreen(iArr2);
            int i7 = iArr2[1];
            measuredHeight = viewGroup.getMeasuredHeight() + iArr2[1];
            i5 = i7;
        } else {
            i5 = iArr[1] - i3;
            measuredHeight = iArr[1] + view.getMeasuredHeight() + i4;
        }
        return point.x >= i6 && point.x <= measuredWidth && point.y >= i5 && point.y <= measuredHeight;
    }
}
