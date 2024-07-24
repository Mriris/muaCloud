

package com.owncloud.android.domain


abstract class BaseUseCaseWithResult<out Type, in Params> {
    protected abstract fun run(params: Params): Type

    operator fun invoke(params: Params): UseCaseResult<Type> =
        try {
            UseCaseResult.Success(run(params))
        } catch (throwable: Throwable) {
            UseCaseResult.Error(throwable)
        }
}
