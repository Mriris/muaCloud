

package com.owncloud.android.dependecyinjection

import com.owncloud.android.data.appregistry.repository.OCAppRegistryRepository
import com.owncloud.android.data.authentication.repository.OCAuthenticationRepository
import com.owncloud.android.data.capabilities.repository.OCCapabilityRepository
import com.owncloud.android.data.files.repository.OCFileRepository
import com.owncloud.android.data.folderbackup.OCFolderBackupRepository
import com.owncloud.android.data.oauth.repository.OCOAuthRepository
import com.owncloud.android.data.server.repository.OCServerInfoRepository
import com.owncloud.android.data.sharing.sharees.repository.OCShareeRepository
import com.owncloud.android.data.sharing.shares.repository.OCShareRepository
import com.owncloud.android.data.spaces.repository.OCSpacesRepository
import com.owncloud.android.data.transfers.repository.OCTransferRepository
import com.owncloud.android.data.user.repository.OCUserRepository
import com.owncloud.android.data.webfinger.repository.OCWebFingerRepository
import com.owncloud.android.domain.appregistry.AppRegistryRepository
import com.owncloud.android.domain.authentication.AuthenticationRepository
import com.owncloud.android.domain.authentication.oauth.OAuthRepository
import com.owncloud.android.domain.camerauploads.FolderBackupRepository
import com.owncloud.android.domain.capabilities.CapabilityRepository
import com.owncloud.android.domain.files.FileRepository
import com.owncloud.android.domain.server.ServerInfoRepository
import com.owncloud.android.domain.sharing.sharees.ShareeRepository
import com.owncloud.android.domain.sharing.shares.ShareRepository
import com.owncloud.android.domain.spaces.SpacesRepository
import com.owncloud.android.domain.transfers.TransferRepository
import com.owncloud.android.domain.user.UserRepository
import com.owncloud.android.domain.webfinger.WebFingerRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    factoryOf(::OCAppRegistryRepository) bind AppRegistryRepository::class
    factoryOf(::OCAuthenticationRepository) bind AuthenticationRepository::class
    factoryOf(::OCCapabilityRepository) bind CapabilityRepository::class
    factoryOf(::OCFileRepository) bind FileRepository::class
    factoryOf(::OCFolderBackupRepository) bind FolderBackupRepository::class
    factoryOf(::OCOAuthRepository) bind OAuthRepository::class
    factoryOf(::OCServerInfoRepository) bind ServerInfoRepository::class
    factoryOf(::OCShareRepository) bind ShareRepository::class
    factoryOf(::OCShareeRepository) bind ShareeRepository::class
    factoryOf(::OCSpacesRepository) bind SpacesRepository::class
    factoryOf(::OCTransferRepository) bind TransferRepository::class
    factoryOf(::OCUserRepository) bind UserRepository::class
    factoryOf(::OCWebFingerRepository) bind WebFingerRepository::class
}
