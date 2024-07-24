

package com.owncloud.android.presentation.avatar

import android.accounts.Account
import android.view.MenuItem
import android.widget.ImageView
import com.owncloud.android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AvatarUtils : KoinComponent {

    private val avatarManager: AvatarManager by inject()


    fun loadAvatarForAccount(
        imageView: ImageView,
        account: Account,
        fetchIfNotCached: Boolean = false,
        displayRadius: Float
    ) {
        //TODO: Tech debt: Move this to a viewModel and use its viewModelScope instead
        CoroutineScope(Dispatchers.IO).launch {
            val drawable = avatarManager.getAvatarForAccount(
                account = account,
                fetchIfNotCached = fetchIfNotCached,
                displayRadius = displayRadius
            )
            withContext(Dispatchers.Main) {
                if (drawable != null) {
                    imageView.setImageDrawable(drawable)
                } else {
                    imageView.setImageResource(R.drawable.ic_account_circle)
                }
            }
        }
    }

    fun loadAvatarForAccount(
        menuItem: MenuItem,
        account: Account,
        fetchIfNotCached: Boolean = false,
        displayRadius: Float
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val drawable = avatarManager.getAvatarForAccount(
                account = account,
                fetchIfNotCached = fetchIfNotCached,
                displayRadius = displayRadius
            )
            withContext(Dispatchers.Main) {
                if (drawable != null) {
                    menuItem.icon = drawable
                } else {
                    menuItem.setIcon(R.drawable.ic_account_circle)
                }
            }
        }
    }
}
