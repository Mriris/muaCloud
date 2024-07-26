

package com.owncloud.android.presentation.avatar

import android.accounts.Account
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import com.owncloud.android.MainApp.Companion.appContext
import com.owncloud.android.R
import com.owncloud.android.datamodel.ThumbnailsCacheManager
import com.owncloud.android.domain.UseCaseResult
import com.owncloud.android.domain.capabilities.usecases.GetStoredCapabilitiesUseCase
import com.owncloud.android.domain.exceptions.FileNotFoundException
import com.owncloud.android.domain.user.model.UserAvatar
import com.owncloud.android.domain.user.usecases.GetUserAvatarAsyncUseCase
import com.owncloud.android.ui.DefaultAvatarTextDrawable
import com.owncloud.android.utils.BitmapUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.error.InstanceCreationException
import timber.log.Timber
import kotlin.math.roundToInt


class AvatarManager : KoinComponent {

    fun getAvatarForAccount(
        account: Account,
        fetchIfNotCached: Boolean,
        displayRadius: Float
    ): Drawable? {
        val imageKey = getImageKeyForAccount(account)

        val avatarBitmap = ThumbnailsCacheManager.getBitmapFromDiskCache(imageKey)
        avatarBitmap?.let {
            Timber.i("Avatar retrieved from cache with imageKey: $imageKey")
            return BitmapUtils.bitmapToCircularBitmapDrawable(appContext.resources, it)
        }

        val shouldFetchAvatar = try {
            val getStoredCapabilitiesUseCase: GetStoredCapabilitiesUseCase by inject()
            val storedCapabilities = getStoredCapabilitiesUseCase(GetStoredCapabilitiesUseCase.Params(account.name))
            storedCapabilities?.isFetchingAvatarAllowed() ?: true
        } catch (instanceCreationException: InstanceCreationException) {
            Timber.e(instanceCreationException, "Koin may not be initialized at this point")
            true
        }

        if (fetchIfNotCached && shouldFetchAvatar) {
            Timber.i("Avatar with imageKey $imageKey is not available in cache. Fetching from server...")
            val getUserAvatarAsyncUseCase: GetUserAvatarAsyncUseCase by inject()
            val useCaseResult =
                getUserAvatarAsyncUseCase(GetUserAvatarAsyncUseCase.Params(accountName = account.name))
            handleAvatarUseCaseResult(account, useCaseResult)?.let { return it }
        }

        try {
            Timber.i("Avatar with imageKey $imageKey is not available in cache. Generating one...")
            return DefaultAvatarTextDrawable.createAvatar(account.name, displayRadius)

        } catch (e: Exception) {

            Timber.e(e, "Error calculating RGB value for active account icon.")
        }
        return null
    }

    
    private fun getAvatarDimension(): Int = appContext.resources.getDimension(R.dimen.file_avatar_size).roundToInt()

    private fun getImageKeyForAccount(account: Account) = "a_${account.name}"

    
    fun handleAvatarUseCaseResult(
        account: Account,
        useCaseResult: UseCaseResult<UserAvatar>
    ): Drawable? {
        Timber.d("Fetch avatar use case is success: ${useCaseResult.isSuccess}")
        val imageKey = getImageKeyForAccount(account)

        if (useCaseResult.isSuccess) {
            val userAvatar = useCaseResult.getDataOrNull()
            userAvatar?.let {
                try {
                    var bitmap = BitmapFactory.decodeByteArray(it.avatarData, 0, it.avatarData.size)
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, getAvatarDimension(), getAvatarDimension())

                    bitmap?.let {
                        ThumbnailsCacheManager.addBitmapToCache(imageKey, bitmap)
                        Timber.d("User avatar saved into cache -> %s", imageKey)
                        return BitmapUtils.bitmapToCircularBitmapDrawable(appContext.resources, bitmap)
                    }
                } catch (t: Throwable) {

                    Timber.e(t, "Generation of avatar for $imageKey failed")
                    if (t is OutOfMemoryError) {
                        System.gc()
                    }
                    null
                }
            }

        } else if (useCaseResult.getThrowableOrNull() is FileNotFoundException) {
            Timber.i("No avatar available, removing cached copy")
            ThumbnailsCacheManager.removeBitmapFromCache(imageKey)
        }
        return null
    }
}
