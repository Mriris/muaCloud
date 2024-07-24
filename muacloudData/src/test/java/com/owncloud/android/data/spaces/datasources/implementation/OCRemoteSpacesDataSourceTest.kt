

package com.owncloud.android.data.spaces.datasources.implementation

import com.owncloud.android.data.ClientManager
import com.owncloud.android.data.spaces.datasources.implementation.OCRemoteSpacesDataSource.Companion.toModel
import com.owncloud.android.lib.resources.spaces.services.OCSpacesService
import com.owncloud.android.testutil.OC_ACCOUNT_NAME
import com.owncloud.android.testutil.SPACE_RESPONSE
import com.owncloud.android.utils.createRemoteOperationResultMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OCRemoteSpacesDataSourceTest {

    private lateinit var ocRemoteSpacesDataSource: OCRemoteSpacesDataSource

    private val ocSpaceService: OCSpacesService = mockk()
    private val clientManager: ClientManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        ocRemoteSpacesDataSource = OCRemoteSpacesDataSource(clientManager)
        every { clientManager.getSpacesService(OC_ACCOUNT_NAME) } returns ocSpaceService
    }

    @Test
    fun `refreshSpacesForAccount returns a list of OCSpace`() {
        val getRemoteSpacesOperationResult = createRemoteOperationResultMock(
            listOf(SPACE_RESPONSE), isSuccess = true
        )

        every { ocSpaceService.getSpaces() } returns getRemoteSpacesOperationResult

        val resultActual = ocRemoteSpacesDataSource.refreshSpacesForAccount(OC_ACCOUNT_NAME)

        assertEquals(listOf(SPACE_RESPONSE.toModel(OC_ACCOUNT_NAME)), resultActual)

        verify(exactly = 1) {
            clientManager.getSpacesService(OC_ACCOUNT_NAME)
            ocSpaceService.getSpaces()
        }
    }
}
