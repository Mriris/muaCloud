

package com.owncloud.android.presentation.releasenotes

import androidx.lifecycle.ViewModel
import com.owncloud.android.MainApp
import com.owncloud.android.MainApp.Companion.versionCode
import com.owncloud.android.R
import com.owncloud.android.data.providers.SharedPreferencesProvider
import com.owncloud.android.providers.ContextProvider

class ReleaseNotesViewModel(
    private val preferencesProvider: SharedPreferencesProvider,
    private val contextProvider: ContextProvider
) : ViewModel() {

    fun getReleaseNotes(): List<ReleaseNote> {
        return releaseNotesList
    }

    fun updateVersionCode() {
        preferencesProvider.putInt(MainApp.PREFERENCE_KEY_LAST_SEEN_VERSION_CODE, versionCode)
    }

    fun shouldWhatsNewSectionBeVisible(): Boolean {
        return contextProvider.getBoolean(R.bool.release_notes_enabled) && getReleaseNotes().isNotEmpty()
    }

    companion object {
        val releaseNotesList = listOf(
            ReleaseNote(
                title = R.string.release_notes_4_3_0_title_accessibility_improvements,
                subtitle = R.string.release_notes_4_3_0_subtitle_accessibility_improvements,
                type = ReleaseNoteType.ENHANCEMENT
            ),
            ReleaseNote(
                title = R.string.release_notes_bugfixes_title,
                subtitle = R.string.release_notes_bugfixes_subtitle,
                type = ReleaseNoteType.BUGFIX
            ),
        )
    }
}
