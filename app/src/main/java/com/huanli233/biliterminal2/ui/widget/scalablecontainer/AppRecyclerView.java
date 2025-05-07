package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.ui.utils.view.ViewUtils;

import java.util.HashSet;
import java.util.Set;

public class AppRecyclerView extends RecyclerView {
    public static final long FLING_CALCULATE_INTERVAL_MS = 30L;
    public static final float OVER_TRANSLATION_DRAG_RATIO = 2.0f;
    private static final float MIN_OVER_TRANSLATION_FOR_RESET_DP = 30.0f;
    private static final int MIN_SCROLL_DIFF_FOR_VELOCITY_CALC_DP = 30;
    private static final int POINTER_INDEX_PRIMARY = 0;
    private static final float TARGET_TRANSLATION_Y_RESET = 0.0f;
    private static final int SCROLL_DIRECTION_VERTICAL = 1;
    private static final float SPRING_DAMPING_RATIO_NO_OSCILLATION = SpringForce.DAMPING_RATIO_NO_BOUNCY;
    private static final float SPRING_STIFFNESS_DEFAULT = SpringForce.STIFFNESS_MEDIUM;
    private static final int MAX_VELOCITY_DIVIDER = 2;

    private static final FloatPropertyCompat<AppRecyclerView> PROPERTY_OVER_TRANSLATION_Y = new FloatPropertyCompat<AppRecyclerView>("overTranslationY") {
        @Override
        public float getValue(AppRecyclerView appRecyclerView) {
            return appRecyclerView.getOverTranslationY();
        }

        @Override
        public void setValue(AppRecyclerView appRecyclerView, float value) {
            appRecyclerView.setOverTranslationYInternal(value);
        }
    };

    private final Set<View> animChildren;
    private final SpringAnimation animation;
    private final boolean enableEndOverScroll;
    private final boolean enableStartOverScroll;
    private int flingVelocityY;
    private boolean forbidEdgeDrag;
    private boolean isOverScrollAvailable = true;
    private long lastScrollTimeMs;
    private int lastScrollYPosition;
    private final int maxFlingVelocityY;
    private float currentOverTranslationY;
    private int currentScrollState;
    private int activePointerId;
    private int totalScrollDistanceY;

    public AppRecyclerView(Context context) {
        this(context, null);
    }

    public AppRecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AppRecyclerView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(attributeSet, R.styleable.AppRecyclerView, defStyleAttr, 0);
        this.enableStartOverScroll = attributes.getBoolean(R.styleable.AppRecyclerView_springEnableStartRV, true);
        this.enableEndOverScroll = attributes.getBoolean(R.styleable.AppRecyclerView_springEnableEndRV, true);
        attributes.recycle();

