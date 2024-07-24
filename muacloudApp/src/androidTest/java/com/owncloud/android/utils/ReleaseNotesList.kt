

package com.owncloud.android.utils

import com.owncloud.android.R
import com.owncloud.android.presentation.releasenotes.ReleaseNote
import com.owncloud.android.presentation.releasenotes.ReleaseNoteType

val releaseNotesList = listOf(
    ReleaseNote(
        title = R.string.release_notes_header,
        subtitle = R.string.release_notes_footer,
        type = ReleaseNoteType.BUGFIX
    ),
    ReleaseNote(
        title = R.string.release_notes_header,
        subtitle = R.string.release_notes_footer,
        type = ReleaseNoteType.BUGFIX
    ),
    ReleaseNote(
        title = R.string.release_notes_header,
        subtitle = R.string.release_notes_footer,
        type = ReleaseNoteType.ENHANCEMENT
    )
)
