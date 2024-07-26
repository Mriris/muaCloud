

package com.owncloud.android.presentation.settings.advanced

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.owncloud.android.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsAdvancedFragment : PreferenceFragmentCompat() {

    private val advancedViewModel by viewModel<SettingsAdvancedViewModel>()

    private var prefShowHiddenFiles: SwitchPreferenceCompat? = null
    private var prefRemoveLocalFiles: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_advanced, rootKey)

        prefShowHiddenFiles = findPreference(PREF_SHOW_HIDDEN_FILES)
        prefRemoveLocalFiles = findPreference<ListPreference>(PREFERENCE_REMOVE_LOCAL_FILES)?.apply {
            entries = listOf(
                getString(R.string.prefs_delete_local_files_entries_never),
                getString(R.string.prefs_delete_local_files_entries_1hour),
                getString(R.string.prefs_delete_local_files_entries_12hours),
                getString(R.string.prefs_delete_local_files_entries_1day),
                getString(R.string.prefs_delete_local_files_entries_30days)
            ).toTypedArray()
            entryValues = listOf(
                RemoveLocalFiles.NEVER.name,
                RemoveLocalFiles.ONE_HOUR.name,
                RemoveLocalFiles.TWELVE_HOURS.name,
                RemoveLocalFiles.ONE_DAY.name,
                RemoveLocalFiles.THIRTY_DAYS.name,
            ).toTypedArray()
            summary = getString(R.string.prefs_delete_local_files_summary, this.entry)
        }
        initPreferenceListeners()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefShowHiddenFiles?.isChecked = advancedViewModel.isHiddenFilesShown()
    }

    private fun initPreferenceListeners() {
        prefShowHiddenFiles?.setOnPreferenceChangeListener { _: Preference?, newValue: Any ->
            advancedViewModel.setShowHiddenFiles(newValue as Boolean)
            true
        }

        prefRemoveLocalFiles?.setOnPreferenceChangeListener { preference: Preference?, newValue: Any ->
            val index = (preference as ListPreference).findIndexOfValue(newValue as String)
            preference.summary = getString(R.string.prefs_delete_local_files_summary, preference.entries[index])
            advancedViewModel.scheduleDeleteLocalFiles(newValue)
            true
        }
    }

    companion object {
        const val PREF_SHOW_HIDDEN_FILES = "show_hidden_files"
    }
}
