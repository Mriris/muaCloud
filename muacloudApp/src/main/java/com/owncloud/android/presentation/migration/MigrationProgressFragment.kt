
package com.owncloud.android.presentation.migration

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.owncloud.android.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MigrationProgressFragment : Fragment(R.layout.fragment_migration_progress) {

    private val migrationViewModel: MigrationViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.migration_progress_button)?.setOnClickListener {
            migrationViewModel.moveToNextState()
        }

        migrationViewModel.moveLegacyStorageToScopedStorage()
    }
}
