

package com.owncloud.android.presentation.viewmodels.settings

import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.presentation.settings.logging.SettingsLogsViewModel
import com.owncloud.android.presentation.settings.logging.SettingsLogsFragment
import com.owncloud.android.presentation.viewmodels.ViewModelTest
import com.owncloud.android.providers.LogsProvider
import com.owncloud.android.providers.WorkManagerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsLogsViewModelTest : ViewModelTest() {
    private lateinit var logsViewModel: SettingsLogsViewModel
    private lateinit var preferencesProvider: SharedPreferencesProvider
    private lateinit var logsProvider: LogsProvider
    private lateinit var workManagerProvider: WorkManagerProvider

    @Before
    fun setUp() {
        preferencesProvider = mockk(relaxUnitFun = true)
        logsProvider = mockk(relaxUnitFun = true)
        workManagerProvider = mockk(relaxUnitFun = true)

        logsViewModel = SettingsLogsViewModel(
            preferencesProvider,
            logsProvider,
            workManagerProvider
        )
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun `should log http requests - ok`() {
        logsViewModel.shouldLogHttpRequests(true)

        verify(exactly = 1) {
            logsProvider.shouldLogHttpRequests(true)
        }
    }

    @Test
    fun `set enable logging - ok - true`() {
        logsViewModel.setEnableLogging(true)

        verify(exactly = 1) {
            preferencesProvider.putBoolean(SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING, true)
            logsProvider.startLogging()
        }
    }

    @Test
    fun `set enable logging - ok - false`() {
        logsViewModel.setEnableLogging(false)

        verify(exactly = 1) {
            preferencesProvider.putBoolean(SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING, false)
            logsProvider.stopLogging()
        }
    }

    @Test
    fun `is enable logging on - ok - true`() {
        every { preferencesProvider.getBoolean(any(), any()) } returns true

        val enableLoggingOn = logsViewModel.isLoggingEnabled()

        assertTrue(enableLoggingOn)

        verify(exactly = 1) {
            preferencesProvider.getBoolean(SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING, false)
        }
    }

    @Test
    fun `is enable logging on - ok - false`() {
        every { preferencesProvider.getBoolean(any(), any()) } returns false

        val enableLoggingOn = logsViewModel.isLoggingEnabled()

        assertFalse(enableLoggingOn)

        verify(exactly = 1) {
            preferencesProvider.getBoolean(SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING, false)
        }
    }

}
