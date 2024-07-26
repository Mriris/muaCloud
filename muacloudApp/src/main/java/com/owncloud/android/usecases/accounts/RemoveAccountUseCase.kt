

package com.owncloud.android.usecases.accounts

import com.owncloud.android.data.appregistry.datasources.LocalAppRegistryDataSource
import com.owncloud.android.data.capabilities.datasources.LocalCapabilitiesDataSource
import com.owncloud.android.data.files.datasources.LocalFileDataSource
import com.owncloud.android.data.sharing.shares.datasources.LocalShareDataSource
import com.owncloud.android.data.spaces.datasources.LocalSpacesDataSource
import com.owncloud.android.data.user.datasources.LocalUserDataSource
import com.owncloud.android.domain.BaseUseCase
import com.owncloud.android.domain.camerauploads.usecases.GetCameraUploadsConfigurationUseCase
import com.owncloud.android.domain.camerauploads.usecases.ResetPictureUploadsUseCase
import com.owncloud.android.domain.camerauploads.usecases.ResetVideoUploadsUseCase
import com.owncloud.android.usecases.transfers.uploads.CancelTransfersFromAccountUseCase
class RemoveAccountUseCase(
    private val getCameraUploadsConfigurationUseCase: GetCameraUploadsConfigurationUseCase,
    private val resetPictureUploadsUseCase: ResetPictureUploadsUseCase,
    private val resetVideoUploadsUseCase: ResetVideoUploadsUseCase,
    private val cancelTransfersFromAccountUseCase: CancelTransfersFromAccountUseCase,
    private val localFileDataSource: LocalFileDataSource,
    private val localCapabilitiesDataSource: LocalCapabilitiesDataSource,
    private val localShareDataSource: LocalShareDataSource,
    private val localUserDataSource: LocalUserDataSource,
    private val localSpacesDataSource: LocalSpacesDataSource,
    private val localAppRegistryDataSource: LocalAppRegistryDataSource,
) : BaseUseCase<Unit, RemoveAccountUseCase.Params>() {

    override fun run(params: Params) {

        val cameraUploadsConfiguration = getCameraUploadsConfigurationUseCase(Unit)
        if (params.accountName == cameraUploadsConfiguration.getDataOrNull()?.pictureUploadsConfiguration?.accountName) {
            resetPictureUploadsUseCase(Unit)
        }
        if (params.accountName == cameraUploadsConfiguration.getDataOrNull()?.videoUploadsConfiguration?.accountName) {
            resetVideoUploadsUseCase(Unit)
        }

        cancelTransfersFromAccountUseCase(
            CancelTransfersFromAccountUseCase.Params(accountName = params.accountName)
        )

        localFileDataSource.deleteFilesForAccount(params.accountName)

        localCapabilitiesDataSource.deleteCapabilitiesForAccount(params.accountName)

        localShareDataSource.deleteSharesForAccount(params.accountName)

        localUserDataSource.deleteQuotaForAccount(params.accountName)

        localSpacesDataSource.deleteSpacesForAccount(params.accountName)

        localAppRegistryDataSource.deleteAppRegistryForAccount(params.accountName)
    }

    data class Params(
        val accountName: String
    )
}
