package com.huanli233.biliterminal2.ui.dialog.fullscreen;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.huanli233.biliterminal2.R;

public class ButtonBarLayout extends LinearLayout {
    /** Amount of the second button to "peek" above the fold when stacked. */
    private static final int PEEK_BUTTON_DP = 16;

    /** Whether the current configuration allows stacking. */
    private boolean mAllowStacking;

    /** Whether the button bar is currently stacked. */
    private boolean mStacked;

    private int mLastWidthSize = -1;

    public ButtonBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ButtonBarLayout);
        ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.ButtonBarLayout,
                attrs, ta, 0, 0);
        mAllowStacking = ta.getBoolean(R.styleable.ButtonBarLayout_allowStacking, true);
        ta.recycle();

        // Stacking may have already been set implicitly via orientation="vertical", in which
        // case we'll need to validate it against allowStacking and re-apply explicitly.
        if (getOrientation() == LinearLayout.VERTICAL) {
            setStacked(mAllowStacking);
        }
    }

    public void setAllowStacking(boolean allowStacking) {
        if (mAllowStacking != allowStacking) {
            mAllowStacking = allowStacking;
            if (!mAllowStacking && isStacked()) {
                setStacked(false);
            }
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int originalHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        boolean stack = isStacked();

        // Step 1: If allowed, check if we should unstack based on wider space
        // If we are currently stacked and the width has increased significantly,
        // attempt to unstack horizontally.
        if (mAllowStacking && widthSize > mLastWidthSize && stack) {
//            setStacked(false);
//            stack = false;
        }
        mLastWidthSize = widthSize; // Update last width size

        // Step 2: If we are tentatively horizontal (or just unstacked) AND stacking is allowed,
        // perform an initial measure pass to see if horizontal space is sufficient.
        // This pass uses AT_MOST width to check if we get MEASURED_STATE_TOO_SMALL.
        // We use the original height spec here as height measurement isn't the goal of THIS pass.
        if (!stack && mAllowStacking) {
            final int initialWidthMeasureSpec = (widthMode == MeasureSpec.EXACTLY)
                    ? MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST)
                    : widthMeasureSpec;
            super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec); // Use original heightSpec

            // Check the result of the horizontal measure: did it report being too small?
            final int measuredWidth = getMeasuredWidthAndState();
            final int measuredWidthState = measuredWidth & View.MEASURED_STATE_MASK;
            if (measuredWidthState == View.MEASURED_STATE_TOO_SMALL) {
                // Yes, horizontal space is insufficient, we need to stack.
                stack = true;
            }
        }

        // Step 3: Update the actual stack state if it needs to change.
        // This calls setOrientation, setGravity, handles the spacer, and reverses child order if needed.
        if (stack != isStacked()) {
            setStacked(stack);
        }

        // Step 4: Perform the FINAL, authoritative measurement pass.
        // This is the measurement result that will be reported to the parent (like ScrollView).
        // If we are currently stacked AND the original height spec was NOT UNSPECIFIED,
        // we need to perform this measurement with UNSPECIFIED height.
        // This allows LinearLayout to measure its full required height based on its children,
        // which is necessary for ScrollView to calculate the scroll range.
        final int finalHeightMeasureSpec;
        if (isStacked() && originalHeightMode != MeasureSpec.UNSPECIFIED) {
            finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        } else {
            // Otherwise, use the original height spec for the final measurement.
            finalHeightMeasureSpec = heightMeasureSpec;
        }

        // Perform the final measurement using the original width spec and the potentially adjusted height spec.
        super.onMeasure(widthMeasureSpec, finalHeightMeasureSpec);

        // Step 5: Calculate the minimum height required.
        // This calculation uses the measured heights of the children from the super.onMeasure call above.
        int minHeight = 0;
        final int firstVisible = getNextVisibleChildIndex(0);
        if (firstVisible >= 0) {
            final View firstButton = getChildAt(firstVisible);
            final LayoutParams firstParams = (LayoutParams) firstButton.getLayoutParams();
            minHeight += getPaddingTop() + firstButton.getMeasuredHeight()
                    + firstParams.topMargin + firstParams.bottomMargin;
            if (isStacked()) {
                final int secondVisible = getNextVisibleChildIndex(firstVisible + 1);
                if (secondVisible >= 0) {
                    // Add the height of the first button + its margins/padding,
                    // plus the top padding of the second button + the PEEK_BUTTON_DP amount.
                    minHeight += getChildAt(secondVisible).getPaddingTop()
                            + (int) (PEEK_BUTTON_DP * getResources().getDisplayMetrics().density);
                }
            } else {
                minHeight += getPaddingBottom(); // In horizontal mode, just add bottom padding
            }
        }

        // Step 6: Set the minimum height and potentially re-measure if needed.
        // We only need to re-measure *here* if the minimum height is greater than the
        // currently measured height AND the original height spec was UNSPECIFIED.
        // If the original spec was constrained, the final super.onMeasure in Step 4
        // already reported the full necessary height for ScrollView, and setting minHeight
        // here won't make it larger unless minHeight > full calculated height (unlikely).
        if (ViewCompat.getMinimumHeight(this) != minHeight) {
            setMinimumHeight(minHeight);

            // Re-measure immediately to fill excess space IF original height was UNSPECIFIED.
            // If original height was UNSPECIFIED, the height measured in Step 4 was based
            // solely on children's heights. setMinimumHeight might increase the reported height
            // if minHeight is larger, so a re-measure with the original (UNSPECIFIED) spec is needed.
            if (originalHeightMode == MeasureSpec.UNSPECIFIED) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
            // If originalHeightMode was AT_MOST/EXACTLY, the final super.onMeasure (Step 4)
            // used UNSPECIFIED mode (if stacked) to report full height. Setting minHeight won't
            // make it larger than this full height unless minHeight is truly massive.
            // No extra re-measure needed in this specific conditional block if original spec was constrained.
        }
    }

    private int getNextVisibleChildIndex(int index) {
        for (int i = index, count = getChildCount(); i < count; i++) {
            if (getChildAt(i).getVisibility() == View.VISIBLE) {
                return i;
            }
        }
        return -1;
    }

    private void setStacked(boolean stacked) {
        if (mStacked != stacked && (!stacked || mAllowStacking)) {
            mStacked = stacked;

            setOrientation(stacked ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            setGravity(stacked ? Gravity.END : Gravity.BOTTOM);

            final View spacer = findViewById(R.id.spacer);
            if (spacer != null) {
                spacer.setVisibility(stacked ? View.GONE : View.INVISIBLE);
            }

            // Reverse the child order. This is specific to the Material button
            // bar's layout XML and will probably not generalize.
            final int childCount = getChildCount();
            for (int i = childCount - 2; i >= 0; i--) {
                bringChildToFront(getChildAt(i));
            }
        }
    }

    private boolean isStacked() {
        return mStacked;
    }
}
