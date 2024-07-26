

package com.owncloud.android.presentation.settings.privacypolicy

import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.owncloud.android.R
import com.owncloud.android.extensions.showMessageInSnackbar
import com.owncloud.android.utils.PreferenceUtils


class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        val toolbar = findViewById<Toolbar>(R.id.standard_toolbar).apply {
            setTitle(R.string.actionbar_privacy_policy)
            isVisible = true
        }
        findViewById<ConstraintLayout>(R.id.root_toolbar).isVisible = false

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val progressBar = findViewById<ProgressBar>(R.id.syncProgressBar)

        findViewById<LinearLayout>(R.id.activityPrivacyPolicyLayout).apply {
            filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(this@PrivacyPolicyActivity)
        }

        findViewById<WebView>(R.id.privacyPolicyWebview).apply {
            settings.javaScriptEnabled = true

            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, progress: Int) {
                    progressBar.progress = progress //Set the web page loading progress
                    if (progress == 100) {
                        progressBar.isVisible = false
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                    showMessageInSnackbar(message = getString(R.string.privacy_policy_error) + description)
                }
            }

            val urlPrivacyPolicy = resources.getString(R.string.url_privacy_policy)
            loadUrl(urlPrivacyPolicy)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var retval = true
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> retval = super.onOptionsItemSelected(item)
        }
        return retval
    }

}
