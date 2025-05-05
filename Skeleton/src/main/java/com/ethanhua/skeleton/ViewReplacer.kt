package com.ethanhua.skeleton

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by ethanhua on 2017/8/2.
 */
class ViewReplacer(private val sourceView: View) {
    var targetView: View? = null
        private set
    private var mTargetViewResID = -1
    var currentView: View?
        private set
    private var mSourceParentView: ViewGroup? = null
    private val mSourceViewLayoutParams: ViewGroup.LayoutParams = sourceView.layoutParams
    private var mSourceViewIndexInParent = 0
    private val mSourceViewId: Int
    fun replace(targetViewResID: Int) {
        if (mTargetViewResID == targetViewResID) {
            return
        }
        if (init()) {
            mTargetViewResID = targetViewResID
            replace(LayoutInflater.from(sourceView.context).inflate(mTargetViewResID, mSourceParentView, false))
        }
    }

    fun replace(targetView: View) {
        if (currentView == targetView) {
            return
        }
        if (targetView.parent != null) {
            (targetView.parent as ViewGroup).removeView(targetView)
        }
        if (init()) {
            this.targetView = targetView
            mSourceParentView!!.removeView(currentView)
            targetView.id = mSourceViewId
            mSourceParentView!!.addView(this.targetView, mSourceViewIndexInParent, mSourceViewLayoutParams)
            currentView = this.targetView
        }
    }

    fun restore() {
        mSourceParentView?.let {
            it.removeView(currentView)
            it.addView(sourceView, mSourceViewIndexInParent, mSourceViewLayoutParams)
            currentView = sourceView
            targetView = null
            mTargetViewResID = -1
        }
    }

    private fun init(): Boolean {
        if (mSourceParentView == null) {
            mSourceParentView = (sourceView.parent as? ViewGroup)?.also {
                for (index in 0 until it.childCount) {
                    if (sourceView == it.getChildAt(index)) {
                        mSourceViewIndexInParent = index
                        break
                    }
                }
            } ?: run {
                Log.e(TAG, "the source view have not attach to any view")
                return false
            }
        }
        return true
    }

    companion object {
        private val TAG = ViewReplacer::class.java.name
    }

    init {
        currentView = sourceView
        mSourceViewId = sourceView.id
    }
}