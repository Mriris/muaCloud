
package com.owncloud.android.presentation.migration

sealed class MigrationState {

    object MigrationIntroState : MigrationState()

    data class MigrationChoiceState(
        val legacyStorageSpaceInBytes: Long,
    ) : MigrationState()

    object MigrationProgressState : MigrationState()

    object MigrationCompletedState : MigrationState()
}
