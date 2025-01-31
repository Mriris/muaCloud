

package com.owncloud.android.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.owncloud.android.R
import com.owncloud.android.presentation.settings.advanced.SettingsAdvancedFragment
import com.owncloud.android.presentation.settings.autouploads.SettingsPictureUploadsFragment
import com.owncloud.android.presentation.settings.autouploads.SettingsVideoUploadsFragment
import com.owncloud.android.presentation.settings.logging.SettingsLogsFragment
import com.owncloud.android.presentation.settings.more.SettingsMoreFragment
import com.owncloud.android.presentation.settings.security.SettingsSecurityFragment
import com.owncloud.android.ui.activity.FileDisplayActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.standard_toolbar).apply {
            isVisible = true
        }
        findViewById<ConstraintLayout>(R.id.root_toolbar).apply {
            isVisible = false
        }
        setSupportActionBar(toolbar)
        updateToolbarTitle()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.addOnBackStackChangedListener { updateToolbarTitle() }

        if (savedInstanceState != null) return

        redirectToSubsection(intent)
    }

    private fun updateToolbarTitle() {
        val titleId = when (supportFragmentManager.fragments.lastOrNull()) {
            is SettingsSecurityFragment -> R.string.prefs_subsection_security
            is SettingsLogsFragment -> R.string.prefs_subsection_logging
            is SettingsPictureUploadsFragment -> R.string.prefs_subsection_picture_uploads
            is SettingsVideoUploadsFragment -> R.string.prefs_subsection_video_uploads
            is SettingsAdvancedFragment -> R.string.prefs_subsection_advanced
            is SettingsMoreFragment -> R.string.prefs_subsection_more
            else -> R.string.actionbar_settings
        }

        supportActionBar?.setTitle(titleId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    intent = Intent(this, FileDisplayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun redirectToSubsection(intent: Intent?) {
        val fragment = when (intent?.getStringExtra(KEY_NOTIFICATION_INTENT)) {
            NOTIFICATION_INTENT_PICTURES -> SettingsPictureUploadsFragment()
            NOTIFICATION_INTENT_VIDEOS -> SettingsVideoUploadsFragment()
            else -> SettingsFragment()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, fragment)
            .commit()
    }

    companion object {
        const val KEY_NOTIFICATION_INTENT = "key_notification_intent"
        const val NOTIFICATION_INTENT_PICTURES = "picture_uploads"
        const val NOTIFICATION_INTENT_VIDEOS = "video_uploads"
    }
}
