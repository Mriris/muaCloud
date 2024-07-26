

package com.owncloud.android.presentation.settings.logging

import android.content.Intent
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.owncloud.android.R
import com.owncloud.android.presentation.logging.LogsListActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsLogsFragment : PreferenceFragmentCompat() {

    private val logsViewModel by viewModel<SettingsLogsViewModel>()

    private var prefEnableLogging: SwitchPreferenceCompat? = null
    private var prefHttpLogs: CheckBoxPreference? = null
    private var prefLogsListActivity: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_logs, rootKey)

        prefEnableLogging = findPreference(PREFERENCE_ENABLE_LOGGING)
        prefHttpLogs = findPreference(PREFERENCE_LOG_HTTP)
        prefLogsListActivity = findPreference(PREFERENCE_LOGS_LIST)

        with(logsViewModel.isLoggingEnabled()) {
            prefHttpLogs?.isEnabled = this
        }

        prefEnableLogging?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
            val value = newValue as Boolean
            logsViewModel.setEnableLogging(value)

            prefHttpLogs?.isEnabled = value

            if (!value) {

                logsViewModel.shouldLogHttpRequests(value)
                prefHttpLogs?.isChecked = false
            }
            true
        }

        prefHttpLogs?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
            logsViewModel.shouldLogHttpRequests(newValue as Boolean)
            true
        }

        prefLogsListActivity?.let {
            it.setOnPreferenceClickListener {
                val intent = Intent(context, LogsListActivity::class.java)
                startActivity(intent)
                true
            }
        }
    }

    override fun onDestroy() {
        logsViewModel.enqueueOldLogsCollectorWorker()
        super.onDestroy()
    }

    companion object {
        const val PREFERENCE_ENABLE_LOGGING = "enable_logging"
        const val PREFERENCE_LOG_HTTP = "set_httpLogs"
        const val PREFERENCE_LOGS_LIST = "logs_list"
    }
}
