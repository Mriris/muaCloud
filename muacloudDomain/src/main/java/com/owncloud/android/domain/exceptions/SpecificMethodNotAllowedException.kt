

package com.owncloud.android.domain.exceptions


class SpecificMethodNotAllowedException(
    httpPhrase: String?,
) : Throwable(message = httpPhrase)
