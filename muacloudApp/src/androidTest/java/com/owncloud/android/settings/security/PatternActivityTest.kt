
package com.owncloud.android.settings.security

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.owncloud.android.R
import com.owncloud.android.db.PreferenceManager
import com.owncloud.android.presentation.security.pattern.PatternActivity
import com.owncloud.android.presentation.security.pattern.PatternViewModel
import com.owncloud.android.testutil.security.OC_PATTERN
import com.owncloud.android.utils.matchers.isDisplayed
import com.owncloud.android.utils.matchers.withText
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class PatternActivityTest {

    private lateinit var activityScenario: ActivityScenario<PatternActivity>

    private lateinit var context: Context

    private lateinit var patternViewModel: PatternViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        patternViewModel = mockk(relaxUnitFun = true)

        stopKoin()

        startKoin {
            context
            allowOverride(override = true)
            modules(
                module {
                    viewModel {
                        patternViewModel
                    }
                }
            )
        }
    }

    @After
    fun tearDown() {

        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
    }

    @Test
    fun patternLockView() {

        openPatternActivity(PatternActivity.ACTION_REQUEST_WITH_RESULT)

        with(R.id.header_pattern) {
            isDisplayed(true)
            withText(R.string.pattern_configure_pattern)
        }
        with(R.id.explanation_pattern) {
            isDisplayed(true)
            withText(R.string.pattern_configure_your_pattern_explanation)
        }
        R.id.pattern_lock_view.isDisplayed(true)
    }

    @Test
    fun removePatternLock() {

        storePattern()

        openPatternActivity(PatternActivity.ACTION_CHECK_WITH_RESULT)

        with(R.id.header_pattern) {
            isDisplayed(true)
            withText(R.string.pattern_remove_pattern)
        }
        with(R.id.explanation_pattern) {
            isDisplayed(true)
            withText(R.string.pattern_no_longer_required)
        }
    }

    private fun storePattern() {
        val appPrefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
        appPrefs.apply {
            putString(PatternActivity.PREFERENCE_PATTERN, OC_PATTERN)
            putBoolean(PatternActivity.PREFERENCE_SET_PATTERN, true)
            apply()
        }
    }

    private fun openPatternActivity(mode: String) {
        val intent = Intent(context, PatternActivity::class.java).apply {
            action = mode
        }
        activityScenario = ActivityScenario.launch(intent)
    }
}
