package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import static com.huanli233.biliterminal2.ui.utils.TypeArrayUtils.optBoolean;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.ui.utils.PressAnimHelper;
import com.huanli233.biliterminal2.ui.utils.UiTouchPointUtil;

public class AppRelativeLayout extends RelativeLayout {
    private final Animator.AnimatorListener animatorListener;
    private ObjectAnimator horizontalAnimator;
    private Point point;
    private final PressAnimHelper pressAnimHelper;
    private final Runnable pressTask;
    private final Runnable releaseTask;
    private View specialView;
    private OnClickListener specialViewListener;
    private ObjectAnimator verticalAnimator;

    public AppRelativeLayout(Context context) {
        this(context, null);
    }

    public AppRelativeLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AppRelativeLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.animatorListener = new AnimatorListenerAdapter() {
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                AppRelativeLayout.this.resetTranslation();
            }
        };
        this.pressTask = new Runnable() {
            @Override
            public void run() {
                AppRelativeLayout.this.pressAnimHelper.press();
            }
        };
        this.releaseTask = new Runnable() {
            @Override
            public void run() {
                AppRelativeLayout.this.pressAnimHelper.release();
            }
        };
        setClickable(true);
        TypedArray obtainStyledAttributes = attributeSet != null ? context.obtainStyledAttributes(attributeSet, R.styleable.AppRelativeLayout) : null;
        boolean optBoolean = optBoolean(obtainStyledAttributes, R.styleable.AppRelativeLayout_useAlphaForRL, true);
        boolean optBoolean2 = optBoolean(obtainStyledAttributes, R.styleable.AppRelativeLayout_useZoomForRL, true);
        if (obtainStyledAttributes != null) {
            obtainStyledAttributes.recycle();
        }
        this.pressAnimHelper = new PressAnimHelper(this, optBoolean, optBoolean2);
        initAnimation();
    }

    private void initAnimation() {
        this.horizontalAnimator = new ObjectAnimator();
        this.horizontalAnimator.setTarget(this);
        this.horizontalAnimator.setPropertyName("translationX");
        this.horizontalAnimator.setDuration(350L);
        this.horizontalAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.horizontalAnimator.addListener(this.animatorListener);
        this.verticalAnimator = new ObjectAnimator();
        this.verticalAnimator.setTarget(this);
        this.verticalAnimator.setPropertyName("translationY");
        this.verticalAnimator.setDuration(400L);
        this.verticalAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.verticalAnimator.addListener(this.animatorListener);
    }

    public void startHorizontalAnimation(float f) {
        this.verticalAnimator.cancel();
        this.horizontalAnimator.cancel();
        resetTranslation();
        this.horizontalAnimator.setFloatValues(f, 0.0f);
        this.horizontalAnimator.start();
    }

    public void startVerticalAnimation(float f) {
        this.horizontalAnimator.cancel();
        this.verticalAnimator.cancel();
        resetTranslation();
        this.verticalAnimator.setFloatValues(f, 0.0f);
        this.verticalAnimator.start();
    }

    public void resetTranslation() {
        setTranslationX(0.0f);
        setTranslationY(0.0f);
    }

    public void setSpecialView(View view) {
        this.specialView = view;
    }

    private void setSpecialViewAndListener(View view, OnClickListener onClickListener) {
        this.specialView = view;
        this.specialViewListener = onClickListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        Point point = this.point;
        if (point == null) {
            this.point = new Point((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
        } else {
            point.set((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
        }
        if (UiTouchPointUtil.isTouchPointInView(this.specialView, this.point)) {
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

    private void dealSpecialView() {
        OnClickListener onClickListener;
        View view = this.specialView;
        if (view == null || (onClickListener = this.specialViewListener) == null) {
            return;
        }
        onClickListener.onClick(view);
    }
}
