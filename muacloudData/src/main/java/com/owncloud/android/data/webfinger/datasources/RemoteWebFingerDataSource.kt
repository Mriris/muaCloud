
package com.owncloud.android.data.webfinger.datasources

import com.owncloud.android.domain.webfinger.model.WebFingerRel

interface RemoteWebFingerDataSource {
    fun getInstancesFromWebFinger(
        lookupServer: String,
        rel: WebFingerRel,
        resource: String
    ): List<String>

    fun getInstancesFromAuthenticatedWebFinger(
        lookupServer: String,
        rel: WebFingerRel,
        resource: String,
        username: String,
        accessToken: String,
    ): List<String>
}
