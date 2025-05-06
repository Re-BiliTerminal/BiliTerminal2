package com.huanli233.biliterminal2.ui.utils.view;

import android.view.View;

import androidx.dynamicanimation.animation.FloatPropertyCompat;

public class SpringAnimationUtils {
    public static final FloatPropertyCompat<View> FLOAT_PROPERTY_TRANSLATION_Y = new FloatPropertyCompat<>("translationY") {
        @Override
        public float getValue(View view) {
            return view.getTranslationY();
        }

        @Override
        public void setValue(View view, float f) {
            view.setTranslationY(f);
        }
    };
}