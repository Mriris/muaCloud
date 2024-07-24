

package com.owncloud.android.presentation.settings.logging

import androidx.lifecycle.ViewModel
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.providers.LogsProvider
import com.owncloud.android.providers.WorkManagerProvider

class SettingsLogsViewModel(
    private val preferencesProvider: SharedPreferencesProvider,
    private val logsProvider: LogsProvider,
    private val workManagerProvider: WorkManagerProvider,
) : ViewModel() {

    fun shouldLogHttpRequests(value: Boolean) = logsProvider.shouldLogHttpRequests(value)

    fun setEnableLogging(value: Boolean) {
        preferencesProvider.putBoolean(SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING, value)
        if (value) {
            logsProvider.startLogging()
        } else {
            logsProvider.stopLogging()
        }
    }

    fun isLoggingEnabled() = preferencesProvider.getBoolean(SettingsLogsFragment.PREFERENCE_ENABLE_LOGGING, false)

    fun enqueueOldLogsCollectorWorker() {
        workManagerProvider.enqueueOldLogsCollectorWorker()
    }
}
