
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class GetFolderImagesUseCase(
    private val repository: FileRepository
) : BaseUseCaseWithResult<List<OCFile>, GetFolderImagesUseCase.Params>() {

    override fun run(params: Params) = repository.getFolderImages(params.folderId)

    data class Params(val folderId: Long)

}
