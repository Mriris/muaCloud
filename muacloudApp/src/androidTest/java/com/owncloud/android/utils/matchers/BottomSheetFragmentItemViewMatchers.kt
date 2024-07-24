
package com.owncloud.android.utils.matchers

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.owncloud.android.R
import com.owncloud.android.presentation.common.BottomSheetFragmentItemView
import org.hamcrest.Description
import org.hamcrest.Matcher

fun Int.bsfItemWithTitle(@StringRes title: Int, @ColorRes tintColor: Int?) {
    Espresso.onView(ViewMatchers.withId(this)).inRoot(RootMatchers.isDialog())
        .check(ViewAssertions.matches(withTitle(title, tintColor)))
}

fun Int.bsfItemWithIcon(@DrawableRes drawable: Int, @ColorRes tintColor: Int?) {
    Espresso.onView(ViewMatchers.withId(this)).inRoot(RootMatchers.isDialog())
        .check(ViewAssertions.matches(withIcon(drawable, tintColor)))
}

private fun withTitle(@StringRes title: Int, @ColorRes tintColor: Int?): Matcher<View> =
    object : BoundedMatcher<View, BottomSheetFragmentItemView>(BottomSheetFragmentItemView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("BottomSheetFragmentItemView with text: $title")
            tintColor?.let { description.appendText(" and tint color id: $tintColor") }
        }

        override fun matchesSafely(item: BottomSheetFragmentItemView): Boolean {
            val itemTitleView = item.findViewById<TextView>(R.id.item_title)
            val textMatches = withText(title).matches(itemTitleView)
            val textColorMatches = tintColor?.let { withTextColor(tintColor).matches(itemTitleView) } ?: true
            return textMatches && textColorMatches
        }
    }

private fun withIcon(@DrawableRes drawable: Int, @ColorRes tintColor: Int?): Matcher<View> =
    object : BoundedMatcher<View, BottomSheetFragmentItemView>(BottomSheetFragmentItemView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("BottomSheetFragmentItemView with icon: $drawable")
            tintColor?.let { description.appendText(" and tint color id: $tintColor") }
        }

        override fun matchesSafely(item: BottomSheetFragmentItemView): Boolean {
            val itemIconView = item.findViewById<ImageView>(R.id.item_icon)
            return withDrawable(drawable, tintColor).matches(itemIconView)
        }
    }
