
package com.owncloud.android.files

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.owncloud.android.R
import com.owncloud.android.presentation.files.SortBottomSheetFragment
import com.owncloud.android.presentation.files.SortOrder
import com.owncloud.android.presentation.files.SortType
import com.owncloud.android.utils.matchers.bsfItemWithIcon
import com.owncloud.android.utils.matchers.bsfItemWithTitle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SortBottomSheetFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<SortBottomSheetFragment>
    private val fragmentListener = mockk<SortBottomSheetFragment.SortDialogListener>()

    @Before
    fun setUp() {
        val fragmentArgs = Bundle().apply {
            putParcelable(SortBottomSheetFragment.ARG_SORT_TYPE, SortType.SORT_TYPE_BY_NAME)
            putParcelable(SortBottomSheetFragment.ARG_SORT_ORDER, SortOrder.SORT_ORDER_ASCENDING)
        }
        fragmentScenario = launchFragment(fragmentArgs)
        every { fragmentListener.onSortSelected(any()) } returns Unit
        fragmentScenario.onFragment { it.sortDialogListener = fragmentListener }
    }

    @Test
    fun test_initial_view() {
        onView(withId(R.id.title))
            .inRoot(RootMatchers.isDialog())
            .check(matches(ViewMatchers.withText(R.string.actionbar_sort_title)))
            .check(matches(ViewMatchers.hasTextColor(R.color.bottom_sheet_fragment_title_color)))

        with(R.id.sort_by_name) {
            bsfItemWithTitle(R.string.global_name, R.color.primary)
            bsfItemWithIcon(R.drawable.ic_sort_by_name, R.color.primary)
        }
        with(R.id.sort_by_size) {
            bsfItemWithTitle(R.string.global_size, R.color.bottom_sheet_fragment_item_color)
            bsfItemWithIcon(R.drawable.ic_sort_by_size, R.color.bottom_sheet_fragment_item_color)
        }
        with(R.id.sort_by_date) {
            bsfItemWithTitle(R.string.global_date, R.color.bottom_sheet_fragment_item_color)
            bsfItemWithIcon(R.drawable.ic_sort_by_date, R.color.bottom_sheet_fragment_item_color)
        }
    }

    @Test
    fun test_sort_by_name_click() {
        onView(withId(R.id.sort_by_name)).inRoot(RootMatchers.isDialog()).perform(ViewActions.click())
        verify { fragmentListener.onSortSelected(SortType.SORT_TYPE_BY_NAME) }
    }

    @Test
    fun test_sort_by_date_click() {
        onView(withId(R.id.sort_by_date)).inRoot(RootMatchers.isDialog()).perform(ViewActions.click())
        verify { fragmentListener.onSortSelected(SortType.SORT_TYPE_BY_DATE) }
    }

    @Test
    fun test_sort_by_size_click() {
        onView(withId(R.id.sort_by_size)).inRoot(RootMatchers.isDialog()).perform(ViewActions.click())
        verify { fragmentListener.onSortSelected(SortType.SORT_TYPE_BY_SIZE) }
    }
}
