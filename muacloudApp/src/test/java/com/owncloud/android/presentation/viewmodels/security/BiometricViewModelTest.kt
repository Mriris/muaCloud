

package com.owncloud.android.presentation.viewmodels.security

import com.owncloud.android.R
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.presentation.security.biometric.BiometricViewModel
import com.owncloud.android.presentation.security.PREFERENCE_LAST_UNLOCK_TIMESTAMP
import com.owncloud.android.presentation.security.passcode.PassCodeActivity
import com.owncloud.android.presentation.viewmodels.ViewModelTest
import com.owncloud.android.providers.ContextProvider
import com.owncloud.android.testutil.security.OC_PASSCODE_4_DIGITS
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class BiometricViewModelTest : ViewModelTest() {
    private lateinit var biometricViewModel: BiometricViewModel
    private lateinit var preferencesProvider: SharedPreferencesProvider
    private lateinit var contextProvider: ContextProvider

    @Before
    fun setUp() {
        preferencesProvider = mockk(relaxUnitFun = true)
        contextProvider = mockk(relaxUnitFun = true)
        biometricViewModel = BiometricViewModel(preferencesProvider, contextProvider)
    }

    @Test
    fun `set last unlock timestamp - ok`() {
        biometricViewModel.setLastUnlockTimestamp()

        verify(exactly = 1) {
            preferencesProvider.putLong(PREFERENCE_LAST_UNLOCK_TIMESTAMP, any())
        }
    }

    @Test
    fun `should ask for new passcode - ok - true`() {
        every { preferencesProvider.getString(any(), any()) } returns OC_PASSCODE_4_DIGITS
        every { contextProvider.getInt(any()) } returns 6

        val shouldAsk = biometricViewModel.shouldAskForNewPassCode()
        assertTrue(shouldAsk)

        verify(exactly = 1) {
            preferencesProvider.getString(PassCodeActivity.PREFERENCE_PASSCODE, any())
            contextProvider.getInt(R.integer.passcode_digits)
        }
    }

    @Test
    fun `should ask for new passcode - ok - false`() {
        every { preferencesProvider.getString(any(), any()) } returns OC_PASSCODE_4_DIGITS
        every { contextProvider.getInt(any()) } returns 4

        val shouldAsk = biometricViewModel.shouldAskForNewPassCode()
        assertFalse(shouldAsk)

        verify(exactly = 1) {
            preferencesProvider.getString(PassCodeActivity.PREFERENCE_PASSCODE, any())
            contextProvider.getInt(R.integer.passcode_digits)
        }
    }

    @Test
    fun `should ask for new passcode - ko - passcode is null`() {
        every { preferencesProvider.getString(any(), any()) } returns null
        every { contextProvider.getInt(any()) } returns 4

        val shouldAsk = biometricViewModel.shouldAskForNewPassCode()
        assertFalse(shouldAsk)

        verify(exactly = 1) {
            preferencesProvider.getString(PassCodeActivity.PREFERENCE_PASSCODE, any())
            contextProvider.getInt(R.integer.passcode_digits)
        }
    }

    @Test
    fun `remove passcode - ok`() {
        biometricViewModel.removePassCode()

        verify(exactly = 1) {
            preferencesProvider.removePreference(PassCodeActivity.PREFERENCE_PASSCODE)
            preferencesProvider.putBoolean(PassCodeActivity.PREFERENCE_SET_PASSCODE, false)
        }
    }
}
