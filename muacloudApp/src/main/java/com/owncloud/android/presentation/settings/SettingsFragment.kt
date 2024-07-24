

package com.owncloud.android.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.owncloud.android.BuildConfig
import com.owncloud.android.R
import com.owncloud.android.extensions.openPrivacyPolicy
import com.owncloud.android.extensions.showMessageInSnackbar
import com.owncloud.android.presentation.releasenotes.ReleaseNotesActivity
import com.owncloud.android.presentation.releasenotes.ReleaseNotesViewModel
import com.owncloud.android.presentation.settings.more.SettingsMoreViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat() {

    // ViewModel
    private val settingsViewModel by viewModel<SettingsViewModel>()
    private val moreViewModel by viewModel<SettingsMoreViewModel>()
    private val releaseNotesViewModel by viewModel<ReleaseNotesViewModel>()

    private var settingsScreen: PreferenceScreen? = null
    private var subsectionPictureUploads: Preference? = null
    private var subsectionVideoUploads: Preference? = null
    private var subsectionMore: Preference? = null
    private var prefPrivacyPolicy: Preference? = null
    private var subsectionWhatsNew: Preference? = null
    private var subsectionNotifications: Preference? = null
    private var prefAboutApp: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        settingsScreen = findPreference(SCREEN_SETTINGS)
        subsectionPictureUploads = findPreference(SUBSECTION_PICTURE_UPLOADS)
        subsectionVideoUploads = findPreference(SUBSECTION_VIDEO_UPLOADS)
        subsectionMore = findPreference(SUBSECTION_MORE)
        prefPrivacyPolicy = findPreference(PREFERENCE_PRIVACY_POLICY)
        subsectionWhatsNew = findPreference(SUBSECTION_WHATSNEW)
        subsectionNotifications = findPreference(SUBSECTION_NOTIFICATIONS)
        prefAboutApp = findPreference(PREFERENCE_ABOUT_APP)

        subsectionPictureUploads?.isVisible = settingsViewModel.isThereAttachedAccount()
        subsectionVideoUploads?.isVisible = settingsViewModel.isThereAttachedAccount()
        subsectionMore?.isVisible = moreViewModel.shouldMoreSectionBeVisible()
        subsectionWhatsNew?.isVisible = releaseNotesViewModel.shouldWhatsNewSectionBeVisible()

        if (moreViewModel.isPrivacyPolicyEnabled()) {
            prefPrivacyPolicy?.setOnPreferenceClickListener {
                requireActivity().openPrivacyPolicy()
                true
            }
        } else {
            settingsScreen?.removePreferenceFromScreen(prefPrivacyPolicy)
        }

        subsectionWhatsNew?.setOnPreferenceClickListener {
            val intent = Intent(context, ReleaseNotesActivity::class.java)
            startActivity(intent)
            true
        }

        subsectionWhatsNew?.setOnPreferenceClickListener {
            val intent = Intent(context, ReleaseNotesActivity::class.java)
            startActivity(intent)
            true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            subsectionNotifications?.setOnPreferenceClickListener {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)
                true
            }
        } else {
            settingsScreen.removePreferenceFromScreen(subsectionNotifications)
        }

        prefAboutApp?.apply {
            summary = String.format(
                getString(R.string.prefs_app_version_summary),
                getString(R.string.app_name),
                BuildConfig.BUILD_TYPE,
                BuildConfig.VERSION_NAME,
                BuildConfig.COMMIT_SHA1
            )
            setOnPreferenceClickListener {
                val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("ownCloud app version", summary)
                clipboard.setPrimaryClip(clip)
                showMessageInSnackbar(getString(R.string.clipboard_text_copied))
                true
            }
        }
    }

    companion object {
        private const val SCREEN_SETTINGS = "settings_screen"
        private const val PREFERENCE_PRIVACY_POLICY = "privacyPolicy"
        private const val PREFERENCE_ABOUT_APP = "about_app"
        private const val SUBSECTION_PICTURE_UPLOADS = "picture_uploads_subsection"
        private const val SUBSECTION_VIDEO_UPLOADS = "video_uploads_subsection"
        private const val SUBSECTION_MORE = "more_subsection"
        private const val SUBSECTION_NOTIFICATIONS = "notifications_subsection"

        // Remove preference with nullability check
        fun PreferenceScreen?.removePreferenceFromScreen(preference: Preference?) {
            preference?.let { this?.removePreference(it) }
        }

        private const val SUBSECTION_WHATSNEW = "whatsNew"
    }
}
