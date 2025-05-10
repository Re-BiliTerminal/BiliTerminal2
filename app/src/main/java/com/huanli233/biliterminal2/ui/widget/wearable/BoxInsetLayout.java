/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huanli233.biliterminal2.ui.widget.wearable;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.UiThread;

import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.data.setting.LocalData;
import com.huanli233.biliterminal2.utils.SystemConfigurationKt;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * BoxInsetLayout is a screen shape-aware ViewGroup that can box its children in the center
 * square of a round screen by applying padding based on the {@code layout_boxedEdges} attribute.
 * The values for this attribute specify the container's edges to be boxed in:
 * {@code left|top|right|bottom} or {@code all}. The {@code layout_boxedEdges} attribute is ignored
 * on a device with a rectangular screen.
 */
@UiThread
public class BoxInsetLayout extends ViewGroup {

    private static final float FACTOR = 0.146447f; // (1 - sqrt(2)/2)/2
    private static final int DEFAULT_CHILD_GRAVITY = Gravity.TOP | Gravity.START;

    private final int mScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    private final int mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

    private boolean mIsRound;
    private Rect mForegroundPadding;
    private Rect mInsets;
    private Drawable mForegroundDrawable;

    private final int mBoxedEdges;
    private final boolean mDoubleTop;

    public BoxInsetLayout(@NonNull Context context) {
        this(context, null);
    }

    public BoxInsetLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoxInsetLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (mForegroundPadding == null) {
            mForegroundPadding = new Rect();
        }
        if (mInsets == null) {
            mInsets = new Rect();
        }

