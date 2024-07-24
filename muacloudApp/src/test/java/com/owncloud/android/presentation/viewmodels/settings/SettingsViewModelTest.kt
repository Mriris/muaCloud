

package com.owncloud.android.presentation.viewmodels.settings

import com.owncloud.android.presentation.settings.SettingsViewModel
import com.owncloud.android.presentation.viewmodels.ViewModelTest
import com.owncloud.android.providers.AccountProvider
import com.owncloud.android.testutil.OC_ACCOUNT
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest : ViewModelTest() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var accountProvider: AccountProvider

    @Before
    fun setUp() {
        accountProvider = mockk()

        settingsViewModel = SettingsViewModel(accountProvider)
    }

    @Test
    fun `is there attached account - ok - true`() {
        every { accountProvider.getCurrentOwnCloudAccount() } returns OC_ACCOUNT

        val attachedAccount = settingsViewModel.isThereAttachedAccount()

        assertTrue(attachedAccount)

        verify(exactly = 1) {
            accountProvider.getCurrentOwnCloudAccount()
        }
    }

    @Test
    fun `is there attached account - ok - false`() {
        every { accountProvider.getCurrentOwnCloudAccount() } returns null

        val attachedAccount = settingsViewModel.isThereAttachedAccount()

        assertFalse(attachedAccount)

        verify(exactly = 1) {
            accountProvider.getCurrentOwnCloudAccount()
        }
    }
}
