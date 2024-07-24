

package com.owncloud.android.presentation.settings.advanced

import androidx.lifecycle.ViewModel
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.presentation.settings.advanced.SettingsAdvancedFragment.Companion.PREF_SHOW_HIDDEN_FILES
import com.owncloud.android.providers.WorkManagerProvider
import com.owncloud.android.workers.RemoveLocallyFilesWithLastUsageOlderThanGivenTimeWorker.Companion.DELETE_FILES_OLDER_GIVEN_TIME_WORKER

class SettingsAdvancedViewModel(
    private val preferencesProvider: SharedPreferencesProvider,
    private val workManagerProvider: WorkManagerProvider,
) : ViewModel() {

    fun isHiddenFilesShown(): Boolean {
        return preferencesProvider.getBoolean(PREF_SHOW_HIDDEN_FILES, false)
    }

    fun setShowHiddenFiles(hide: Boolean) {
        preferencesProvider.putBoolean(PREF_SHOW_HIDDEN_FILES, hide)
    }

    fun scheduleDeleteLocalFiles(newValue: String) {
        workManagerProvider.cancelAllWorkByTag(DELETE_FILES_OLDER_GIVEN_TIME_WORKER)
        if (newValue != RemoveLocalFiles.NEVER.name) {
            workManagerProvider.enqueueRemoveLocallyFilesWithLastUsageOlderThanGivenTimeWorker()
        }
    }
}
