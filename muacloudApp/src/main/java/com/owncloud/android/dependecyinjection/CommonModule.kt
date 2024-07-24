

package com.owncloud.android.dependecyinjection

import androidx.work.WorkManager
import com.owncloud.android.presentation.avatar.AvatarManager
import com.owncloud.android.providers.AccountProvider
import com.owncloud.android.providers.ContextProvider
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import com.owncloud.android.providers.LogsProvider
import com.owncloud.android.providers.MdmProvider
import com.owncloud.android.providers.WorkManagerProvider
import com.owncloud.android.providers.implementation.OCContextProvider
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val commonModule = module {

    single { AvatarManager() }
    single { CoroutinesDispatcherProvider() }
    factory<ContextProvider> { OCContextProvider(androidContext()) }
    single { LogsProvider(get(), get()) }
    single { MdmProvider(androidContext()) }
    single { WorkManagerProvider(androidContext()) }
    single { AccountProvider(androidContext()) }
    single { WorkManager.getInstance(androidApplication()) }
}
