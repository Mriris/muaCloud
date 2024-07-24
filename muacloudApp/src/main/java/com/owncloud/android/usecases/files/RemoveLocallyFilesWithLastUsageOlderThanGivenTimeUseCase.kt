

package com.owncloud.android.usecases.files

import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.usecases.RemoveFileUseCase
import com.owncloud.android.presentation.settings.advanced.PREFERENCE_REMOVE_LOCAL_FILES
import com.owncloud.android.presentation.settings.advanced.RemoveLocalFiles

class RemoveLocallyFilesWithLastUsageOlderThanGivenTimeUseCase(
    private val fileRepository: FileRepository,
    private val removeFileUseCase: RemoveFileUseCase,
    private val preferencesProvider: SharedPreferencesProvider,
) : BaseUseCaseWithResult<Unit, RemoveLocallyFilesWithLastUsageOlderThanGivenTimeUseCase.Params>() {
    override fun run(params: Params) {
        val timeSelected =
            RemoveLocalFiles.valueOf(preferencesProvider.getString(PREFERENCE_REMOVE_LOCAL_FILES, RemoveLocalFiles.NEVER.name)!!).toMilliseconds()
        val listOfFilesToDelete = fileRepository.getFilesWithLastUsageOlderThanGivenTime(timeSelected)
        if (listOfFilesToDelete.isNotEmpty()) {
            val listOfFilesToDeleteUpdated = listOfFilesToDelete.filter { file ->
                file.remoteId != params.idFilePreviewing
            }
            removeFileUseCase(RemoveFileUseCase.Params(listOfFilesToDeleteUpdated, true))
        }
    }

    data class Params(
        val idFilePreviewing: String?,
    )
}
