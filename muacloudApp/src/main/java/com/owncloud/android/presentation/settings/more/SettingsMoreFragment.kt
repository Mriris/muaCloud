

package com.owncloud.android.presentation.settings.more

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.owncloud.android.R
import com.owncloud.android.extensions.goToUrl
import com.owncloud.android.extensions.sendEmail
import com.owncloud.android.extensions.sendEmailOrOpenFeedbackDialogAction
import com.owncloud.android.presentation.settings.SettingsFragment.Companion.removePreferenceFromScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsMoreFragment : PreferenceFragmentCompat() {

    private val moreViewModel by viewModel<SettingsMoreViewModel>()

    private var moreScreen: PreferenceScreen? = null
    private var prefHelp: Preference? = null
    private var prefSync: Preference? = null
    private var prefAccessDocProvider: Preference? = null
    private var prefRecommend: Preference? = null
    private var prefFeedback: Preference? = null
    private var prefImprint: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_more, rootKey)

        moreScreen = findPreference(SCREEN_MORE)
        prefHelp = findPreference(PREFERENCE_HELP)
        prefSync = findPreference(PREFERENCE_SYNC_CALENDAR_CONTACTS)
        prefAccessDocProvider = findPreference(PREFERENCE_ACCESS_DOCUMENT_PROVIDER)
        prefRecommend = findPreference(PREFERENCE_RECOMMEND)
        prefFeedback = findPreference(PREFERENCE_FEEDBACK)
        prefImprint = findPreference(PREFERENCE_IMPRINT)

        if (moreViewModel.isHelpEnabled()) {
            prefHelp?.setOnPreferenceClickListener {
                val helpUrl = moreViewModel.getHelpUrl()
                requireActivity().goToUrl(helpUrl)
                true
            }
        } else {
            moreScreen?.removePreferenceFromScreen(prefHelp)
        }

        if (moreViewModel.isSyncEnabled()) {
            prefSync?.setOnPreferenceClickListener {
                val syncUrl = moreViewModel.getSyncUrl()
                requireActivity().goToUrl(syncUrl)
                true
            }
        } else {
            moreScreen?.removePreferenceFromScreen(prefSync)
        }

        if (moreViewModel.isDocProviderAppEnabled()) {
            prefAccessDocProvider?.setOnPreferenceClickListener {
                val docProviderAppUrl = moreViewModel.getDocProviderAppUrl()
                requireActivity().goToUrl(docProviderAppUrl)
                true
            }
        } else {
            moreScreen?.removePreferenceFromScreen(prefAccessDocProvider)
        }

        if (moreViewModel.isRecommendEnabled()) {
            prefRecommend?.setOnPreferenceClickListener {
                val appName = getString(R.string.app_name)
                val downloadUrl = getString(R.string.url_app_download)

                val recommendEmail = getString(R.string.mail_recommend)
                val recommendSubject = String.format(getString(R.string.recommend_subject), appName)
                val recommendText = String.format(getString(R.string.recommend_text), appName, downloadUrl)

                requireActivity().sendEmail(email = recommendEmail, subject = recommendSubject, text = recommendText)
                true
            }
        } else {
            moreScreen?.removePreferenceFromScreen(prefRecommend)
        }

        if (moreViewModel.isFeedbackEnabled()) {
            prefFeedback?.setOnPreferenceClickListener {
                requireActivity().sendEmailOrOpenFeedbackDialogAction(moreViewModel.getFeedbackMail())
                true
            }
        } else {
            moreScreen?.removePreferenceFromScreen(prefFeedback)
        }

        if (moreViewModel.isImprintEnabled()) {
            prefImprint?.setOnPreferenceClickListener {
                val imprintUrl = moreViewModel.getImprintUrl()
                requireActivity().goToUrl(imprintUrl)
                true
            }
        } else {
            moreScreen?.removePreferenceFromScreen(prefImprint)
        }
    }

    companion object {
        private const val SCREEN_MORE = "more_screen"
        private const val PREFERENCE_HELP = "help"
        private const val PREFERENCE_SYNC_CALENDAR_CONTACTS = "syncCalendarContacts"
        private const val PREFERENCE_ACCESS_DOCUMENT_PROVIDER = "accessDocumentProvider"
        private const val PREFERENCE_RECOMMEND = "recommend"
        private const val PREFERENCE_FEEDBACK = "feedback"
        private const val PREFERENCE_IMPRINT = "imprint"
    }

}
