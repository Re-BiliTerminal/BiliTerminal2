package com.huanli233.biliterminal2.ui.widget.recycler;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.util.MsgUtil;

public class CustomLinearManager extends LinearLayoutManager {
    public CustomLinearManager(Context context) {
        super(context);
    }

    public CustomLinearManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomLinearManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Throwable e){
            MsgUtil.err("列表报错：",e);
        }
    }
}
