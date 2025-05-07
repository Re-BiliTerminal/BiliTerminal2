package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.ui.utils.view.PressAnimHelper;
import com.huanli233.biliterminal2.ui.utils.view.TypeArrayUtils;
import com.huanli233.biliterminal2.ui.utils.view.UiTouchPointUtil;

public class AppLinearLayout extends LinearLayout {
    private static final int DEFAULT_STYLE_ATTR = 0;

    private View forbidTouchDispatchView;
    private final PressAnimHelper pressAnimHelper;
    private final Runnable pressAnimationTask;
    private final Runnable releaseAnimationTask;
    private final Point lastTouchPointGlobal;

    public AppLinearLayout(Context context) {
        this(context, null);
    }

    public AppLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, DEFAULT_STYLE_ATTR);
    }

    public AppLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.lastTouchPointGlobal = new Point();
        this.pressAnimationTask = AppLinearLayout.this.pressAnimHelper::press;
        this.releaseAnimationTask = AppLinearLayout.this.pressAnimHelper::release;

        if (!isClickable()) {
            setClickable(true);
        }

        TypedArray styledAttributes = null;
        boolean useAlphaInPressAnim = true;
        boolean useZoomInPressAnim = true;

        if (attrs != null) {
            styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.AppLinearLayout);
            useAlphaInPressAnim = TypeArrayUtils.optBoolean(styledAttributes, R.styleable.AppLinearLayout_useAlphaForLL, true);
            useZoomInPressAnim = TypeArrayUtils.optBoolean(styledAttributes, R.styleable.AppLinearLayout_useZoomForLL, true);
            styledAttributes.recycle();
        }
        this.pressAnimHelper = new PressAnimHelper(this, useAlphaInPressAnim, useZoomInPressAnim);
    }

    public View getForbidTouchDispatchView() {
        return this.forbidTouchDispatchView;
    }

    public void setForbidTouchDispatchView(View view) {
        this.forbidTouchDispatchView = view;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        this.lastTouchPointGlobal.set((int) event.getRawX(), (int) event.getRawY());

        if (UiTouchPointUtil.isTouchPointInView(this.forbidTouchDispatchView, this.lastTouchPointGlobal)) {
            removeCallbacks(this.pressAnimationTask); // Cancel pending press task
            post(this.releaseAnimationTask); // Ensure release animation is played
            return super.dispatchTouchEvent(event);
        }

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(this.releaseAnimationTask); // Cancel pending release task
                post(this.pressAnimationTask);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                removeCallbacks(this.pressAnimationTask); // Cancel pending press task
                post(this.releaseAnimationTask);
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}

