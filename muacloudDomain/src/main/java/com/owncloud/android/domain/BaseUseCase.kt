

package com.owncloud.android.domain


abstract class BaseUseCase<out Type, in Params> {

    protected abstract fun run(params: Params): Type

    operator fun invoke(params: Params): Type = run(params)
}
