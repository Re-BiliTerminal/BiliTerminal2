package com.huanli233.biliterminal2.ui.utils.view;

import android.view.View;

public class ViewUtils {
    public static final int DIRECTION_END = 1;
    public static final int DIRECTION_START = -1;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static boolean isInAbsoluteStart(View view, int i) {
        if (i == 0) {
            return !view.canScrollHorizontally(-1);
        }
        return !view.canScrollVertically(-1);
    }

    public static boolean isInAbsoluteEnd(View view, int i) {
        if (i == 0) {
            return !view.canScrollHorizontally(1);
        }
        return !view.canScrollVertically(1);
    }

}
