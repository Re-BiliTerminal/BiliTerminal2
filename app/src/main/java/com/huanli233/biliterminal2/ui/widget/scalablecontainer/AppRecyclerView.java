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
import androidx.recyclerview.widget.RecyclerView;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.ui.utils.ViewUtils;

import java.util.HashSet;
import java.util.Set;

public class AppRecyclerView extends RecyclerView {
    public static final long FLING_CALCULATE_INTERVAL = 30;
    public static final float OVER_TRANSLATION_RATIO = 2.0f;
    private static final FloatPropertyCompat<AppRecyclerView> PROPERTY_OVER_TRANSLATION_Y = new FloatPropertyCompat<>("overTranslationY") {
        @Override
        public float getValue(AppRecyclerView appRecyclerView) {
            return appRecyclerView.getOverTranslationY();
        }

        @Override
        public void setValue(AppRecyclerView appRecyclerView, float f) {
            appRecyclerView.setOverTranslationY((int) f);
        }
    };
    private final Set<View> animChildren;
    private final SpringAnimation animation;
    private final boolean enableEnd;
    private final boolean enableStart;
    private int flingVelocityY;
    private boolean forbidEdgeDrag;
    private boolean isAvailable = true;
    private long lastScrollTime;
    private int lastScrollY;
    private final int maxVelocityY;
    private float overTranslationY;
    private int scrollState;
    private int startPointId;
    private int totalScrollY;

    public AppRecyclerView(Context context) {
        this(context, null);
    }

    public AppRecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AppRecyclerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.AppRecyclerView, 0, 0);
        this.enableStart = obtainStyledAttributes.getBoolean(R.styleable.AppRecyclerView_springEnableStart, true);
        this.enableEnd = obtainStyledAttributes.getBoolean(R.styleable.AppRecyclerView_springEnableEnd, true);
        obtainStyledAttributes.recycle();
        this.animChildren = new HashSet<>();
        this.animation = new SpringAnimation(this, PROPERTY_OVER_TRANSLATION_Y).setSpring(new SpringForce().setDampingRatio(1.0f).setStiffness(150.0f));
        this.maxVelocityY = ViewConfiguration.get(context).getScaledMaximumFlingVelocity() / 2;
    }

    public float getOverTranslationY() {
        return this.overTranslationY;
    }

    public void setAvailable(boolean z) {
        this.isAvailable = z;
    }

    public void setForbidEdgeDrag(boolean z) {
        this.forbidEdgeDrag = z;
    }

    @Override
    public void onScrolled(int i, int i2) {
        if (!this.isAvailable) {
            super.onScrolled(i, i2);
            return;
        }
        this.totalScrollY += i2;
        boolean isInAbsoluteStart = ViewUtils.isInAbsoluteStart(this, 1);
        boolean isInAbsoluteEnd = ViewUtils.isInAbsoluteEnd(this, 1);
        if (!isInAbsoluteStart && !isInAbsoluteEnd && isOverTranslationNeedReset()) {
            stopAnim();
            setOverTranslationY(0.0f);
            this.flingVelocityY = 0;
            this.lastScrollY = this.totalScrollY;
            this.lastScrollTime = SystemClock.elapsedRealtime();
            return;
        }
        int i3 = this.totalScrollY - this.lastScrollY;
        if (Math.abs(i3) < 30) {
            return;
        }
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long j = elapsedRealtime - this.lastScrollTime;
        if (j <= 0) {
            return;
        }
        this.flingVelocityY = (int) ((i3 * 1000) / j);
        this.lastScrollY = this.totalScrollY;
        this.lastScrollTime = elapsedRealtime;
    }

    private boolean isOverTranslationNeedReset() {
        return Math.abs(this.overTranslationY) > 30.0f;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.isAvailable) {
            if (this.forbidEdgeDrag) {
                return super.dispatchTouchEvent(motionEvent);
            }
            int action = motionEvent.getAction();
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager == null) {
                return super.dispatchTouchEvent(motionEvent);
            }
            boolean canScrollVertically = layoutManager.canScrollVertically();
            int childCount = getChildCount();
            if (!canScrollVertically || childCount == 0) {
                return super.dispatchTouchEvent(motionEvent);
            }
            int i = 0;
            boolean z = ViewUtils.isInAbsoluteStart(this, 1) && this.enableStart;
            boolean z2 = ViewUtils.isInAbsoluteEnd(this, 1) && this.enableEnd;
            if (action != 1) {
                if (action == 2) {
                    if (motionEvent.getHistorySize() != 0) {
                        float y = motionEvent.getY(0) - motionEvent.getHistoricalY(0, 0);
                        if (Math.abs(y) >= Math.abs(motionEvent.getX(0) - motionEvent.getHistoricalX(0, 0))) {
                            int pointerId = motionEvent.getPointerId(0);
                            float f = this.overTranslationY;
                            if (f != 0.0f) {
                                if (pointerId == this.startPointId) {
                                    int i2 = (int) ((y / 2.0f) + f);
                                    if (i2 * f >= 0.0f) {
                                        i = i2;
                                    }
                                }
                                stopAnim();
                                setOverTranslationY(i);
                            } else if ((y > 0.0f && z) || (y < 0.0f && z2)) {
                                this.startPointId = pointerId;
                                setOverTranslationY((int) (y / 2.0f));
                                ViewParent parent = getParent();
                                if (parent != null) {
                                    parent.requestDisallowInterceptTouchEvent(true);
                                }
                            }
                        }
                    }
                } else if (action == 3) {
                    finishOverScroll();
                }
                return super.dispatchTouchEvent(motionEvent);
            } else {
                finishOverScroll();
            }
            finishOverScroll();
            return super.dispatchTouchEvent(motionEvent);
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private void stopAnim() {
        if (this.animation.isRunning()) {
            this.animation.cancel();
        }
    }

    public void setOverTranslationY(float f) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.animChildren.add(getChildAt(i));
        }
        for (View view : this.animChildren) {
            view.setTranslationY(f);
        }
        this.overTranslationY = f;
        if (f == 0.0f) {
            this.animChildren.clear();
        }
    }

    private void finishOverScroll() {
        this.animation.animateToFinalPosition(0.0f);
    }

    @Override
    public void onScrollStateChanged(int i) {
        if (!this.isAvailable) {
            super.onScrollStateChanged(i);
        } else if (this.scrollState != 2 || i != 0 || isOverTranslationNeedReset()) {
            this.scrollState = i;
        } else {
            this.scrollState = i;
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager != null && layoutManager.canScrollVertically()) {
                boolean z = true;
                boolean z2 = ViewUtils.isInAbsoluteStart(this, 1) && this.enableStart;
                boolean z3 = ViewUtils.isInAbsoluteEnd(this, 1) && this.enableEnd;
                if ((!z2 || this.flingVelocityY >= 0) && (!z3 || this.flingVelocityY <= 0)) {
                    z = false;
                }
                if (z) {
                    float min = Math.min(Math.abs(this.flingVelocityY), this.maxVelocityY);
                    if (min < 0.0f) {
                        return;
                    }
                    if (this.flingVelocityY >= 0) {
                        min = -min;
                    }
                    this.animation.setStartVelocity(min).animateToFinalPosition(0.0f);
                }
            }
        }
    }
}
