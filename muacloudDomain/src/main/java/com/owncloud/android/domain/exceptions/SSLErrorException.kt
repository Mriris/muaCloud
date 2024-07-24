

package com.owncloud.android.domain.exceptions

import java.lang.Exception

class SSLErrorException(override val message: String? = null, val code: SSLErrorCode = SSLErrorCode.GENERIC) : Exception(message)

enum class SSLErrorCode { GENERIC, NOT_HTTP_ALLOWED }

const val NOT_HTTP_ALLOWED_MESSAGE = "Connection is not secure, http traffic is not allowed."