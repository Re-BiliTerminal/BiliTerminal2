package com.huanli233.biliterminal2.ui.utils;

import android.os.Build;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

public class PressAnimHelper {
    private static final float ALPHA_SMALL = 0.8f;
    public static final float RESET_ALPHA = 1.0f;
    public static final float RESET_SCALE = 1.0f;
    private static final float SCALE_SMALL = 0.95f;
    private final boolean isAlpha;
    private final boolean isScale;
    private Interpolator pressInterpolator;
    private Interpolator releaseInterpolator;
    private final View view;
    private float smallScale = SCALE_SMALL;
    private float smallAlpha = 0.8f;

    public PressAnimHelper(View view, boolean z, boolean z2) {
        this.view = view;
        this.isAlpha = z;
        this.isScale = z2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.pressInterpolator = new PathInterpolator(0.0f, 0.0f, 0.58f, 1.0f);
            this.releaseInterpolator = new PathInterpolator(0.42f, 0.0f, 1.0f, 1.0f);
        }
    }

    public void setSmallScale(float f) {
        this.smallScale = f;
    }

    public void setSmallAlpha(float f) {
        this.smallAlpha = f;
    }

    public void press() {
        ViewPropertyAnimator duration = this.view.animate().setDuration(150L);
        if (this.isScale) {
            duration.scaleX(this.smallScale).scaleY(this.smallScale);
        }
        if (this.isAlpha) {
            duration.alpha(this.smallAlpha);
        }
        if (pressInterpolator != null) {
            duration.setInterpolator(this.pressInterpolator);
        }
        duration.start();
    }

    public void release() {
        ViewPropertyAnimator duration = this.view.animate().setDuration(150L);
        if (this.isScale) {
            duration.scaleX(1.0f).scaleY(1.0f);
        }
        if (this.isAlpha) {
            duration.alpha(1.0f);
        }
        if (releaseInterpolator != null) {
            duration.setInterpolator(this.releaseInterpolator);
        }
        duration.start();
    }
}
