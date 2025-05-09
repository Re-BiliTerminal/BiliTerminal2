/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huanli233.biliterminal2.ui.widget.pager;

import android.animation.Animator;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * Convenience class for listening for Animator events that implements the AnimatorListener
 * interface and allows extending only methods that are necessary.
 *
 */
@RestrictTo(Scope.LIBRARY)
public class SimpleAnimatorListener implements Animator.AnimatorListener {

    private boolean mWasCanceled;

    @Override
    public void onAnimationCancel(@NonNull Animator animator) {
        mWasCanceled = true;
    }

    @Override
    public void onAnimationEnd(@NonNull Animator animator) {
        if (!mWasCanceled) {
            onAnimationComplete(animator);
        }
    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animator) {}

    @Override
    public void onAnimationStart(@NonNull Animator animator) {
        mWasCanceled = false;
    }

    /**
     * Called when the animation finishes. Not called if the animation was canceled.
     */
    public void onAnimationComplete(Animator animator) {}

    /**
     * Provides information if the animation was cancelled.
     *
     * @return True if animation was cancelled.
     */
    public boolean wasCanceled() {
        return mWasCanceled;
    }
}