

package com.owncloud.android.presentation.releasenotes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.owncloud.android.BuildConfig
import com.owncloud.android.MainApp
import com.owncloud.android.MainApp.Companion.versionCode
import com.owncloud.android.R
import com.owncloud.android.databinding.ReleaseNotesActivityBinding
import com.owncloud.android.presentation.authentication.LoginActivity
import com.owncloud.android.ui.activity.FileDisplayActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReleaseNotesActivity : AppCompatActivity() {

    // ViewModel
    private val releaseNotesViewModel by viewModel<ReleaseNotesViewModel>()

    private var _binding: ReleaseNotesActivityBinding? = null
    val binding get() = _binding!!

    private val releaseNotesAdapter = ReleaseNotesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ReleaseNotesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setData()
        initView()
    }

    private fun initView() {
        binding.releaseNotes.let {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = releaseNotesAdapter
        }

        binding.btnProceed.setOnClickListener {
            releaseNotesViewModel.updateVersionCode()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setData() {
        releaseNotesAdapter.setData(releaseNotesViewModel.getReleaseNotes())

        val header = String.format(
            getString(R.string.release_notes_header),
            getString(R.string.app_name)
        )

        val footer = String.format(
            getString(R.string.release_notes_footer),
            getString(R.string.app_name)
        )

        binding.txtHeader.text = header
        binding.txtFooter.text = footer
    }

    companion object {
        fun runIfNeeded(context: Context) {
            if (context is ReleaseNotesActivity) {
                return
            }
            if (shouldShow(context)) {
                context.startActivity(Intent(context, ReleaseNotesActivity::class.java))
            }
        }

        private fun shouldShow(context: Context): Boolean {
            val showReleaseNotes = context.resources.getBoolean(R.bool.release_notes_enabled) && !BuildConfig.DEBUG

            return firstRunAfterUpdate() && showReleaseNotes &&
                    ReleaseNotesViewModel.releaseNotesList.isNotEmpty() &&
                    (context is FileDisplayActivity || context is LoginActivity)
        }

        private fun firstRunAfterUpdate(): Boolean {
            return MainApp.getLastSeenVersionCode() != versionCode
        }
    }
}
