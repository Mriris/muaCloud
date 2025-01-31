

package com.owncloud.android.domain.authentication.usecases

import com.owncloud.android.domain.authentication.AuthenticationRepository
import com.owncloud.android.domain.exceptions.AccountNotFoundException
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.OC_SECURE_BASE_URL
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetBaseUrlUseCaseTest {

    private val repository: AuthenticationRepository = spyk()
    private val useCase = GetBaseUrlUseCase(repository)
    private val useCaseParams = GetBaseUrlUseCase.Params(
        accountName = OC_ACCOUNT_NAME
    )

    @Test
    fun `get base url - ko - invalid params`() {
        val invalidGetBaseUrlUseCaseParams = useCaseParams.copy(accountName = "")
        val getBaseUrlUseCaseResult = useCase(invalidGetBaseUrlUseCaseParams)

        assertTrue(getBaseUrlUseCaseResult.isError)
        assertTrue(getBaseUrlUseCaseResult.getThrowableOrNull() is IllegalArgumentException)

        verify(exactly = 0) { repository.getBaseUrl(any()) }
    }

    @Test
    fun `get base url - ok`() {
        every { repository.getBaseUrl(any()) } returns OC_SECURE_BASE_URL

        val getBaseUrlUseCaseResult = useCase(useCaseParams)

        assertTrue(getBaseUrlUseCaseResult.isSuccess)
        assertEquals(OC_SECURE_BASE_URL, getBaseUrlUseCaseResult.getDataOrNull())

        verify(exactly = 1) { repository.getBaseUrl(any()) }
    }

    @Test
    fun `get base url - ko - another exception`() {
        every { repository.getBaseUrl(any()) } throws AccountNotFoundException()

        val getBaseUrlUseCaseResult = useCase(useCaseParams)

        assertTrue(getBaseUrlUseCaseResult.isError)
        assertTrue(getBaseUrlUseCaseResult.getThrowableOrNull() is AccountNotFoundException)

        verify(exactly = 1) { repository.getBaseUrl(any()) }
    }
}
