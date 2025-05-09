package com.huanli233.biliterminal2.ui.widget.scalablecontainer;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
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
    private int flingOverScrollState;
    private float flingVelocityY;
    private boolean isAnimScale;
    private long lastTrackTime;
    private int lastY;
    private int overScrollState;
    private int startDragSide;
    private int startPointId;

    public AppScrollView(Context context) {
        this(context, null);
    }

    public AppScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AppScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.overScrollState = 0;
        this.flingOverScrollState = 0;
        this.enableStart = true;
        this.enableEnd = true;
        this.animationEndListener = (dynamicAnimation, z, f, f2) -> {
            AppScrollView.this.overScrollState = 0;
            AppScrollView.this.flingOverScrollState = 0;
        };
        setOverScrollMode(2);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.AppScrollView, 0, 0);
        this.isAnimScale = obtainStyledAttributes.getBoolean(R.styleable.AppScrollView_animScaleSV, true);
        this.enableStart = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_springEnableStartNSV, true);
        this.enableEnd = obtainStyledAttributes.getBoolean(R.styleable.AppNestedScrollView_springEnableEndNSV, true);
        obtainStyledAttributes.recycle();
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
        post(AppScrollView.this::scaleVerticalChildView);
    }

    private boolean hasChild() {
        return getChildCount() != 0;
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

    private void doScrollChanged() {
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (hasChild()) {
            View childAt = getChildAt(0);
            int action = motionEvent.getAction();
            float translationY = childAt.getTranslationY();
            SpringAnimation springAnimation = this.anim;
            if (springAnimation != null && springAnimation.isRunning()) {
                this.anim.cancel();
            }
            if (action != 1) {
                if (action == 2) {
                    if (motionEvent.getHistorySize() != 0) {
                        float y = motionEvent.getY(0) - motionEvent.getHistoricalY(0, 0);
                        if (Math.abs(y) >= Math.abs(motionEvent.getX(0) - motionEvent.getHistoricalX(0, 0))) {
                            int i = y > 0.0f ? 1 : 2;
                            boolean z = ViewUtils.isInAbsoluteStart(this, 1) && this.enableStart;
                            boolean z2 = ViewUtils.isInAbsoluteEnd(this, 1) && this.enableEnd;
                            if (this.overScrollState == 0) {
                                if ((i == 1 && z) || (i == 2 && z2)) {
                                    this.startPointId = motionEvent.getPointerId(0);
                                    this.startDragSide = i;
                                    this.overScrollState = 1;
                                }
                            }
                            if (this.overScrollState == 1) {
                                if (this.startPointId != motionEvent.getPointerId(0)) {
                                    finishOverScroll();
                                } else {
                                    float f = translationY + (y / 1.5f);
                                    int i2 = this.startDragSide;
                                    if (i != i2 && ((i2 == 1 && f <= 0.0f) || (this.startDragSide == 2 && f > 0.0f))) {
                                        this.overScrollState = 0;
                                    } else {
                                        ViewParent parent = getParent();
                                        if (parent != null) {
                                            parent.requestDisallowInterceptTouchEvent(true);
                                        }
                                        childAt.setTranslationY(f);
                                    }
                                }
                            }
                        }
                    }
                } else if (action == 3) {
                    int i3 = this.overScrollState;
                    if (i3 != 2) {
                        if (i3 == 1) {
                            finishOverScroll();
                        } else if (this.flingOverScrollState == 0) {
                            this.flingVelocityY = 0.0f;
                            this.lastY = getScrollY();
                            this.flingOverScrollState = 3;
                            this.lastTrackTime = System.currentTimeMillis();
                            doScrollChanged();
                        }
                    }
                }
                return super.dispatchTouchEvent(motionEvent);
            }
            int i3 = this.overScrollState;
            if (i3 != 2) {
                if (i3 == 1) {
                    finishOverScroll();
                } else if (this.flingOverScrollState == 0) {
                    this.flingVelocityY = 0.0f;
                    this.lastY = getScrollY();
                    this.flingOverScrollState = 3;
                    this.lastTrackTime = System.currentTimeMillis();
                    doScrollChanged();
                }
            }
            return super.dispatchTouchEvent(motionEvent);
        }
        return super.dispatchTouchEvent(motionEvent);
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