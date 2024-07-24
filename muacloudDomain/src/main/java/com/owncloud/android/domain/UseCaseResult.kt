

package com.owncloud.android.domain

sealed class UseCaseResult<out T> {
    data class Success<out T>(val data: T) : UseCaseResult<T>()
    data class Error<out T>(val throwable: Throwable) : UseCaseResult<T>()

    val isSuccess get() = this is Success
    val isError get() = this is Error

    fun getDataOrNull(): T? =
        when (this) {
            is Success -> data
            else -> null
        }

    fun getThrowableOrNull(): Throwable? =
        when (this) {
            is Error -> throwable
            else -> null
        }
}
