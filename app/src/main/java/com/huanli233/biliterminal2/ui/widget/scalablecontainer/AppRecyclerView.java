package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas; // Needed for draw method
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.ui.utils.view.ViewUtils; // Assuming this is still used or can be removed if not
import com.huanli233.biliterminal2.ui.widget.wearable.WearableRecyclerView;

public class AppRecyclerView extends WearableRecyclerView {
    // Region: Constants
    private static final int SCROLL_CALCULATION_INTERVAL_MS = 30;
    private static final float OVERSCROLL_RESET_THRESHOLD = 30.0f;
    private static final float SPRING_DAMPING_RATIO = 1.0f;
    private static final float SPRING_STIFFNESS = 150.0f;
    private static final int VELOCITY_MULTIPLIER = 1000;
    private static final float OVERSCROLL_DRAG_DIVIDER = 2.0f;

    private float overTranslationY = 0.0f; // Stores the current visual overscroll offset

    private static final FloatPropertyCompat<AppRecyclerView> OVER_TRANSLATION_Y_PROPERTY =
            new FloatPropertyCompat<AppRecyclerView>("overTranslationY") {
                @Override
                public float getValue(AppRecyclerView view) {
                    return view.overTranslationY;
                }

                @Override
                public void setValue(AppRecyclerView view, float value) {
                    view.setOverTranslationYValue(value);
                }
            };
    // EndRegion

    // Region: Member Variables
    private final SpringAnimation springAnimation;
    private final boolean isSpringEnabledAtStart;
    private final boolean isSpringEnabledAtEnd;
    private final boolean autoFocus;
    private final int maxFlingVelocityY;

    private int currentFlingVelocityY;
    private boolean isEdgeDragForbidden;
    private boolean isInteractionEnabled = true;
    private long lastScrollTimestamp;
    private int lastScrollPositionY;
    private int currentScrollState;
    private int touchStartPointerId = -1;
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
        autoFocus = attributes.getBoolean(R.styleable.AppRecyclerView_autoFocusRV, true);
        boolean verticalScrollBar = attributes.getBoolean(R.styleable.AppRecyclerView_verticalScroll, true);
        boolean horizontalScrollBar = attributes.getBoolean(R.styleable.AppRecyclerView_horizontalScroll, false);
        attributes.recycle();

        SpringForce spring = new SpringForce()
                .setDampingRatio(SPRING_DAMPING_RATIO)
                .setStiffness(SPRING_STIFFNESS);
        springAnimation = new SpringAnimation(this, OVER_TRANSLATION_Y_PROPERTY).setSpring(spring);
        maxFlingVelocityY = ViewConfiguration.get(context).getScaledMaximumFlingVelocity() / 2;

