package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import static com.huanli233.biliterminal2.ui.utils.view.TypeArrayUtils.optBoolean;

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
import com.huanli233.biliterminal2.ui.utils.view.PressAnimHelper;
import com.huanli233.biliterminal2.ui.utils.view.UiTouchPointUtil;

public class AppRelativeLayout extends RelativeLayout {
    private static final int DEFAULT_STYLE_ATTR = 0;
    private static final long HORIZONTAL_ANIMATION_DURATION_MS = 350L;
    private static final long VERTICAL_ANIMATION_DURATION_MS = 400L;
    private static final float TARGET_TRANSLATION_RESET = 0.0f;

    private final Animator.AnimatorListener animationEndListener;
    private ObjectAnimator horizontalTranslationAnimator;
    private Point lastRawTouchPoint;
    private final PressAnimHelper pressAnimHelper;
    private final Runnable pressAnimationTask;
    private final Runnable releaseAnimationTask;
    private View specialDispatchTargetView; // Renamed for clarity
    private OnClickListener specialViewClickListener;
    private ObjectAnimator verticalTranslationAnimator;

    public AppRelativeLayout(Context context) {
        this(context, null);
    }

    public AppRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, DEFAULT_STYLE_ATTR);
    }

    public AppRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.animationEndListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                AppRelativeLayout.this.resetViewTranslation();
            }
        };
        this.pressAnimationTask = () -> AppRelativeLayout.this.pressAnimHelper.press();
        this.releaseAnimationTask = () -> AppRelativeLayout.this.pressAnimHelper.release();

        if (!isClickable()) {
            setClickable(true);
        }

        TypedArray styledAttributes = null;
        boolean useAlphaInPressAnim = true;
        boolean useZoomInPressAnim = true;

        if (attrs != null) {
            styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.AppRelativeLayout);
            useAlphaInPressAnim = optBoolean(styledAttributes, R.styleable.AppRelativeLayout_useAlphaForRL, true);
            useZoomInPressAnim = optBoolean(styledAttributes, R.styleable.AppRelativeLayout_useZoomForRL, true);
            styledAttributes.recycle();
        }
        this.pressAnimHelper = new PressAnimHelper(this, useAlphaInPressAnim, useZoomInPressAnim);
        initializeAnimations();
    }

    private void initializeAnimations() {
        this.horizontalTranslationAnimator = new ObjectAnimator();
        this.horizontalTranslationAnimator.setTarget(this);
        this.horizontalTranslationAnimator.setPropertyName("translationX");
        this.horizontalTranslationAnimator.setDuration(HORIZONTAL_ANIMATION_DURATION_MS);
        this.horizontalTranslationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.horizontalTranslationAnimator.addListener(this.animationEndListener);

        this.verticalTranslationAnimator = new ObjectAnimator();
        this.verticalTranslationAnimator.setTarget(this);
        this.verticalTranslationAnimator.setPropertyName("translationY");
        this.verticalTranslationAnimator.setDuration(VERTICAL_ANIMATION_DURATION_MS);
        this.verticalTranslationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.verticalTranslationAnimator.addListener(this.animationEndListener);
    }

    public void startHorizontalReturnAnimation(float fromTranslationX) {
        this.verticalTranslationAnimator.cancel();
        this.horizontalTranslationAnimator.cancel();
        resetViewTranslation(); // Reset before starting new animation
        this.horizontalTranslationAnimator.setFloatValues(fromTranslationX, TARGET_TRANSLATION_RESET);
        this.horizontalTranslationAnimator.start();
    }

    public void startVerticalReturnAnimation(float fromTranslationY) {
        this.horizontalTranslationAnimator.cancel();
        this.verticalTranslationAnimator.cancel();
        resetViewTranslation(); // Reset before starting new animation
        this.verticalTranslationAnimator.setFloatValues(fromTranslationY, TARGET_TRANSLATION_RESET);
        this.verticalTranslationAnimator.start();
    }

    public void resetViewTranslation() {
        setTranslationX(TARGET_TRANSLATION_RESET);
        setTranslationY(TARGET_TRANSLATION_RESET);
    }

    public void setSpecialDispatchTargetView(View view) {
        this.specialDispatchTargetView = view;
    }

    // This method seems to be intended for setting a special view and its click listener.
    // It might be better to have separate setters if they are used independently.
    public void setSpecialDispatchTargetViewAndListener(View view, OnClickListener onClickListener) {
        this.specialDispatchTargetView = view;
        this.specialViewClickListener = onClickListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.lastRawTouchPoint == null) {
            this.lastRawTouchPoint = new Point((int) event.getRawX(), (int) event.getRawY());
        } else {
            this.lastRawTouchPoint.set((int) event.getRawX(), (int) event.getRawY());
        }

        if (this.specialDispatchTargetView != null && UiTouchPointUtil.isTouchPointInView(this.specialDispatchTargetView, this.lastRawTouchPoint)) {
            // If touch is on the special view, release press animation and let super handle dispatch.
            removeCallbacks(this.pressAnimationTask);
            post(this.releaseAnimationTask);
            // If specialViewListener is set, it implies special handling for this view's touch.
            // However, the original `dealSpecialView` was not called here. If the intent is to consume
            // the event or trigger the listener directly, that logic should be here.
            // For now, just passing to super.dispatchTouchEvent.
            return super.dispatchTouchEvent(event);
        }

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(this.releaseAnimationTask);
                post(this.pressAnimationTask);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                removeCallbacks(this.pressAnimationTask);
                post(this.releaseAnimationTask);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    // This method was unused in the original dispatchTouchEvent logic for specialView.
    // If it's intended to be called, its invocation point needs to be determined.
    private void triggerSpecialViewClick() {
        if (this.specialDispatchTargetView != null && this.specialViewClickListener != null) {
            this.specialViewClickListener.onClick(this.specialDispatchTargetView);
        }
    }
}

