
package com.owncloud.android.data.webfinger.repository

import com.owncloud.android.data.webfinger.datasources.RemoteWebFingerDataSource
import com.owncloud.android.domain.webfinger.WebFingerRepository
import com.owncloud.android.domain.webfinger.model.WebFingerRel

class OCWebFingerRepository(
    private val remoteWebFingerDatasource: RemoteWebFingerDataSource,
) : WebFingerRepository {

    override fun getInstancesFromWebFinger(
        server: String,
        rel: WebFingerRel,
        resource: String
    ): List<String> =
        remoteWebFingerDatasource.getInstancesFromWebFinger(
            lookupServer = server,
            rel = rel,
            resource = resource
        )

    override fun getInstancesFromAuthenticatedWebFinger(
        server: String,
        rel: WebFingerRel,
        resource: String,
        username: String,
        accessToken: String,
    ): List<String> =
        remoteWebFingerDatasource.getInstancesFromAuthenticatedWebFinger(
            lookupServer = server,
            rel = rel,
            resource = resource,
            username = username,
            accessToken = accessToken,
        )
}
