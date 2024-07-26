package com.owncloud.android.domain.availableoffline.usecases

import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.files.model.OCFile

class GetFilesAvailableOfflineFromEveryAccountUseCase(
    private val fileRepository: FileRepository
) : BaseUseCase<List<OCFile>, Unit>() {

    override fun run(params: Unit): List<OCFile> = fileRepository.getFilesAvailableOfflineFromEveryAccount()
}
