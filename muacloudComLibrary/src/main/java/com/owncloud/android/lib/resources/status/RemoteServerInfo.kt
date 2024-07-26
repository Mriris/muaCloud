package com.owncloud.android.lib.resources.status

data class RemoteServerInfo(
    val ownCloudVersion: OwnCloudVersion,
    val baseUrl: String,
    val isSecureConnection: Boolean
)
