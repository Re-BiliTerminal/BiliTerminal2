package com.huanli233.bilizepam.ui.utils.view

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

object ViewHierarchyPrinter {

    private const val INDENT_STEP = "  "

    fun printViewHierarchy(view: View?, tag: String = "ViewHierarchy") {
        if (view == null) {
            android.util.Log.e(tag, "Root view is null. Cannot print hierarchy.")
            return
        }
        android.util.Log.d(tag, "--- View Hierarchy Start ---")
        printView(view, 0, tag)
        android.util.Log.d(tag, "--- View Hierarchy End ---")
    }

    private fun printView(view: View, indentLevel: Int, tag: String) {
        val indent = INDENT_STEP.repeat(indentLevel)
        val stringBuilder = StringBuilder()

        stringBuilder.append(indent)
            .append("-> ${view.javaClass.simpleName}")

        try {
            val id = view.resources.getResourceEntryName(view.id)
            stringBuilder.append(" id=\"@id/$id\"")
        } catch (_: Exception) {
            if (view.id != View.NO_ID) {
                stringBuilder.append(" id=\"${view.id}\"")
            }
        }

        when (view) {
            is Button -> {
                if (view.text.isNotBlank()) {
                    stringBuilder.append(" text=\"${view.text}\"")
                }
            }
            is EditText -> {
                if (view.text.isNotBlank()) {
                    stringBuilder.append(" text=\"${view.text}\"")
                }
            }
            is TextView -> {
                if (view.text.isNotBlank()) {
                    stringBuilder.append(" text=\"${view.text}\"")
                }
            }
        }

        val visibility = when (view.visibility) {
            View.VISIBLE -> "VISIBLE"
            View.INVISIBLE -> "INVISIBLE"
            View.GONE -> "GONE"
            else -> "UNKNOWN"
        }
        stringBuilder.append(" visibility=\"$visibility\"")

        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        stringBuilder.append(" [${rect.left},${rect.top}-${rect.right},${rect.bottom}]")
        stringBuilder.append(" (${view.width}x${view.height})")

        // Add LayoutParams information
        view.layoutParams?.let { params ->
            stringBuilder.append(" layout_width=\"${
                when (params.width) {
                    ViewGroup.LayoutParams.MATCH_PARENT -> "match_parent"
                    ViewGroup.LayoutParams.WRAP_CONTENT -> "wrap_content"
                    else -> params.width.toString() + "px"
                }
            }\"")
            stringBuilder.append(" layout_height=\"${
                when (params.height) {
                    ViewGroup.LayoutParams.MATCH_PARENT -> "match_parent"
                    ViewGroup.LayoutParams.WRAP_CONTENT -> "wrap_content"
                    else -> params.height.toString() + "px"
                }
            }\"")

            if (params is ViewGroup.MarginLayoutParams) {
                stringBuilder.append(" margins=\"L:${params.leftMargin} T:${params.topMargin} R:${params.rightMargin} B:${params.bottomMargin}\"")
            }

            // Add ConstraintLayout.LayoutParams specific information
            if (params is ConstraintLayout.LayoutParams) {
                val clParams = params
                stringBuilder.append(" constraints={")

                if (clParams.leftToLeft != View.NO_ID) stringBuilder.append(" leftToLeft=\"${getResourceName(view, clParams.leftToLeft)}\"")
                if (clParams.leftToRight != View.NO_ID) stringBuilder.append(" leftToRight=\"${getResourceName(view, clParams.leftToRight)}\"")
                if (clParams.topToTop != View.NO_ID) stringBuilder.append(" topToTop=\"${getResourceName(view, clParams.topToTop)}\"")
                if (clParams.topToBottom != View.NO_ID) stringBuilder.append(" topToBottom=\"${getResourceName(view, clParams.topToBottom)}\"")
                if (clParams.rightToLeft != View.NO_ID) stringBuilder.append(" rightToLeft=\"${getResourceName(view, clParams.rightToLeft)}\"")
                if (clParams.rightToRight != View.NO_ID) stringBuilder.append(" rightToRight=\"${getResourceName(view, clParams.rightToRight)}\"")
                if (clParams.bottomToTop != View.NO_ID) stringBuilder.append(" bottomToTop=\"${getResourceName(view, clParams.bottomToTop)}\"")
                if (clParams.bottomToBottom != View.NO_ID) stringBuilder.append(" bottomToBottom=\"${getResourceName(view, clParams.bottomToBottom)}\"")
                if (clParams.baselineToBaseline != View.NO_ID) stringBuilder.append(" baselineToBaseline=\"${getResourceName(view, clParams.baselineToBaseline)}\"")

                if (clParams.horizontalBias != 0.5f) stringBuilder.append(" horizontalBias=\"${clParams.horizontalBias}\"")
                if (clParams.verticalBias != 0.5f) stringBuilder.append(" verticalBias=\"${clParams.verticalBias}\"")

                if (clParams.circleConstraint != View.NO_ID) {
                    stringBuilder.append(" circleConstraint=\"${getResourceName(view, clParams.circleConstraint)}\"")
                    stringBuilder.append(" circleAngle=\"${clParams.circleAngle}\"")
                    stringBuilder.append(" circleRadius=\"${clParams.circleRadius}\"")
                }

                if (!clParams.dimensionRatio.isNullOrBlank()) stringBuilder.append(" dimensionRatio=\"${clParams.dimensionRatio}\"")

                stringBuilder.append("}")
            }
        }

        if (view.isClickable) {
            stringBuilder.append(" clickable=\"true\"")
        }

        if (view.isLongClickable) {
            stringBuilder.append(" longClickable=\"true\"")
        }

        if (view.isFocusable) {
            stringBuilder.append(" focusable=\"true\"")
        }
        if (view.hasFocus()) {
            stringBuilder.append(" focused=\"true\"")
        }

        if (!view.isEnabled) {
            stringBuilder.append(" enabled=\"false\"")
        }

        if (view is ImageView && !view.contentDescription.isNullOrBlank()) {
            stringBuilder.append(" contentDescription=\"${view.contentDescription}\"")
        }

        if (view.tag != null) {
            stringBuilder.append(" tag=\"${view.tag}\"")
        }

        android.util.Log.d(tag, stringBuilder.toString())

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                printView(child, indentLevel + 1, tag)
            }
        }
    }

    /**
     * Helper function to get resource name from ID.
     */
    private fun getResourceName(view: View, resId: Int): String {
        return try {
            if (resId == ConstraintLayout.LayoutParams.PARENT_ID) {
                "parent"
            } else {
                view.resources.getResourceEntryName(resId)
            }
        } catch (_: Exception) {
            resId.toString()
        }
    }
}