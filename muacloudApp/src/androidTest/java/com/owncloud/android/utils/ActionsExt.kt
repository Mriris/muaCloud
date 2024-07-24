

package com.owncloud.android.utils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId

fun Int.typeText(text: String) {
    onView(withId(this)).perform(scrollTo(), ViewActions.typeText(text))
}

fun Int.replaceText(text: String) {
    onView(withId(this)).perform(scrollTo(), ViewActions.replaceText(text))
}

fun Int.scrollAndClick() {
    onView(withId(this)).perform(scrollTo(), ViewActions.click())
}

fun Int.click() {
    onView(withId(this)).perform(ViewActions.click())
}
