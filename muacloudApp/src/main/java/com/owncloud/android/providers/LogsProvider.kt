

package com.owncloud.android.providers

import android.content.Context
import com.owncloud.android.BuildConfig
import com.owncloud.android.MainApp
import com.owncloud.android.R
import com.owncloud.android.data.providers.implementation.OCSharedPreferencesProvider
import com.owncloud.android.data.providers.ScopedStorageProvider
import com.owncloud.android.lib.common.http.logging.LogInterceptor
import com.owncloud.android.lib.common.utils.LoggingHelper
import com.owncloud.android.utils.CONFIGURATION_REDACT_AUTH_HEADER_LOGS
import timber.log.Timber
import java.io.File

class LogsProvider(
    private val context: Context,
    private val mdmProvider: MdmProvider,
) {
    private val sharedPreferencesProvider = OCSharedPreferencesProvider(context)

    fun startLogging() {
        val dataFolder = MainApp.dataFolder
        val localStorageProvider = ScopedStorageProvider(dataFolder, context)

        // Set folder for store logs
        LoggingHelper.startLogging(
            directory = File(localStorageProvider.getLogsPath()),
            storagePath = dataFolder
        )
        Timber.d("${BuildConfig.BUILD_TYPE} start logging ${BuildConfig.VERSION_NAME} ${BuildConfig.COMMIT_SHA1}")

        initHttpLogs()
    }

    fun stopLogging() {
        LoggingHelper.stopLogging()
    }

    private fun initHttpLogs() {
        val httpLogsEnabled: Boolean = sharedPreferencesProvider.getBoolean(PREFERENCE_LOG_HTTP, false)
        LogInterceptor.httpLogsEnabled = httpLogsEnabled
        val redactAuthHeader = mdmProvider.getBrandingBoolean(mdmKey = CONFIGURATION_REDACT_AUTH_HEADER_LOGS, booleanKey = R.bool.redact_auth_header_logs)
        LogInterceptor.redactAuthHeader = redactAuthHeader
    }

    fun shouldLogHttpRequests(logsEnabled: Boolean) {
        sharedPreferencesProvider.putBoolean(PREFERENCE_LOG_HTTP, logsEnabled)
        LogInterceptor.httpLogsEnabled = logsEnabled
    }

    companion object {
        private const val PREFERENCE_LOG_HTTP = "set_httpLogs"
    }
}
