
package com.owncloud.android.utils.matchers

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun withTextColor(
    @ColorRes textColor: Int
): Matcher<View> =
    object : BoundedMatcher<View, TextView>(TextView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("TextView with text color: $textColor")
        }

        override fun matchesSafely(view: TextView): Boolean {
            val expectedColor = ContextCompat.getColor(view.context, textColor)
            val actualColor = view.currentTextColor
            return actualColor == expectedColor
        }
    }
