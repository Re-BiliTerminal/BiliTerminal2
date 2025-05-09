package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

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

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.ui.utils.view.ViewUtils;
import com.huanli233.biliterminal2.ui.widget.wearable.WearableRecyclerView;

import java.util.HashSet;
import java.util.Set;

public class AppRecyclerView extends WearableRecyclerView {
    // Region: Constants
    private static final int SCROLL_CALCULATION_INTERVAL_MS = 30;
    private static final float OVERSCROLL_TRANSLATION_RATIO = 2.0f;
    private static final float OVERSCROLL_RESET_THRESHOLD = 30.0f;
    private static final float SPRING_DAMPING_RATIO = 1.0f;
    private static final float SPRING_STIFFNESS = 150.0f;
    private static final int VELOCITY_MULTIPLIER = 1000;
    private static final float OVERSCROLL_DRAG_DIVIDER = 2.0f;

    private static final FloatPropertyCompat<AppRecyclerView> OVER_TRANSLATION_Y_PROPERTY =
            new FloatPropertyCompat<>("overTranslationY") {
                @Override
                public float getValue(AppRecyclerView view) {
                    return view.getOverTranslationY();
                }

                @Override
                public void setValue(AppRecyclerView view, float value) {
                    view.setOverTranslationY((int) value);
                }
            };
    // EndRegion

    // Region: Member Variables
    private final Set<View> animatedChildren = new HashSet<>();
    private final SpringAnimation springAnimation;
    private final boolean isSpringEnabledAtStart;
    private final boolean isSpringEnabledAtEnd;
    private final int maxFlingVelocityY;

    private int currentFlingVelocityY;
    private boolean isEdgeDragForbidden;
    private boolean isInteractionEnabled = true;
    private long lastScrollTimestamp;
    private int lastScrollPositionY;
    private float overTranslationY;
    private int currentScrollState;
    private int touchStartPointerId;
    private int totalScrollY;
    // EndRegion

    // Region: Constructors
    public AppRecyclerView(Context context) {
        this(context, null);
    }

