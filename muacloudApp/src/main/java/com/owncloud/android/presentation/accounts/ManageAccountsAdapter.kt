

package com.owncloud.android.presentation.accounts

import android.accounts.Account
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.owncloud.android.R
import com.owncloud.android.databinding.AccountActionBinding
import com.owncloud.android.databinding.AccountItemBinding
import com.owncloud.android.lib.common.OwnCloudAccount
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.presentation.avatar.AvatarUtils
import com.owncloud.android.utils.DisplayUtils
import com.owncloud.android.utils.PreferenceUtils
import timber.log.Timber

class ManageAccountsAdapter(private val accountListener: AccountAdapterListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var accountItemsList = listOf<AccountRecyclerItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == AccountManagementRecyclerItemViewType.ITEM_VIEW_ACCOUNT.ordinal) {
            val view = inflater.inflate(R.layout.account_item, parent, false)
            view.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(parent.context)
            AccountManagementViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.account_action, parent, false)
            view.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(parent.context)
            NewAccountViewHolder(view)
        }

    }

    fun submitAccountList(accountList: List<AccountRecyclerItem>) {
        accountItemsList = accountList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AccountManagementViewHolder -> {
                val accountItem = getItem(position) as AccountRecyclerItem.AccountItem
                val account: Account = accountItem.account
                val accountAvatarRadiusDimension = holder.itemView.context.resources.getDimension(R.dimen.list_item_avatar_icon_radius)

                try {
                    val oca = OwnCloudAccount(account, holder.itemView.context)
                    holder.binding.name.text = oca.displayName
                } catch (e: Exception) {
                    Timber.w(
                        "Account not found right after being read :\\ ; using account name instead of display name"
                    )
                    holder.binding.name.text = AccountUtils.getUsernameOfAccount(account.name)
                }
                holder.binding.name.tag = account.name

                holder.binding.account.text = DisplayUtils.convertIdn(account.name, false)

                try {
                    val avatarUtils = AvatarUtils()
                    avatarUtils.loadAvatarForAccount(
                        holder.binding.icon,
                        account,
                        true,
                        accountAvatarRadiusDimension
                    )
                } catch (e: java.lang.Exception) {
                    Timber.e(e, "Error calculating RGB value for account list item.")

                    holder.binding.icon.setImageResource(R.drawable.ic_user)
                }

                if (AccountUtils.getCurrentOwnCloudAccount(holder.itemView.context).name == account.name) {
                    holder.binding.ticker.visibility = View.VISIBLE
                } else {
                    holder.binding.ticker.visibility = View.INVISIBLE
                }

                holder.binding.cleanAccountLocalStorageButton.apply {
                    setImageResource(R.drawable.ic_clean_account)
                    setOnClickListener { accountListener.cleanAccountLocalStorage(account) }
                }

                holder.binding.removeButton.apply {
                    setImageResource(R.drawable.ic_action_delete_grey)
                    setOnClickListener { accountListener.removeAccount(account) }
                }

                holder.itemView.apply {
                    setOnClickListener { accountListener.switchAccount(position) }
                }
            }
            is NewAccountViewHolder -> {
                holder.binding.icon.setImageResource(R.drawable.ic_account_plus)
                holder.binding.name.setText(R.string.prefs_add_account)

                holder.binding.constraintLayoutAction.setOnClickListener {
                    accountListener.createAccount()
                }
            }
        }

    }

    override fun getItemCount(): Int = accountItemsList.size

    fun getItem(position: Int) = accountItemsList[position]

    sealed class AccountRecyclerItem {
        data class AccountItem(val account: Account) : AccountRecyclerItem()
        object NewAccount : AccountRecyclerItem()
    }

    class AccountManagementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = AccountItemBinding.bind(itemView)
    }

    class NewAccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = AccountActionBinding.bind(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AccountRecyclerItem.AccountItem -> AccountManagementRecyclerItemViewType.ITEM_VIEW_ACCOUNT.ordinal
            is AccountRecyclerItem.NewAccount -> AccountManagementRecyclerItemViewType.ITEM_VIEW_ADD.ordinal
        }
    }

    enum class AccountManagementRecyclerItemViewType {
        ITEM_VIEW_ACCOUNT, ITEM_VIEW_ADD
    }


    interface AccountAdapterListener {
        fun removeAccount(account: Account)
        fun cleanAccountLocalStorage(account: Account)
        fun createAccount()
        fun switchAccount(position: Int)
    }
}
