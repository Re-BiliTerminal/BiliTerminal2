package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class ObservableRecyclerView extends AppRecyclerView {
    private boolean isUp;
    private final List<OnScrollCallback> mScrollCallbacks = new ArrayList<>();

    public interface OnScrollCallback {
        void onScrollIdle(boolean isUp);

        void onScrolling();
    }

    public ObservableRecyclerView(Context context) {
        super(context);
        this.isUp = false;
    }

    public ObservableRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.isUp = false;
    }

    public ObservableRecyclerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.isUp = false;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if (this.mScrollCallbacks != null && canScrollVertically(1) && canScrollVertically(-1)) {
            this.isUp = dy <= 0;
            for (OnScrollCallback callback : mScrollCallbacks) {
                callback.onScrolling();
            }
        }
    }

    @Override
    public void onScrollStateChanged(int i) {
        super.onScrollStateChanged(i);
        if (i != SCROLL_STATE_IDLE) {
            return;
        }
        for (OnScrollCallback callback : mScrollCallbacks) {
            callback.onScrollIdle(this.isUp);
        }
    }

    public void addScrollCallback(OnScrollCallback onScrollCallback) {
        this.mScrollCallbacks.add(onScrollCallback);
    }

    public void removeScrollCallback(OnScrollCallback onScrollCallback) {
        this.mScrollCallbacks.remove(onScrollCallback);
    }

    public void removeAllScrollCallback() {
        this.mScrollCallbacks.clear();
    }
}