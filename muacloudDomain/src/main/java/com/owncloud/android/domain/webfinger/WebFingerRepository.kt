

package com.owncloud.android.domain.webfinger

import com.owncloud.android.domain.webfinger.model.WebFingerRel

interface WebFingerRepository {
    fun getInstancesFromWebFinger(
        server: String,
        rel: WebFingerRel,
        resource: String
    ): List<String>

    fun getInstancesFromAuthenticatedWebFinger(
        server: String,
        rel: WebFingerRel,
        resource: String,
        username: String,
        accessToken: String,
    ): List<String>
}
