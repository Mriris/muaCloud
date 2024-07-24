

package com.owncloud.android.domain.server.model

enum class AuthenticationMethod {
    BASIC_HTTP_AUTH {
        override fun toString(): String = "basic"
    },
    BEARER_TOKEN {
        override fun toString(): String = "bearer"
    },
    NONE {
        override fun toString(): String = "none"
    };
}
