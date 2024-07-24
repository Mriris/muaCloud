

package com.owncloud.android.data.sharing.sharees.repository

import com.owncloud.android.data.sharing.sharees.datasources.RemoteShareeDataSource
import com.owncloud.android.domain.exceptions.NoConnectionWithServerException
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class OCShareeRepositoryTest {

    private val remoteShareeDataSource = mockk<RemoteShareeDataSource>(relaxed = true)
    private val oCShareeRepository: OCShareeRepository = OCShareeRepository((remoteShareeDataSource))

    @Test
    fun readShareesFromNetworkOk() {
        every { remoteShareeDataSource.getSharees(any(), any(), any(), any()) } returns arrayListOf()

        oCShareeRepository.getSharees("user", 1, 5, OC_ACCOUNT_NAME)

        verify(exactly = 1) {
            remoteShareeDataSource.getSharees("user", 1, 5, OC_ACCOUNT_NAME)
        }
    }

    @Test(expected = NoConnectionWithServerException::class)
    fun readShareesFromNetworkNoConnection() {
        every { remoteShareeDataSource.getSharees(any(), any(), any(), any()) } throws NoConnectionWithServerException()

        oCShareeRepository.getSharees("user", 1, 5, OC_ACCOUNT_NAME)

        verify(exactly = 1) {
            remoteShareeDataSource.getSharees("user", 1, 5, OC_ACCOUNT_NAME)
        }
    }
}
