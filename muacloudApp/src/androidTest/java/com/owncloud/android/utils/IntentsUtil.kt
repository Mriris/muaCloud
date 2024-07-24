

package com.owncloud.android.utils

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent

fun mockIntent(
    extras: Pair<String, String>,
    resultCode: Int = Activity.RESULT_OK,
    action: String
) {
    val result = Intent()
    result.putExtra(extras.first, extras.second)
    val intentResult = Instrumentation.ActivityResult(resultCode, result)
    intending(hasAction(action)).respondWith(intentResult)
}

@JvmName("mockIntentNoExtras")
fun mockIntent(
    resultCode: Int = Activity.RESULT_OK,
    action: String
) {
    val result = Intent()
    val intentResult = Instrumentation.ActivityResult(resultCode, result)
    intending(hasAction(action)).respondWith(intentResult)
}

fun mockIntentToComponent(
    resultCode: Int = Activity.RESULT_OK,
    packageName: String
) {
    val result = Intent()
    val intentResult = Instrumentation.ActivityResult(resultCode, result)
    intending(hasComponent(packageName)).respondWith(intentResult)
}
