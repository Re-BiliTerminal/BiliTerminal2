package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ScaleEdgeLayoutManager extends LinearLayoutManager {
    public static final float RESET_SCALE = 1.0f;
    public static final float START_SCALE = 0.8f;

    public ScaleEdgeLayoutManager(Context context) {
        super(context, RecyclerView.VERTICAL, false);
    }

    @Override
    public int scrollVerticallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrollVerticallyBy = super.scrollVerticallyBy(i, recycler, state);
        scaleVerticalChildView();
        return scrollVerticallyBy;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException ignored) {}
        if (getItemCount() < 0 || state.isPreLayout()) {
            return;
        }
        scaleVerticalChildView();
    }

    private void scaleVerticalChildView() {
        int height = getHeight();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt != null) {
                int height2 = childAt.getHeight();
                int bottom = childAt.getBottom();
                float f = 1.0f;
                if (height2 < height && bottom > height) {
                    f = 0.8f + (((height2 - (bottom - height)) * 0.19999999f) / height2);
                }
                childAt.setPivotX(childAt.getWidth() / 2.0f);
                childAt.setPivotY(0.0f);
                childAt.setScaleX(f);
                childAt.setScaleY(f);
            }
        }
    }
}