        // Read the layout_boxedEdges attribute for the BoxInsetLayout itself
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BoxInsetLayout, defStyleAttr, 0);
        try {
            mBoxedEdges = a.getInt(R.styleable.BoxInsetLayout_boxedEdges, LayoutParams.BOX_BOTTOM);
            mDoubleTop = a.getBoolean(R.styleable.BoxInsetLayout_doubleTop, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void setForeground(Drawable drawable) {
        super.setForeground(drawable);
        mForegroundDrawable = drawable;
        if (mForegroundPadding == null) {
            mForegroundPadding = new Rect();
        }
        if (mForegroundDrawable != null) {
            drawable.getPadding(mForegroundPadding);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new BoxInsetLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    @SuppressWarnings("deprecation") /* getSystemWindowInsetXXXX */
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (isInEditMode() || LocalData.INSTANCE.getSettings().getUiSettings().getRoundMode())) {
            mIsRound = SystemConfigurationKt.isRound();
            WindowInsets insets = getRootWindowInsets();
            if (insets != null) {
                mInsets.set(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            } else {
                mInsets.setEmpty();
            }
        } else {
            mIsRound = false;
            mInsets.setEmpty();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int childState = 0;

        // Calculate desired inset based on screen dimensions.
        int desiredInset = calculateInset(mScreenWidth, mScreenHeight);

        // Determine the EXTRA padding that the BoxInsetLayout itself will add for boxing.
        // This is applied only on round screens based on the layout's mBoxedEdges attribute.
        int addedLeftPadding = (mIsRound && (mBoxedEdges & LayoutParams.BOX_LEFT) != 0) ? desiredInset : 0;
        int addedTopPadding = (mIsRound && (mBoxedEdges & LayoutParams.BOX_TOP) != 0) ? (mDoubleTop ? desiredInset * 2 : desiredInset) : 0;
        int addedRightPadding = (mIsRound && (mBoxedEdges & LayoutParams.BOX_RIGHT) != 0) ? desiredInset : 0;
        int addedBottomPadding = (mIsRound && (mBoxedEdges & LayoutParams.BOX_BOTTOM) != 0) ? desiredInset : 0;

        // Calculate the total effective padding of the BoxInsetLayout (including original padding, foreground padding, and added boxing padding).
        int totalEffectiveHorizontalPadding = getPaddingLeft() + mForegroundPadding.left + getPaddingRight() + mForegroundPadding.right + addedLeftPadding + addedRightPadding;
        int totalEffectiveVerticalPadding = getPaddingTop() + mForegroundPadding.top + getPaddingBottom() + mForegroundPadding.bottom + addedTopPadding + addedBottomPadding;

        // Measure children. Children are measured within the space available
        // after accounting for the layout's total effective padding and the child's own margins.
        int maxChildWidth = 0;
        int maxChildHeight = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                // Measure child using parent spec minus the total effective padding AND the child's own margins.
                int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        totalEffectiveHorizontalPadding + lp.leftMargin + lp.rightMargin, // Account for layout's total padding + child's margins
                        lp.width);
                int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                        totalEffectiveVerticalPadding + lp.topMargin + lp.bottomMargin, // Account for layout's total padding + child's margins
                        lp.height);

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                maxChildWidth = Math.max(maxChildWidth, child.getMeasuredWidth());
                maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());

                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }

        // The layout's required size is the max child measured size PLUS the layout's total effective padding.
        // Children are measured to fit within the padded area, so the layout's size is that padded area plus its padding.
        int requiredWidth = maxChildWidth + totalEffectiveHorizontalPadding;
        int requiredHeight = maxChildHeight + totalEffectiveVerticalPadding;

        // Check against minimums
        requiredHeight = Math.max(requiredHeight, getSuggestedMinimumHeight());
        requiredWidth = Math.max(requiredWidth, getSuggestedMinimumWidth());
        if (mForegroundDrawable != null) {
            requiredHeight = Math.max(requiredHeight, mForegroundDrawable.getMinimumHeight());
            requiredWidth = Math.max(requiredWidth, mForegroundDrawable.getMinimumWidth());
        }

        // Set BoxInsetLayout's measured dimensions. This size is reported to the parent (ScrollView).
        int measuredWidth = resolveSizeAndState(requiredWidth, widthMeasureSpec, childState);
        int measuredHeight = resolveSizeAndState(requiredHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        // Calculate desired inset based on screen dimensions.
        int desiredInset = calculateInset(mScreenWidth, mScreenHeight);

        // Determine the EXTRA padding that the BoxInsetLayout itself adds for boxing.
        int addedLeftPadding = (mIsRound && (mBoxedEdges & LayoutParams.BOX_LEFT) != 0) ? desiredInset : 0;
        int addedTopPadding = (mIsRound && (mBoxedEdges & LayoutParams.BOX_TOP) != 0) ? desiredInset : 0;
        int addedRightPadding = (mIsRound && (mBoxedEdges & LayoutParams.BOX_RIGHT) != 0) ? desiredInset : 0;
        int addedBottomPadding = (mIsRound && (mBoxedEdges & LayoutParams.BOX_BOTTOM) != 0) ? desiredInset : 0;


        // Parent bounds for child layout. This is the inner content area of the BoxInsetLayout,
        // considering its original padding, foreground padding, AND the added boxing padding.
        final int parentLeft = getPaddingLeft() + mForegroundPadding.left + addedLeftPadding;
        final int parentRight = right - left - getPaddingRight() - mForegroundPadding.right - addedRightPadding;

        final int parentTop = getPaddingTop() + mForegroundPadding.top + addedTopPadding;
        final int parentBottom = bottom - top - getPaddingBottom() - mForegroundPadding.bottom - addedBottomPadding;


        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth(); // Use measured width from onMeasure
                final int height = child.getMeasuredHeight(); // Use measured height from onMeasure

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }

                final int layoutDirection;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    layoutDirection = getLayoutDirection();
                } else {
                    layoutDirection = 0; // Default to LTR
                }
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
                final int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;

                // Position children using their standard margins relative to the padded parent bounds (parentLeft, parentTop etc.).
                // The boxing effect is handled by the parent's padding, so no extra margin calculation needed here.

                if (lp.width == LayoutParams.MATCH_PARENT) {
                    childLeft = parentLeft + lp.leftMargin;
                } else {
                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            // Center within the padded horizontal bounds
                            childLeft = parentLeft + (parentRight - parentLeft - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            // Position from the right edge of the padded bounds
                            childLeft = parentRight - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            // Position from the left edge of the padded bounds
                            childLeft = parentLeft + lp.leftMargin;
                    }
                }

                if (lp.height == LayoutParams.MATCH_PARENT) {
                    childTop = parentTop + lp.topMargin;
                } else {
                    switch (verticalGravity) {
                        case Gravity.CENTER_VERTICAL:
                            // Center within the padded vertical bounds
                            childTop = parentTop + (parentBottom - parentTop - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            // Position from the bottom edge of the padded bounds
                            childTop = parentBottom - height - lp.bottomMargin;
                            break;
                        case Gravity.TOP:
                        default:
                            // Position from the top edge of the padded bounds
                            childTop = parentTop + lp.topMargin;
                    }
                }
                // Layout the child
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    // Keep calculateInset using screen dimensions for consistency
    public static int calculateInset(int width, int height) {
        return (int) (FACTOR * Math.max(width, height));
    }

    // REMOVE the calculateChildLeftMargin, calculateChildRightMargin,
    // calculateChildTopMargin, calculateChildBottomMargin methods as they are no longer needed
    // for adding the boxing inset.

    /**
     * Per-child layout information for layouts that support margins, gravity and (deprecated for boxing effect) boxedEdges.
     * The primary boxing effect is now controlled by the parent BoxInsetLayout's layout_boxedEdges attribute.
     * See {@link R.styleable#BoxInsetLayout BoxInsetLayout Layout Attributes} for a list
     * of all child view attributes that this class supports.
     *
     * {@link com.huanli233.biliterminal2.R.attr#layout_boxedEdges} (on parent)
     */
    public static class LayoutParams extends FrameLayout.LayoutParams {

        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @IntDef({BOX_NONE, BOX_LEFT, BOX_TOP, BOX_RIGHT, BOX_BOTTOM, BOX_ALL})
        @Retention(RetentionPolicy.SOURCE)
        public @interface BoxedEdges {}

        public static final int BOX_NONE = 0x0;
        public static final int BOX_LEFT = 0x01;
        public static final int BOX_TOP = 0x02;
        public static final int BOX_RIGHT = 0x04;
        public static final int BOX_BOTTOM = 0x08;
        public static final int BOX_ALL = 0x0F;

        /**
         * This attribute on LayoutParams is read for compatibility but does NOT
         * control the primary boxing padding effect, which is now on the parent BoxInsetLayout.
         */
        @BoxedEdges
        public int boxedEdges;

        @SuppressWarnings("ResourceType")
        public LayoutParams(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BoxInsetLayout,
                    0, 0);
            // Read the boxedEdges attribute for the child, but it's not used for boxing padding now.
            // Keeping it for potential future use or compatibility.
            int boxedEdgesResourceKey = R.styleable.BoxInsetLayout_Layout_layout_boxedEdges;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (!a.hasValueOrEmpty(R.styleable.BoxInsetLayout_Layout_layout_boxedEdges)){
                    boxedEdgesResourceKey = R.styleable.BoxInsetLayout_boxedEdges;
                }
            }
            // Store the read value, but it's not used in the measure/layout logic for padding.
            boxedEdges = a.getInt(boxedEdgesResourceKey, BOX_BOTTOM); // Default value
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        // This constructor might be less relevant now that boxing is parent-controlled, but keep for compatibility.
        public LayoutParams(int width, int height, int gravity, @BoxedEdges int boxed) {
            super(width, height, gravity);
            this.boxedEdges = boxed; // Store the value
        }


        public LayoutParams(ViewGroup.@NonNull LayoutParams source) {
            super(source);
        }


        public LayoutParams(ViewGroup.@NonNull MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public LayoutParams(FrameLayout.@NonNull LayoutParams source) {
            super(source);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public LayoutParams(@NonNull LayoutParams source) {
            super(source);
            this.boxedEdges = source.boxedEdges;
            this.gravity = source.gravity;
        }
    }
}