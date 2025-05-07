package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.ui.utils.view.SpringAnimationUtils;
import com.huanli233.biliterminal2.ui.utils.view.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class AppScrollView extends ScrollView {
    public static final float DEFAULT_TOUCH_DRAG_MOVE_RATIO = 1.5f;
    private static final int DRAG_SIDE_END = 2;
    private static final int DRAG_SIDE_START = 1;
    public static final int OVER_SCROLLING_STATE = 1;
    public static final int OVER_SCROLL_FLING_ING = 4;
    public static final int OVER_SCROLL_STATE_BACKING = 2;
    public static final int OVER_SCROLL_STATE_IDLE = 0;
    public static final float RESET_SCALE = 1.0f;
    public static final float START_SCALE = 0.8f;
    public static final int TYPE_DRAG_OVER_BACK = 1;
    public static final int TYPE_FLING_BACK = 0;

    private static final int POINTER_INDEX_PRIMARY = 0;
    private static final float FLING_VELOCITY_DAMPING_FACTOR = 2.0f;
    private static final float TARGET_TRANSLATION_Y_RESET = 0.0f;
    private static final float SCALE_FACTOR_RANGE = 0.19999999f; 
    private static final float MIN_SCALE_FACTOR = 0.8f;
    private static final float PIVOT_CENTER_FACTOR = 2.0f;
    private static final float PIVOT_TOP_Y = 0.0f;
    private static final float SPRING_DAMPING_RATIO_ORIGINAL = SpringForce.DAMPING_RATIO_NO_BOUNCY; 
    private static final float SPRING_STIFFNESS_FLING_ORIGINAL = 115.0f; 
    private static final float SPRING_STIFFNESS_DRAG_BACK_ORIGINAL = 200.0f; 
    private static final int SCROLL_DIRECTION_VERTICAL = 1;

    private static final int PIXEL_TOLERANCE_FOR_BOUNDARY_CHECK = 2;

    private SpringAnimation anim;
    private List<View> animScaleViews;
    private final DynamicAnimation.OnAnimationEndListener animationEndListener;
    private boolean enableEnd;
    private boolean enableStart;
    private int flingOverScrollState;
    private float flingVelocityY;
    private boolean isAnimScale;
    private long lastTrackTime;
    private int lastY;
    private int overScrollState;
    private int startDragSide;
    private int startPointId = -1; 
    private final int minFlingVelocity;

    public AppScrollView(Context context) {
        this(context, null);
    }

    public AppScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AppScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.overScrollState = OVER_SCROLL_STATE_IDLE;
        this.flingOverScrollState = OVER_SCROLL_STATE_IDLE;
        this.enableStart = true;
        this.enableEnd = true;
        this.minFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

        this.animationEndListener = (dynamicAnimation, z, f, f2) -> {
            AppScrollView.this.overScrollState = OVER_SCROLL_STATE_IDLE;
            AppScrollView.this.flingOverScrollState = OVER_SCROLL_STATE_IDLE;
        };
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        this.isAnimScale = false; 
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.AppScrollView, 0, 0);
        this.enableStart = obtainStyledAttributes.getBoolean(R.styleable.AppScrollView_springEnableStartSV, true); 
        this.enableEnd = obtainStyledAttributes.getBoolean(R.styleable.AppScrollView_springEnableEndSV, true);     
        obtainStyledAttributes.recycle();
    }

    @Override
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
        if (this.isAnimScale) {
            View childAt = getChildAt(POINTER_INDEX_PRIMARY);
            if (childAt instanceof ViewGroup) {
                setAnimScaleViews(collectChildren((ViewGroup) childAt));
            }
        }
    }

    public void setAnimScale(boolean z) {
        this.isAnimScale = z;
    }

    public boolean isEnableStart() {
        return this.enableStart;
    }

    public void setEnableStart(boolean z) {
        this.enableStart = z;
    }

    public boolean isEnableEnd() {
        return this.enableEnd;
    }

    public void setEnableEnd(boolean z) {
        this.enableEnd = z;
    }

    public void setAnimScaleViews(List<View> list) {
        this.animScaleViews = list;
        if (this.isAnimScale) { 
            post(AppScrollView.this::scaleVerticalChildView);
        }
    }

    private boolean hasChild() {
        return getChildCount() > 0 && getChildAt(POINTER_INDEX_PRIMARY) != null;
    }

    @Override
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (!this.isAnimScale || this.animScaleViews == null) {
            return;
        }
        scaleVerticalChildView();
    }

    @Override
    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        doScrollChanged();
    }

    private boolean isEffectivelyAtStart() {
        if (getChildCount() == 0) {
            return true;
        }
        return getScrollY() <= PIXEL_TOLERANCE_FOR_BOUNDARY_CHECK;
    }

    private boolean isEffectivelyAtEnd() {
        if (getChildCount() == 0) {
            return true;
        }
        View child = getChildAt(0);
        if (child == null) return true;

        int scrollY = getScrollY();
        int viewportHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        int childActualHeight = child.getHeight();

        if (childActualHeight <= viewportHeight) {
            return true;
        }

        int maxScrollY = childActualHeight - viewportHeight;

        return scrollY >= (maxScrollY - PIXEL_TOLERANCE_FOR_BOUNDARY_CHECK);
    }

    private void doScrollChanged() {
        if (hasChild()) {
            View childView = getChildAt(POINTER_INDEX_PRIMARY); // Get child view

            long currentTimeMillis = System.currentTimeMillis();
            long deltaTime = currentTimeMillis - this.lastTrackTime;
            int currentScrollY = getScrollY();
            int lastScrollY = this.lastY;

            if (currentScrollY != lastScrollY && deltaTime > 0) {
                this.flingVelocityY = ((currentScrollY - lastScrollY) * 1000.0f) / ((float) deltaTime);
                this.lastY = currentScrollY;
                this.lastTrackTime = currentTimeMillis;
            } else if (deltaTime <= 0 && currentScrollY != lastScrollY) {
                this.lastY = currentScrollY;
                this.lastTrackTime = currentTimeMillis;
            } else if (currentScrollY == lastScrollY) {
                // Scroll position hasn't changed, flingVelocityY should ideally reflect the velocity leading to this stop.
                // No change to flingVelocityY here, it holds the last calculated moving velocity.
            }


            if (this.overScrollState == OVER_SCROLL_STATE_IDLE &&
                    this.flingOverScrollState == OVER_SCROLL_STATE_IDLE &&
                    childView != null &&
                    childView.getTranslationY() == TARGET_TRANSLATION_Y_RESET) {

                boolean atStart = isEffectivelyAtStart();
                boolean atEnd = isEffectivelyAtEnd();

                if (atStart && this.enableStart && this.flingVelocityY < -this.minFlingVelocity) {
                    this.flingOverScrollState = OVER_SCROLL_FLING_ING;
                    createAnimIfNeed(TYPE_FLING_BACK);
                    this.anim.setStartVelocity((-this.flingVelocityY) / FLING_VELOCITY_DAMPING_FACTOR);
                    this.anim.animateToFinalPosition(TARGET_TRANSLATION_Y_RESET);
                }
                else if (atEnd && this.enableEnd && this.flingVelocityY > this.minFlingVelocity) {
                    this.flingOverScrollState = OVER_SCROLL_FLING_ING;
                    createAnimIfNeed(TYPE_FLING_BACK);
                    this.anim.setStartVelocity((-this.flingVelocityY) / FLING_VELOCITY_DAMPING_FACTOR);
                    this.anim.animateToFinalPosition(TARGET_TRANSLATION_Y_RESET);
                }
            }

            if (!this.isAnimScale || this.animScaleViews == null) {
                return;
            }
            scaleVerticalChildView();
        }
    }

    public void scaleVerticalChildView() {
        if (this.animScaleViews == null) return;
        int scrollY = getScrollY();
        int measuredHeight = getMeasuredHeight();
        for (View view : this.animScaleViews) {
            if (view.getVisibility() == View.VISIBLE) {
                int top = view.getTop();
                int bottom = view.getBottom();
                int height = view.getHeight();
                int width = view.getWidth();
                int visibleBottom = scrollY + measuredHeight;
                if (bottom >= scrollY && top <= visibleBottom) {
                    float visiblePart = (bottom <= visibleBottom || top >= visibleBottom) ? height : visibleBottom - top;
                    float scale = (visiblePart * SCALE_FACTOR_RANGE / height) + MIN_SCALE_FACTOR;
                    view.setPivotX(width / PIVOT_CENTER_FACTOR);
                    view.setPivotY(PIVOT_TOP_Y);
                    view.setScaleX(scale);
                    view.setScaleY(scale);
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!hasChild()) {
            return super.dispatchTouchEvent(motionEvent);
        }

        View childView = getChildAt(POINTER_INDEX_PRIMARY);
        int action = motionEvent.getActionMasked();
        float currentTranslationY = childView.getTranslationY();

        if (this.anim != null && this.anim.isRunning()) {
            this.anim.cancel();
            this.overScrollState = OVER_SCROLL_STATE_IDLE;
            this.flingOverScrollState = OVER_SCROLL_STATE_IDLE;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.startPointId = motionEvent.getPointerId(POINTER_INDEX_PRIMARY);
                if (getScrollY() == this.lastY) {
                    this.flingVelocityY = 0f;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (this.startPointId == -1) this.startPointId = motionEvent.getPointerId(POINTER_INDEX_PRIMARY);

                int pointerIndex = motionEvent.findPointerIndex(this.startPointId);
                if (pointerIndex < 0) {
                    finishOverScrollIfNeeded();
                    return super.dispatchTouchEvent(motionEvent);
                }

                if (motionEvent.getHistorySize() > 0) {
                    float dy = motionEvent.getY(pointerIndex) - motionEvent.getHistoricalY(pointerIndex, 0);
                    if (Math.abs(dy) >= Math.abs(motionEvent.getX(pointerIndex) - motionEvent.getHistoricalX(pointerIndex, 0))) {
                        int currentDragSide = dy > 0.0f ? DRAG_SIDE_START : DRAG_SIDE_END;

                        boolean canOverScrollStart = isEffectivelyAtStart() && this.enableStart;
                        boolean canOverScrollEnd = isEffectivelyAtEnd() && this.enableEnd;

                        if (this.overScrollState == OVER_SCROLL_STATE_IDLE) {
                            if ((currentDragSide == DRAG_SIDE_START && canOverScrollStart) || (currentDragSide == DRAG_SIDE_END && canOverScrollEnd)) {
                                this.startDragSide = currentDragSide;
                                this.overScrollState = OVER_SCROLLING_STATE;
                                ViewParent parent = getParent();
                                if (parent != null) {
                                    parent.requestDisallowInterceptTouchEvent(true);
                                }
                            }
                        }

                        if (this.overScrollState == OVER_SCROLLING_STATE) {
                            float newTranslationY = currentTranslationY + (dy / DEFAULT_TOUCH_DRAG_MOVE_RATIO);
                            if (currentDragSide != this.startDragSide &&
                                    ((this.startDragSide == DRAG_SIDE_START && newTranslationY <= TARGET_TRANSLATION_Y_RESET) ||
                                            (this.startDragSide == DRAG_SIDE_END && newTranslationY >= TARGET_TRANSLATION_Y_RESET))) {
                                this.overScrollState = OVER_SCROLL_STATE_IDLE;
                                childView.setTranslationY(TARGET_TRANSLATION_Y_RESET);
                            } else {
                                childView.setTranslationY(newTranslationY);
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                boolean wasDraggingOverscroll = (this.overScrollState == OVER_SCROLLING_STATE);
                float childTransY = childView.getTranslationY();

                if (wasDraggingOverscroll || childTransY != TARGET_TRANSLATION_Y_RESET) {
                    finishOverScroll();
                } else {
                    if (this.flingOverScrollState == OVER_SCROLL_STATE_IDLE) {
                        boolean startFlingBack = isFlingBack();

                        if (startFlingBack) {
                            this.flingOverScrollState = OVER_SCROLL_FLING_ING;
                            createAnimIfNeed(TYPE_FLING_BACK);
                            this.anim.setStartVelocity((-this.flingVelocityY) / FLING_VELOCITY_DAMPING_FACTOR);
                            this.anim.animateToFinalPosition(TARGET_TRANSLATION_Y_RESET);
                        }
                    }
                }
                this.startPointId = -1;
                break;
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private boolean isFlingBack() {
        boolean isInAbsoluteStart = isEffectivelyAtStart();
        boolean isInAbsoluteEnd = isEffectivelyAtEnd();
        boolean startFlingBack = false;

        if (isInAbsoluteStart && this.enableStart && this.flingVelocityY < -this.minFlingVelocity) {
            startFlingBack = true;
        } else if (isInAbsoluteEnd && this.enableEnd && this.flingVelocityY > this.minFlingVelocity) {
            startFlingBack = true;
        }
        return startFlingBack;
    }

    private void finishOverScrollIfNeeded() { 
        if (this.overScrollState == OVER_SCROLLING_STATE || (hasChild() && getChildAt(POINTER_INDEX_PRIMARY).getTranslationY() != TARGET_TRANSLATION_Y_RESET)) {
            finishOverScroll();
        }
    }

    private void finishOverScroll() {
        if (this.overScrollState == OVER_SCROLLING_STATE || (hasChild() && getChildAt(POINTER_INDEX_PRIMARY).getTranslationY() != TARGET_TRANSLATION_Y_RESET)) {
            this.overScrollState = OVER_SCROLL_STATE_BACKING;
            createAnimIfNeed(TYPE_DRAG_OVER_BACK);
            this.anim.setStartVelocity(TARGET_TRANSLATION_Y_RESET) 
                      .animateToFinalPosition(TARGET_TRANSLATION_Y_RESET);
        } else {
             this.overScrollState = OVER_SCROLL_STATE_IDLE;
        }
    }

    private void createAnimIfNeed(int type) {
        if (!hasChild()) return;
        View childView = getChildAt(POINTER_INDEX_PRIMARY);
        if (this.anim == null) {
            this.anim = new SpringAnimation(childView, SpringAnimationUtils.FLOAT_PROPERTY_TRANSLATION_Y)
                .setSpring(new SpringForce().setDampingRatio(SPRING_DAMPING_RATIO_ORIGINAL));
            this.anim.addEndListener(this.animationEndListener);
        }
        SpringForce spring = this.anim.getSpring();
        if (type == TYPE_FLING_BACK) {
            spring.setStiffness(SPRING_STIFFNESS_FLING_ORIGINAL);
        } else if (type == TYPE_DRAG_OVER_BACK) {
            spring.setStiffness(SPRING_STIFFNESS_DRAG_BACK_ORIGINAL);
        }
    }

    private List<View> collectChildren(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        ArrayList<View> arrayList = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            arrayList.add(viewGroup.getChildAt(i));
        }
        return arrayList;
    }
}

