package com.huanli233.bilizepam.ui.widget.scalablecontainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.core.widget.NestedScrollView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.huanli233.bilizepam.R;
import com.huanli233.bilizepam.ui.utils.view.SpringAnimationUtils;
import com.huanli233.bilizepam.ui.utils.view.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class AppNestedScrollView extends NestedScrollView {
    public static final float DEFAULT_TOUCH_DRAG_MOVE_RATIO = 1.5f;
    private static final int DRAG_SIDE_END = 2;
    private static final int DRAG_SIDE_START = 1;
    public static final int OVER_SCROLLING_STATE = 1;
    public static final int OVER_SCROLL_FLING_CHECKING = 3;
    public static final int OVER_SCROLL_FLING_ING = 4;
    public static final int OVER_SCROLL_STATE_BACKING = 2;
    public static final int OVER_SCROLL_STATE_IDLE = 0;
    public static final float RESET_SCALE = 1.0f;
    public static final float START_SCALE = 0.8f;
    private static final String TAG = "AppScrollView";
    public static final int TYPE_DRAG_OVER_BACK = 1;
    public static final int TYPE_FLING_BACK = 0;
    private SpringAnimation anim;
    private List<View> animScaleViews;
    private final DynamicAnimation.OnAnimationEndListener animationEndListener;
    private boolean enableEnd;
    private boolean enableStart;
    private final boolean autoFocus;
    private int flingOverScrollState;
    private float flingVelocityY;
    private boolean isAnimScale;
    private long lastTrackTime;
    private int lastY;
    private int overScrollState;
    private int startDragSide;
    private int startPointId;

    public AppNestedScrollView(Context context) {
        this(context, null);
    }

    public AppNestedScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AppNestedScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.overScrollState = 0;
        this.flingOverScrollState = 0;
        this.enableStart = true;
        this.enableEnd = true;
        this.animationEndListener = (dynamicAnimation, z, f, f2) -> {
            AppNestedScrollView.this.overScrollState = 0;
            AppNestedScrollView.this.flingOverScrollState = 0;
        };
        setOverScrollMode(2);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.AppNestedScrollView, 0, 0);
        this.isAnimScale = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_animScaleNSV, true);
        this.enableStart = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_springEnableStartNSV, true);
        this.enableEnd = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_springEnableEndNSV, true);
        this.autoFocus = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_autoFocusNSV, true);
        obtainStyledAttributes.recycle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setDefaultFocusHighlightEnabled(false);
        }
        if (autoFocus) {
            setFocusable(true);
            setFocusableInTouchMode(true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
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

    @Override
    public boolean onGenericMotionEvent(@NonNull MotionEvent event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (event.getAction() == MotionEvent.ACTION_SCROLL &&
                    event.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
            ) {
                float delta = -event.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                        ViewConfigurationCompat.getScaledVerticalScrollFactor(
                                ViewConfiguration.get(getContext()), getContext()
                        );
                scrollBy(0, Math.round(delta));
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
        if (this.isAnimScale) {
            View childAt = getChildAt(0);
            if (childAt instanceof ViewGroup) {
                setAnimScaleViews(collectChildren((ViewGroup) childAt));
            }
        }
    }

    public void setAnimScale(boolean z) {
        this.isAnimScale = z;
    }

    public void setAnimScaleViews(List<View> list) {
        this.animScaleViews = list;
        post(AppNestedScrollView.this::scaleVerticalChildView);
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

    private boolean hasChild() {
        return getChildCount() != 0;
    }

    @Override
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (!this.isAnimScale || this.animScaleViews == null) {
            return;
        }
        scaleVerticalChildView();
    }

    @Override
    public void onScrollChanged(int i, int i2, int i3, int i4) {
        super.onScrollChanged(i, i2, i3, i4);
        doScrollChanged(i2);
    }

    private void doScrollChanged(int i) {
        if (hasChild()) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = currentTimeMillis - this.lastTrackTime;
            int scrollY = getScrollY();
            int i2 = this.lastY;
            if (scrollY != i2 && j > 0) {
                this.flingVelocityY = ((scrollY - i2) * 1000.0f) / ((float) j);
                this.lastY = scrollY;
                this.lastTrackTime = currentTimeMillis;
            }
            if (this.flingOverScrollState == 3) {
                float translationY = getChildAt(0).getTranslationY();
                boolean isInAbsoluteStart = ViewUtils.isInAbsoluteStart(this, 1);
                boolean isInAbsoluteEnd = ViewUtils.isInAbsoluteEnd(this, 1);
                if ((isInAbsoluteStart && this.enableStart && this.flingVelocityY < 0.0f) || (isInAbsoluteEnd && this.enableEnd && this.flingVelocityY > 0.0f)) {
                    this.flingOverScrollState = 4;
                    createAnimIfNeed(0);
                    this.anim.setStartVelocity(((-this.flingVelocityY)) / 2.0f);
                    this.anim.animateToFinalPosition(0.0f);
                } else if ((isInAbsoluteStart || isInAbsoluteEnd) && translationY != 0.0f) {
                    this.flingOverScrollState = 4;
                    createAnimIfNeed(0);
                    this.anim.animateToFinalPosition(0.0f);
                }
            }
            if (!this.isAnimScale || this.animScaleViews == null) {
                return;
            }
            scaleVerticalChildView();
        }
    }

    public void scaleVerticalChildView() {
        int scrollY = getScrollY();
        int measuredHeight = getMeasuredHeight();
        for (View view : this.animScaleViews) {
            if (view.getVisibility() == View.VISIBLE) {
                int top = view.getTop();
                int bottom = view.getBottom();
                int height = view.getHeight();
                int width = view.getWidth();
                int i = scrollY + measuredHeight;
                if (bottom >= scrollY && top <= i) {
                    float f = ((((bottom <= i || top >= i) ? height : i - top) * 0.19999999f) / height) + 0.8f;
                    view.setPivotX(width / 2.0f);
                    view.setPivotY(0.0f);
                    view.setScaleX(f);
                    view.setScaleY(f);
                }
            }
        }
    }

    private final int mTouchSlop;
    private float mLastMotionX;
    private float mLastMotionY;
    private boolean mIsBeingDragged = false;
    private int mActivePointerId = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!hasChild()) {
            return super.dispatchTouchEvent(ev);
        }

        final int action = ev.getActionMasked();
        View child = getChildAt(0);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);

                mLastMotionX = ev.getX();
                mLastMotionY = ev.getY();

                mIsBeingDragged = false;
                if (this.anim != null && this.anim.isRunning()) {
                    this.anim.cancel();
                }

                if (child.getTranslationY() != 0) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mIsBeingDragged = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == -1) {
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    break;
                }

                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);
                final float yDiff = y - mLastMotionY;
                final float xDiff = x - mLastMotionX;

                if (!mIsBeingDragged) {
                    if (Math.abs(yDiff) > mTouchSlop && Math.abs(yDiff) > Math.abs(xDiff)) {
                        mIsBeingDragged = true;

                        getParent().requestDisallowInterceptTouchEvent(true);

                        mLastMotionY = y - (yDiff > 0 ? -mTouchSlop : mTouchSlop);
                    }
                }

                if (mIsBeingDragged) {
                    boolean canPullDown = ViewUtils.isInAbsoluteStart(this, 1) && this.enableStart;
                    boolean canPullUp = ViewUtils.isInAbsoluteEnd(this, 1) && this.enableEnd;

                    if ((canPullDown && yDiff > 0) || (canPullUp && yDiff < 0)) {
                        this.overScrollState = 1;

                        float currentTranslationY = child.getTranslationY();
                        float newTranslationY = currentTranslationY + (y - mLastMotionY) / 1.5f;

                        child.setTranslationY(newTranslationY);

                        mLastMotionY = y;
                        mLastMotionX = x;
                        return true;
                    }
                }

                // 更新最后的位置
                mLastMotionY = y;
                mLastMotionX = x;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (this.overScrollState == 1) {
                    finishOverScroll();
                } else if (this.flingOverScrollState == 0) {
                    this.flingVelocityY = 0.0f;
                    this.lastY = getScrollY();
                    this.flingOverScrollState = 3;
                    this.lastTrackTime = System.currentTimeMillis();
                    doScrollChanged(this.lastY);
                }
                mIsBeingDragged = false;
                mActivePointerId = -1;
                break;
        }

        // 对于其他所有情况，都沿用父类的默认实现
        return super.dispatchTouchEvent(ev);
    }

    private void finishOverScroll() {
        this.overScrollState = 2;
        createAnimIfNeed(1);
        this.anim.setStartVelocity(0.0f).animateToFinalPosition(0.0f);
    }

    private void createAnimIfNeed(int i) {
        if (this.anim == null) {
            View childAt = getChildAt(0);
            this.anim = new SpringAnimation(childAt, SpringAnimationUtils.FLOAT_PROPERTY_TRANSLATION_Y).setSpring(new SpringForce().setDampingRatio(1.0f).setStiffness(115.0f));
            this.anim.addEndListener(this.animationEndListener);
        }
        SpringForce spring = this.anim.getSpring();
        if (i == 0) {
            spring.setStiffness(115.0f);
        }
        if (i == 1) {
            spring.setStiffness(200.0f);
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