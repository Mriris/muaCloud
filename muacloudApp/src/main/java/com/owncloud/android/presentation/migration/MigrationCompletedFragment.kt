
package com.owncloud.android.presentation.migration

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.owncloud.android.R

class MigrationCompletedFragment : Fragment(R.layout.fragment_migration_completed) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.migration_completed_button)?.setOnClickListener {
            activity?.finish()
        }
    }
}
