

package com.owncloud.android.data.appregistry.datasources.implementation

import com.owncloud.android.data.appregistry.datasources.LocalAppRegistryDataSource
import com.owncloud.android.data.appregistry.db.AppRegistryDao
import com.owncloud.android.data.appregistry.db.AppRegistryEntity
import com.owncloud.android.domain.appregistry.model.AppRegistry
import com.owncloud.android.domain.appregistry.model.AppRegistryMimeType
import com.owncloud.android.domain.appregistry.model.AppRegistryProvider
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.reflect.Type

class OCLocalAppRegistryDataSource(
    private val appRegistryDao: AppRegistryDao,
) : LocalAppRegistryDataSource {
    override fun getAppRegistryForMimeTypeAsStream(accountName: String, mimeType: String): Flow<AppRegistryMimeType?> =
        appRegistryDao.getAppRegistryForMimeType(accountName, mimeType).map { it?.toModel() }

    override fun getAppRegistryWhichAllowCreation(accountName: String): Flow<List<AppRegistryMimeType>> =
        appRegistryDao.getAppRegistryWhichAllowCreation(accountName).map { listAppRegistryEntities ->
            listAppRegistryEntities.map { it.toModel() }
        }

    override fun saveAppRegistryForAccount(appRegistry: AppRegistry) {
        val appRegistryEntitiesToInsert = mutableListOf<AppRegistryEntity>()

        val newAppRegistryEntities = appRegistry.toEntities(appRegistry.accountName)
        newAppRegistryEntities.forEach { appRegistryEntity ->
            appRegistryEntitiesToInsert.add(appRegistryEntity)
        }

        appRegistryDao.deleteAppRegistryForAccount(appRegistry.accountName)
        appRegistryDao.upsertAppRegistries(appRegistryEntitiesToInsert)
    }

    override fun deleteAppRegistryForAccount(accountName: String) {
        appRegistryDao.deleteAppRegistryForAccount(accountName)
    }

    private fun AppRegistry.toEntities(accountName: String): List<AppRegistryEntity> {
        return mimetypes.map { appRegistryMimeTypes ->
            AppRegistryEntity(
                accountName = accountName,
                mimeType = appRegistryMimeTypes.mimeType,
                ext = appRegistryMimeTypes.ext,
                appProviders = appRegistryMimeTypes.appProviders.toJsonString(),
                name = appRegistryMimeTypes.name,
                icon = appRegistryMimeTypes.icon,
                description = appRegistryMimeTypes.description,
                allowCreation = appRegistryMimeTypes.allowCreation,
                defaultApplication = appRegistryMimeTypes.defaultApplication,
            )
        }
    }

    private fun AppRegistryEntity.toModel(): AppRegistryMimeType =
        AppRegistryMimeType(
            mimeType = mimeType,
            ext = ext,
            appProviders = appProviders.toAppRegistryProvider(),
            name = name,
            icon = icon,
            description = description,
            allowCreation = allowCreation,
            defaultApplication = defaultApplication,
        )

    private fun List<AppRegistryProvider>.toJsonString(): String {
        val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type: Type = Types.newParameterizedType(List::class.java, AppRegistryProvider::class.java)

        val jsonAdapter: JsonAdapter<List<AppRegistryProvider>> = moshi.adapter(type)

        return jsonAdapter.toJson(this)
    }

    private fun String.toAppRegistryProvider(): List<AppRegistryProvider> {
        val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type: Type = Types.newParameterizedType(List::class.java, AppRegistryProvider::class.java)

        val jsonAdapter: JsonAdapter<List<AppRegistryProvider>> = moshi.adapter(type)

        return jsonAdapter.fromJson(this) ?: emptyList()
    }
}