    public AppRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.AppRecyclerView);
        isSpringEnabledAtStart = attributes.getBoolean(R.styleable.AppRecyclerView_springEnableStartRV, true);
        isSpringEnabledAtEnd = attributes.getBoolean(R.styleable.AppRecyclerView_springEnableEndRV, true);
        attributes.recycle();

        SpringForce spring = new SpringForce()
                .setDampingRatio(SPRING_DAMPING_RATIO)
                .setStiffness(SPRING_STIFFNESS);
        springAnimation = new SpringAnimation(this, OVER_TRANSLATION_Y_PROPERTY).setSpring(spring);
        maxFlingVelocityY = ViewConfiguration.get(context).getScaledMaximumFlingVelocity() / 2;
    }
    // EndRegion

    // Region: Public Methods
    public float getOverTranslationY() {
        return overTranslationY;
    }

    public void setInteractionEnabled(boolean enabled) {
        isInteractionEnabled = enabled;
    }

    public void setEdgeDragForbidden(boolean forbidden) {
        isEdgeDragForbidden = forbidden;
    }
    // EndRegion

    // Region: RecyclerView Overrides
    @Override
    public void onScrolled(int scrollX, int scrollY) {
        if (!isInteractionEnabled) {
            super.onScrolled(scrollX, scrollY);
            return;
        }

        totalScrollY += scrollY;
        final boolean isAtStart = ViewUtils.isInAbsoluteStart(this, View.FOCUS_DOWN);
        final boolean isAtEnd = ViewUtils.isInAbsoluteEnd(this, View.FOCUS_DOWN);

        if (!isAtStart && !isAtEnd && shouldResetOverTranslation()) {
            resetOverScrollState();
            return;
        }

        updateFlingVelocity(scrollY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isInteractionEnabled) {
            return super.dispatchTouchEvent(event);
        }

        if (isEdgeDragForbidden) {
            return super.dispatchTouchEvent(event);
        }

        final int action = event.getActionMasked();
        final LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || !layoutManager.canScrollVertically() || getChildCount() == 0) {
            return super.dispatchTouchEvent(event);
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                handleMoveEvent(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                finishOverScroll();
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onScrollStateChanged(int newState) {
        if (!isInteractionEnabled) {
            super.onScrollStateChanged(newState);
            return;
        }

        if (shouldProcessScrollStateChange(newState)) {
            handleFlingAfterScroll(newState);
        }
        currentScrollState = newState;
    }
    // EndRegion

    // Region: Private Helpers
    private boolean shouldResetOverTranslation() {
        return Math.abs(overTranslationY) > OVERSCROLL_RESET_THRESHOLD;
    }

    private void resetOverScrollState() {
        stopSpringAnimation();
        setOverTranslationY(0);
        currentFlingVelocityY = 0;
        lastScrollPositionY = totalScrollY;
        lastScrollTimestamp = SystemClock.elapsedRealtime();
    }

    private void updateFlingVelocity(int deltaScrollY) {
        if (Math.abs(deltaScrollY) < SCROLL_CALCULATION_INTERVAL_MS) {
            return;
        }

        final long currentTime = SystemClock.elapsedRealtime();
        final long elapsedTime = currentTime - lastScrollTimestamp;

        if (elapsedTime > 0) {
            currentFlingVelocityY = (int) ((deltaScrollY * VELOCITY_MULTIPLIER) / elapsedTime);
            lastScrollPositionY = totalScrollY;
            lastScrollTimestamp = currentTime;
        }
    }

    private void handleMoveEvent(MotionEvent event) {
        if (event.getHistorySize() == 0) return;

        final float deltaY = event.getY(0) - event.getHistoricalY(0, 0);
        final float deltaX = event.getX(0) - event.getHistoricalX(0, 0);

        if (Math.abs(deltaY) < Math.abs(deltaX)) return;

        processVerticalScroll(event, deltaY);
    }

    private void processVerticalScroll(MotionEvent event, float deltaY) {
        final int pointerId = event.getPointerId(0);
        final boolean isAtStart = ViewUtils.isInAbsoluteStart(this, View.FOCUS_DOWN) && isSpringEnabledAtStart;
        final boolean isAtEnd = ViewUtils.isInAbsoluteEnd(this, View.FOCUS_DOWN) && isSpringEnabledAtEnd;

        if (overTranslationY != 0) {
            handleExistingOverScroll(pointerId, deltaY);
        } else if ((deltaY > 0 && isAtStart) || (deltaY < 0 && isAtEnd)) {
            startNewOverScroll(pointerId, deltaY);
        }
    }

    private void handleExistingOverScroll(int pointerId, float deltaY) {
        if (pointerId != touchStartPointerId) return;

        int newTranslation = (int) ((deltaY / OVERSCROLL_DRAG_DIVIDER) + overTranslationY);
        if (newTranslation * overTranslationY >= 0) {
            stopSpringAnimation();
            setOverTranslationY(newTranslation);
        }
    }

    private void startNewOverScroll(int pointerId, float deltaY) {
        touchStartPointerId = pointerId;
        setOverTranslationY((int) (deltaY / OVERSCROLL_DRAG_DIVIDER));
        requestParentDisallowInterceptTouchEvent();
    }

    private void requestParentDisallowInterceptTouchEvent() {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private boolean shouldProcessScrollStateChange(int newState) {
        return currentScrollState == SCROLL_STATE_SETTLING &&
                newState == SCROLL_STATE_IDLE &&
                !shouldResetOverTranslation();
    }

    private void handleFlingAfterScroll(int newState) {
        final LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || !layoutManager.canScrollVertically()) return;

        final boolean isAtStart = ViewUtils.isInAbsoluteStart(this, View.FOCUS_DOWN) && isSpringEnabledAtStart;
        final boolean isAtEnd = ViewUtils.isInAbsoluteEnd(this, View.FOCUS_DOWN) && isSpringEnabledAtEnd;

        if (shouldApplySpringForce(isAtStart, isAtEnd)) {
            applySpringAnimation();
        }
    }

    private boolean shouldApplySpringForce(boolean isAtStart, boolean isAtEnd) {
        return (isAtStart && currentFlingVelocityY < 0) ||
                (isAtEnd && currentFlingVelocityY > 0);
    }

    private void applySpringAnimation() {
        float velocity = Math.min(Math.abs(currentFlingVelocityY), maxFlingVelocityY);
        velocity = currentFlingVelocityY > 0 ? -velocity : velocity;
        springAnimation.setStartVelocity(velocity).animateToFinalPosition(0);
    }

    private void stopSpringAnimation() {
        if (springAnimation.isRunning()) {
            springAnimation.cancel();
        }
    }

    private void setOverTranslationY(float translation) {
        setTranslationY(translation);
        overTranslationY = translation;

        /*if (translation == 0) {
            animatedChildren.clear();
        }*/
    }

    private void finishOverScroll() {
        springAnimation.animateToFinalPosition(0);
    }
    // EndRegion
}