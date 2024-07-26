
package com.owncloud.android.domain.webfinger.model

enum class WebFingerRel(val uri: String) {
    OWNCLOUD_INSTANCE("http://webfinger.owncloud/rel/server-instance"),

    OIDC_ISSUER_DISCOVERY("http://openid.net/specs/connect/1.0/issuer")
}