        this.animChildren = new HashSet<>();
        this.animation = new SpringAnimation(this, PROPERTY_OVER_TRANSLATION_Y)
                .setSpring(new SpringForce()
                        .setDampingRatio(SPRING_DAMPING_RATIO_NO_OSCILLATION)
                        .setStiffness(SPRING_STIFFNESS_DEFAULT));
        this.maxFlingVelocityY = ViewConfiguration.get(context).getScaledMaximumFlingVelocity() / MAX_VELOCITY_DIVIDER;
        this.activePointerId = MotionEvent.INVALID_POINTER_ID;
    }

    public float getOverTranslationY() {
        return this.currentOverTranslationY;
    }

    private void setOverTranslationYInternal(float translationY) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.animChildren.add(getChildAt(i));
        }

        for (View view : this.animChildren) {
            view.setTranslationY(translationY);
        }
        this.currentOverTranslationY = translationY;

        if (translationY == TARGET_TRANSLATION_Y_RESET) {
            this.animChildren.clear();
        }
    }

    public void setOverScrollAvailable(boolean available) {
        this.isOverScrollAvailable = available;
    }

    public void setForbidEdgeDrag(boolean forbid) {
        this.forbidEdgeDrag = forbid;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if (!this.isOverScrollAvailable) {
            return;
        }

        this.totalScrollDistanceY += dy;
        boolean isInAbsoluteStart = ViewUtils.isInAbsoluteStart(this, SCROLL_DIRECTION_VERTICAL);
        boolean isInAbsoluteEnd = ViewUtils.isInAbsoluteEnd(this, SCROLL_DIRECTION_VERTICAL);

        if (!isInAbsoluteStart && !isInAbsoluteEnd && isOverTranslationSignificant()) {
            stopSpringAnimation();
            setOverTranslationYInternal(TARGET_TRANSLATION_Y_RESET);
            this.flingVelocityY = 0;
            this.lastScrollYPosition = this.totalScrollDistanceY;
            this.lastScrollTimeMs = SystemClock.elapsedRealtime();
            return;
        }

        int scrollDiff = this.totalScrollDistanceY - this.lastScrollYPosition;
        if (Math.abs(scrollDiff) < MIN_SCROLL_DIFF_FOR_VELOCITY_CALC_DP) {
            return;
        }

        long currentTimeMs = SystemClock.elapsedRealtime();
        long deltaTimeMs = currentTimeMs - this.lastScrollTimeMs;

        if (deltaTimeMs > 0) {
            this.flingVelocityY = (int) ((scrollDiff * 1000L) / deltaTimeMs);
            this.lastScrollYPosition = this.totalScrollDistanceY;
            this.lastScrollTimeMs = currentTimeMs;
        }
    }

    private boolean isOverTranslationSignificant() {
        return Math.abs(this.currentOverTranslationY) > MIN_OVER_TRANSLATION_FOR_RESET_DP;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!this.isOverScrollAvailable || this.forbidEdgeDrag) {
            return super.dispatchTouchEvent(event);
        }

        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || !layoutManager.canScrollVertically() || getChildCount() == 0) {
            return super.dispatchTouchEvent(event);
        }

        int action = event.getActionMasked();
        boolean canOverScrollAtStart = ViewUtils.isInAbsoluteStart(this, SCROLL_DIRECTION_VERTICAL) && this.enableStartOverScroll;
        boolean canOverScrollAtEnd = ViewUtils.isInAbsoluteEnd(this, SCROLL_DIRECTION_VERTICAL) && this.enableEndOverScroll;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.activePointerId = event.getPointerId(POINTER_INDEX_PRIMARY);
                if (this.animation.isRunning() && this.currentOverTranslationY != TARGET_TRANSLATION_Y_RESET) {
                    stopSpringAnimation();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (this.activePointerId == MotionEvent.INVALID_POINTER_ID) break; 
                int pointerIndex = event.findPointerIndex(this.activePointerId);
                if (pointerIndex < 0) break;

                if (event.getHistorySize() > 0) {
                    float dy = event.getY(pointerIndex) - event.getHistoricalY(pointerIndex, 0);
                    if (Math.abs(dy) < Math.abs(event.getX(pointerIndex) - event.getHistoricalX(pointerIndex, 0))) {
                        return super.dispatchTouchEvent(event);
                    }

                    float currentTranslation = this.currentOverTranslationY;
                    if (currentTranslation != TARGET_TRANSLATION_Y_RESET) {
                        float newTranslation = currentTranslation + (dy / OVER_TRANSLATION_DRAG_RATIO);
                        if ((currentTranslation > TARGET_TRANSLATION_Y_RESET && newTranslation < TARGET_TRANSLATION_Y_RESET) || 
                            (currentTranslation < TARGET_TRANSLATION_Y_RESET && newTranslation > TARGET_TRANSLATION_Y_RESET)) {
                            newTranslation = TARGET_TRANSLATION_Y_RESET;
                        }
                        stopSpringAnimation();
                        setOverTranslationYInternal(newTranslation);
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                        return true;
                    } else if ((dy > TARGET_TRANSLATION_Y_RESET && canOverScrollAtStart) || (dy < TARGET_TRANSLATION_Y_RESET && canOverScrollAtEnd)) {
                        stopSpringAnimation();
                        setOverTranslationYInternal(dy / OVER_TRANSLATION_DRAG_RATIO);
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.activePointerId = MotionEvent.INVALID_POINTER_ID;
                if (this.currentOverTranslationY != TARGET_TRANSLATION_Y_RESET) {
                    finishOverScrollAnimation();
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void stopSpringAnimation() {
        if (this.animation.isRunning()) {
            this.animation.cancel();
        }
    }

    private void finishOverScrollAnimation() {
        this.animation.setStartVelocity(0);
        this.animation.animateToFinalPosition(TARGET_TRANSLATION_Y_RESET);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (!this.isOverScrollAvailable) {
            return;
        }

        if (this.currentScrollState == SCROLL_STATE_FLING && state == SCROLL_STATE_IDLE && isOverTranslationSignificant()) {
             // This might be too late, as fling could have already overscrolled and stopped.
             // The primary mechanism for fling overscroll should be within onScrolled or a custom fling handler.
        }
        this.currentScrollState = state;

        if (state == SCROLL_STATE_IDLE && this.currentOverTranslationY == TARGET_TRANSLATION_Y_RESET) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager != null && layoutManager.canScrollVertically()) {
                boolean canFlingOverScrollStart = ViewUtils.isInAbsoluteStart(this, SCROLL_DIRECTION_VERTICAL) && this.enableStartOverScroll;
                boolean canFlingOverScrollEnd = ViewUtils.isInAbsoluteEnd(this, SCROLL_DIRECTION_VERTICAL) && this.enableEndOverScroll;
                
                boolean shouldAnimateFling = false;
                if (canFlingOverScrollStart && this.flingVelocityY > 0) {
                    shouldAnimateFling = true;
                } else if (canFlingOverScrollEnd && this.flingVelocityY < 0) { // Flinging towards end
                    shouldAnimateFling = true;
                }

                if (shouldAnimateFling) {
                    float startVelocityForSpring = -this.flingVelocityY;
                    startVelocityForSpring = Math.max(-this.maxFlingVelocityY, Math.min(startVelocityForSpring, this.maxFlingVelocityY));
                    
                    if (Math.abs(startVelocityForSpring) > 0) {
                        this.animation.setStartVelocity(startVelocityForSpring);
                        this.animation.animateToFinalPosition(TARGET_TRANSLATION_Y_RESET);
                    }
                }
            }
        }
    }
}

