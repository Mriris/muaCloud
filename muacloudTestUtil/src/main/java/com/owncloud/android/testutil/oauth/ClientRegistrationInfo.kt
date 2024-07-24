
package com.owncloud.android.testutil.oauth

import com.owncloud.android.domain.authentication.oauth.model.ClientRegistrationInfo
import com.owncloud.android.testutil.OC_CLIENT_ID
import com.owncloud.android.testutil.OC_CLIENT_SECRET
import com.owncloud.android.testutil.OC_CLIENT_SECRET_EXPIRATION

val OC_CLIENT_REGISTRATION = ClientRegistrationInfo(
    clientId = OC_CLIENT_ID,
    clientSecret = OC_CLIENT_SECRET,
    clientIdIssuedAt = null,
    clientSecretExpiration = OC_CLIENT_SECRET_EXPIRATION
)
