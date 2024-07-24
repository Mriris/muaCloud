

package com.owncloud.android.testutil

import com.owncloud.android.domain.server.model.ServerInfo

const val OC_SECURE_BASE_URL = "https://demo.owncloud.com"
const val OC_INSECURE_BASE_URL = "http://demo.owncloud.com"

val OC_SECURE_SERVER_INFO_BASIC_AUTH = ServerInfo.BasicServer(
    baseUrl = OC_SECURE_BASE_URL,
    ownCloudVersion = "10.3.2.1",
)

val OC_INSECURE_SERVER_INFO_BASIC_AUTH = ServerInfo.BasicServer(
    baseUrl = OC_INSECURE_BASE_URL,
    ownCloudVersion = "10.3.2.1",
)

val OC_SECURE_SERVER_INFO_BEARER_AUTH = ServerInfo.OAuth2Server(
    baseUrl = OC_SECURE_BASE_URL,
    ownCloudVersion = "10.3.2.1",
)

val OC_INSECURE_SERVER_INFO_BEARER_AUTH = ServerInfo.OAuth2Server(
    baseUrl = OC_INSECURE_BASE_URL,
    ownCloudVersion = "10.3.2.1",
)

const val OC_WEBFINGER_INSTANCE_URL = "WEBFINGER_INSTANCE"

val OC_SECURE_SERVER_INFO_BEARER_AUTH_WEBFINGER_INSTANCE = ServerInfo.OAuth2Server(
    baseUrl = OC_WEBFINGER_INSTANCE_URL,
    ownCloudVersion = "10.3.2.1",
)
