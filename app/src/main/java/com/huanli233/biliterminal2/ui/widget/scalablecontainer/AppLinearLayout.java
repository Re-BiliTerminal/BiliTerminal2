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
    int extraBottom;
    int extraLeft;
    int extraRight;
    int extraTop;
    private View forbidView;
    private final PressAnimHelper pressAnimHelper;
    private final Runnable pressTask;
    private final Runnable releaseTask;
    private final Point touchPoint;

    public AppLinearLayout(Context context) {
        this(context, null);
    }

    public AppLinearLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AppLinearLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.touchPoint = new Point();
        this.pressTask = new Runnable() {
            @Override
            public void run() {
                AppLinearLayout.this.pressAnimHelper.press();
            }
        };
        this.releaseTask = new Runnable() {
            @Override
            public void run() {
                AppLinearLayout.this.pressAnimHelper.release();
            }
        };
        setClickable(true);
        TypedArray obtainStyledAttributes = attributeSet != null ? context.obtainStyledAttributes(attributeSet, R.styleable.AppLinearLayout) : null;
        boolean optBoolean = TypeArrayUtils.optBoolean(obtainStyledAttributes, R.styleable.AppLinearLayout_useAlphaForLL, true);
        boolean optBoolean2 = TypeArrayUtils.optBoolean(obtainStyledAttributes, R.styleable.AppLinearLayout_useZoomForLL, true);
        if (obtainStyledAttributes != null) {
            obtainStyledAttributes.recycle();
        }
        this.pressAnimHelper = new PressAnimHelper(this, optBoolean, optBoolean2);
    }

    public View getForbidView() {
        return this.forbidView;
    }

    public void setForbidView(View view) {
        this.forbidView = view;
    }

    @Deprecated
    public void setForbidViewExtra(int i, int i2, int i3, int i4) {
        this.extraLeft = i;
        this.extraRight = i2;
        this.extraTop = i3;
        this.extraBottom = i4;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        this.touchPoint.set((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
        if (UiTouchPointUtil.isTouchPointInView(this.forbidView, this.touchPoint)) {
            this.pressAnimHelper.release();
            return super.dispatchTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            post(this.pressTask);
        } else if (action == 1 || action == 3) {
            post(this.releaseTask);
        }
        return super.dispatchTouchEvent(motionEvent);
    }
}
