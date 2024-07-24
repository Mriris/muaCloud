

package com.owncloud.android.utils.matchers

import androidx.preference.Preference
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Assert.assertEquals

fun Preference.verifyPreference(
    keyPref: String,
    titlePref: String,
    summaryPref: String? = null,
    visible: Boolean,
    enabled: Boolean? = null
) {
    if (visible) onView(withText(titlePref)).check(matches(isDisplayed()))
    summaryPref?.let {
        if (visible) onView(withText(it)).check(matches(isDisplayed()))
        assertEquals(it, summary)
    }
    assertEquals(keyPref, key)
    assertEquals(titlePref, title)
    assertEquals(visible, isVisible)
    enabled?.let {
        assertEquals(enabled, isEnabled)
    }
}
