
package com.owncloud.android.utils

import com.owncloud.android.lib.common.operations.RemoteOperationResult
import io.mockk.every
import io.mockk.mockk

fun <T> createRemoteOperationResultMock(
    data: T,
    isSuccess: Boolean,
    httpPhrase: String? = null,
    resultCode: RemoteOperationResult.ResultCode? = null,
    exception: Exception? = null,
    authenticationHeader: List<String> = listOf(),
    httpCode: Int? = null,
    redirectedLocation: String? = null
): RemoteOperationResult<T> {
    val remoteOperationResult = mockk<RemoteOperationResult<T>>(relaxed = true)

    every { remoteOperationResult.data } returns data

    every { remoteOperationResult.isSuccess } returns isSuccess

    if (httpPhrase != null) {
        every { remoteOperationResult.httpPhrase } returns httpPhrase
    }

    if (resultCode != null) {
        every { remoteOperationResult.code } returns resultCode
    }

    if (exception != null) {
        throw exception
    }

    if (authenticationHeader.isNotEmpty()) {
        every { remoteOperationResult.authenticateHeaders } returns authenticationHeader
    }

    if (httpCode != null) {
        every { remoteOperationResult.httpCode } returns httpCode
    }

    if (redirectedLocation != null) {
        every { remoteOperationResult.redirectedLocation } returns redirectedLocation
    }

    return remoteOperationResult
}
