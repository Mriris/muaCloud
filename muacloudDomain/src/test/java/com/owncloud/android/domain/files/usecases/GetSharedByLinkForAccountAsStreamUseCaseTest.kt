

package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFileWithSyncInfo
import com.owncloud.android.testutil.OC_FILES_WITH_SYNC_INFO
import com.owncloud.android.testutil.OC_FILES_WITH_SYNC_INFO_EMPTY
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetSharedByLinkForAccountAsStreamUseCaseTest {

    private val repository: FileRepository = spyk()
    private val useCase = GetSharedByLinkForAccountAsStreamUseCase(repository)
    private val useCaseParams = GetSharedByLinkForAccountAsStreamUseCase.Params(owner = "owner")

    @Test
    fun `get files shared by link - ok`() = runTest {
        every { repository.getSharedByLinkWithSyncInfoForAccountAsFlow(useCaseParams.owner) } returns flowOf(OC_FILES_WITH_SYNC_INFO)

        val useCaseResult = useCase(useCaseParams)
        val listEmittedByFlow: List<OCFileWithSyncInfo> = useCaseResult.first()

        Assert.assertTrue(listEmittedByFlow.containsAll(OC_FILES_WITH_SYNC_INFO))

        verify(exactly = 1) { repository.getSharedByLinkWithSyncInfoForAccountAsFlow(useCaseParams.owner) }
    }

    @Test
    fun `get files shared by link - ok - empty list`() = runTest {
        every { repository.getSharedByLinkWithSyncInfoForAccountAsFlow(useCaseParams.owner) } returns flowOf(OC_FILES_WITH_SYNC_INFO_EMPTY)

        val useCaseResult = useCase(useCaseParams)
        val listEmittedByFlow: List<OCFileWithSyncInfo> = useCaseResult.first()

        Assert.assertTrue(listEmittedByFlow.isEmpty())

        verify(exactly = 1) { repository.getSharedByLinkWithSyncInfoForAccountAsFlow(useCaseParams.owner) }
    }
}
