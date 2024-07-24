
package com.owncloud.android.domain.files.usecases

import com.owncloud.android.domain.BaseUseCaseWithResult
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.FileListOption
import com.owncloud.android.domain.files.model.OCFile

class GetSearchFolderContentUseCase(
    private val repository: FileRepository
) : BaseUseCaseWithResult<List<OCFile>, GetSearchFolderContentUseCase.Params>() {

    override fun run(params: Params) = repository.getSearchFolderContent(
        fileListOption = params.fileListOption,
        folderId = params.folderId,
        search = params.search
    )

    data class Params(
        val fileListOption: FileListOption,
        val folderId: Long,
        val search: String
    )

}
