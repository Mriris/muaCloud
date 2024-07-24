

package com.owncloud.android.utils.matchers

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.CoreMatchers

fun Int.isDisplayed(displayed: Boolean) {
    val displayMatcher = if (displayed) ViewMatchers.isDisplayed() else CoreMatchers.not(ViewMatchers.isDisplayed())

    onView(withId(this))
        .check(matches(displayMatcher))
}

fun Int.isEnabled(enabled: Boolean) {
    val enableMatcher = if (enabled) ViewMatchers.isEnabled() else CoreMatchers.not(ViewMatchers.isEnabled())

    onView(withId(this))
        .check(matches(enableMatcher))
}

fun Int.isFocusable(focusable: Boolean) {
    val focusableMatcher = if (focusable) ViewMatchers.isFocusable() else CoreMatchers.not(ViewMatchers.isFocusable())

    onView(withId(this))
        .check(matches(focusableMatcher))
}

fun Int.withText(text: String) {
    onView(withId(this))
        .check(matches(ViewMatchers.withText(text)))
}

fun Int.withText(resourceId: Int) {
    onView(withId(this))
        .check(matches(ViewMatchers.withText(resourceId)))
}

fun Int.withChildCountAndId(count: Int, resourceId: Int) {
    onView(withId(this))
        .check(matches(withChildViewCount(count, withId(resourceId))))
}

fun Int.assertVisibility(visibility: ViewMatchers.Visibility) {
    onView(withId(this))
        .check(matches(ViewMatchers.withEffectiveVisibility(visibility)))
}

fun Int.assertChildCount(childs: Int) {
    onView(withId(this))
        .check(matches(hasChildCount(childs)))
}
