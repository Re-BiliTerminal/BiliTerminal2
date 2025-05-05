package com.ethanhua.skeleton

/**
 * Created by ethanhua on 2017/7/29.
 */
interface SkeletonScreen {
    /**
     * show skeleton screen
     * @return current skeleton screen
     */
    fun show(): SkeletonScreen
    fun hide()
}