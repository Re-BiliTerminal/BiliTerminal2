package com.huanli233.bilizepam.ui.utils.hikage.extension

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat

/**
 * Starts the Activity [A], in a more concise way, while still allowing to configure the [Intent] in
 * the optional [configIntent] lambda.
 */
inline fun <reified A : Activity> Context.start(activityOptions: Bundle?, configIntent: Intent.() -> Unit = {}) {
    ContextCompat.startActivity(this, Intent(this, A::class.java).apply(configIntent), activityOptions)
}

/**
 * Starts an Activity that supports the passed [action], in a more concise way,
 * while still allowing to configure the [Intent] in the optional [configIntent] lambda.
 *
 * If there's no matching [Activity], the underlying platform API will throw an
 * [ActivityNotFoundException].
 *
 * If there is more than one matching [Activity], the Android system may show an activity chooser to
 * the user.
 */
@Throws(ActivityNotFoundException::class)
inline fun Context.startActivity(action: String, activityOptions: Bundle?, configIntent: Intent.() -> Unit = {}) {
    ContextCompat.startActivity(this, Intent(action).apply(configIntent), activityOptions)
}