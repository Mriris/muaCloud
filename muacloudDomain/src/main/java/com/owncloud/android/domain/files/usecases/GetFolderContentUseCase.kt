
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class GetFolderContentUseCase(
    private val repository: FileRepository
) : BaseUseCaseWithResult<List<OCFile>, GetFolderContentUseCase.Params>() {

    override fun run(params: Params) = repository.getFolderContent(params.folderId)

    data class Params(val folderId: Long)

}