        if (verticalScrollBar && !horizontalScrollBar) {
            setVerticalScrollBarEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ViewCompat.setScrollIndicators(this, ViewCompat.SCROLL_INDICATOR_TOP | ViewCompat.SCROLL_INDICATOR_BOTTOM, ViewCompat.SCROLL_INDICATOR_TOP | ViewCompat.SCROLL_INDICATOR_BOTTOM);
            }
        }
        if (horizontalScrollBar && !verticalScrollBar) {
            setHorizontalScrollBarEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ViewCompat.setScrollIndicators(this, ViewCompat.SCROLL_INDICATOR_START | ViewCompat.SCROLL_INDICATOR_END, ViewCompat.SCROLL_INDICATOR_START | ViewCompat.SCROLL_INDICATOR_END);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDefaultFocusHighlightEnabled(false);
        }
        if (autoFocus) {
            setFocusable(true);
            setFocusableInTouchMode(true);
        }
    }
    // EndRegion

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFocusable() && !isFocused()) {
            requestFocus();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && autoFocus && isFocusable() && !isFocused()) {
            requestFocus();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (autoFocus && visibility == View.VISIBLE && isFocusable() && !isFocused()) {
            requestFocus();
        }
    }

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
        super.onScrolled(scrollX, scrollY);
        if (!isInteractionEnabled) {
            return;
        }

        totalScrollY += scrollY;
        final boolean isAtStart = !canScrollVertically(-1);
        final boolean isAtEnd = !canScrollVertically(1);

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
            case MotionEvent.ACTION_DOWN:
                touchStartPointerId = event.getPointerId(0);
                if (springAnimation.isRunning()) {
                    springAnimation.cancel();
                }
                break;
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
        super.onScrollStateChanged(newState);
        if (!isInteractionEnabled) {
            return;
        }

        if (shouldProcessScrollStateChange(newState)) {
            handleFlingAfterScroll();
        }
        currentScrollState = newState;
    }

    /**
     * Overridden draw method to handle global canvas translation for overscroll effect.
     * This ensures items and decorations are drawn translated, while scrollbars are
     * redrawn in their correct, untranslated positions.
     */
    @Override
    public void draw(Canvas canvas) {
        if (overTranslationY != 0 && getLayoutManager() != null && getLayoutManager().canScrollVertically()) {
            final int saveCount = canvas.save();
            try {
                canvas.translate(0, overTranslationY);
                super.draw(canvas);
            } finally {
                canvas.restoreToCount(saveCount);
            }

            super.onDrawScrollBars(canvas);
        } else {
            super.draw(canvas);
        }
    }
    // EndRegion

    // Region: Private Helpers
    private void setOverTranslationYValue(float translation) {
        if (this.overTranslationY == translation) {
            return;
        }
        this.overTranslationY = translation;
        invalidate();
    }

    private boolean shouldResetOverTranslation() {
        final boolean isAtStart = !canScrollVertically(-1);
        final boolean isAtEnd = !canScrollVertically(1);
        return !isAtStart && !isAtEnd && Math.abs(overTranslationY) > OVERSCROLL_RESET_THRESHOLD;
    }

    private void resetOverScrollState() {
        stopSpringAnimation();
        setOverTranslationYValue(0f);
        currentFlingVelocityY = 0;
        lastScrollPositionY = totalScrollY;
        lastScrollTimestamp = SystemClock.elapsedRealtime();
    }

    private void updateFlingVelocity(int deltaScrollY) {
        final long currentTime = SystemClock.elapsedRealtime();
        final long elapsedTime = currentTime - lastScrollTimestamp;

        if (elapsedTime > SCROLL_CALCULATION_INTERVAL_MS / 2) {
            currentFlingVelocityY = (int) (((float) deltaScrollY * VELOCITY_MULTIPLIER) / elapsedTime);
            lastScrollPositionY = totalScrollY;
            lastScrollTimestamp = currentTime;
        } else if (deltaScrollY == 0 && elapsedTime > SCROLL_CALCULATION_INTERVAL_MS) {
            currentFlingVelocityY = 0;
        }
    }

    private void handleMoveEvent(MotionEvent event) {
        if (touchStartPointerId == -1) return;

        final int pointerIndex = event.findPointerIndex(touchStartPointerId);
        if (pointerIndex < 0 || event.getHistorySize() == 0) return;

        final float historicalY = event.getHistoricalY(pointerIndex, 0);
        final float currentY = event.getY(pointerIndex);
        final float deltaY = currentY - historicalY;

        final float historicalX = event.getHistoricalX(pointerIndex, 0);
        final float currentX = event.getX(pointerIndex);
        final float deltaX = currentX - historicalX;

        LayoutManager lm = getLayoutManager();
        if (lm == null) return;

        if (lm.canScrollHorizontally() && Math.abs(deltaX) > Math.abs(deltaY) && overTranslationY == 0) {
            return;
        }

        if (lm.canScrollVertically()) {
            processVerticalScroll(deltaY);
        }
    }

    private void processVerticalScroll(float deltaY) {
        final boolean isAtStart = !canScrollVertically(-1) && isSpringEnabledAtStart;
        final boolean isAtEnd = !canScrollVertically(1) && isSpringEnabledAtEnd;

        if (overTranslationY != 0) {
            handleExistingOverScroll(deltaY);
        } else if ((deltaY > 0 && isAtStart) || (deltaY < 0 && isAtEnd)) {
            startNewOverScroll(deltaY);
        }
    }

    private void handleExistingOverScroll(float deltaY) {
        float newTranslation = (deltaY / OVERSCROLL_DRAG_DIVIDER) + overTranslationY;
        if ((newTranslation * overTranslationY) >= 0 || Math.abs(newTranslation) < Math.abs(overTranslationY)) {
            stopSpringAnimation();
            setOverTranslationYValue(newTranslation);
        } else {
            setOverTranslationYValue(0f);
        }
    }

    private void startNewOverScroll(float deltaY) {
        setOverTranslationYValue(deltaY / OVERSCROLL_DRAG_DIVIDER);
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

    private void handleFlingAfterScroll() {
        final LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || !layoutManager.canScrollVertically()) return;

        final boolean isAtStart = !canScrollVertically(-1);
        final boolean isAtEnd = !canScrollVertically(1);

        if (shouldApplySpringForce(isAtStart, isAtEnd)) {
            applySpringAnimation();
        } else if (overTranslationY != 0 && !springAnimation.isRunning()) {
            finishOverScroll();
        }
    }

    private boolean shouldApplySpringForce(boolean isAtStart, boolean isAtEnd) {
        return (isAtStart && currentFlingVelocityY < 0 && isSpringEnabledAtStart) ||
                (isAtEnd && currentFlingVelocityY > 0 && isSpringEnabledAtEnd);
    }

    private void applySpringAnimation() {
        springAnimation.setStartVelocity(currentFlingVelocityY);
        springAnimation.animateToFinalPosition(0f);
    }

    private void stopSpringAnimation() {
        if (springAnimation.isRunning()) {
            springAnimation.cancel();
        }
    }

    private void finishOverScroll() {
        if (overTranslationY != 0 || springAnimation.isRunning()) {
            if (!springAnimation.isRunning() && overTranslationY != 0) {
                springAnimation.setStartVelocity(0);
            }
            springAnimation.animateToFinalPosition(0f);
        }
        touchStartPointerId = -1;
    }
    // EndRegion
}
