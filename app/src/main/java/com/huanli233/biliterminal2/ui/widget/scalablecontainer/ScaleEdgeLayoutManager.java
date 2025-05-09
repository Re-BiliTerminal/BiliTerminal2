package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.utils.SystemConfigurationKt;

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

    private final boolean mIsRound = SystemConfigurationKt.isRound();

    private void scaleVerticalChildView() {
        int height = getHeight();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt != null) {
                int childHeight = childAt.getHeight();
                int bottom = childAt.getBottom();
                int top = childAt.getTop();
                float pivotY = /* childHeight / 2.0f */ 0.0f;
                float scale = RESET_SCALE;
                if (top < height && bottom > height) {
                    int visibleHeight = childHeight - (bottom - height);
                    visibleHeight = Math.max(0, visibleHeight);
                    scale = START_SCALE + (visibleHeight * 0.19999999f) / childHeight;
                    pivotY = 0.0f;
                } else if (top < 0 && bottom > 0 && mIsRound) {
                    int visibleHeight = childHeight + top;
                    visibleHeight = Math.max(0, visibleHeight);
                    scale = START_SCALE + (visibleHeight * 0.19999999f) / childHeight;
                    pivotY = (float) childHeight;
                }
                childAt.setPivotX(childAt.getWidth() / 2.0f);
                childAt.setPivotY(pivotY);
                childAt.setScaleX(scale);
                childAt.setScaleY(scale);
            }
        }
    }
}