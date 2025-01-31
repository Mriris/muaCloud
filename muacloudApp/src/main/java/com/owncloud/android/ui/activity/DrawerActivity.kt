

package com.owncloud.android.ui.activity

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerFuture
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.owncloud.android.R
import com.owncloud.android.domain.capabilities.model.OCCapability
import com.owncloud.android.domain.files.model.FileListOption
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.extensions.goToUrl
import com.owncloud.android.extensions.openPrivacyPolicy
import com.owncloud.android.extensions.sendEmailOrOpenFeedbackDialogAction
import com.owncloud.android.lib.common.OwnCloudAccount
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.presentation.avatar.AvatarUtils
import com.owncloud.android.presentation.capabilities.CapabilityViewModel
import com.owncloud.android.presentation.common.DrawerViewModel
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.presentation.settings.SettingsActivity
import com.owncloud.android.utils.DisplayUtils
import com.owncloud.android.utils.PreferenceUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import kotlin.math.ceil


abstract class DrawerActivity : ToolbarActivity() {

    private val drawerViewModel by viewModel<DrawerViewModel>()
    private val capabilitiesViewModel by viewModel<CapabilityViewModel> {
        parametersOf(
            account?.name
        )
    }

    private var currentAccountAvatarRadiusDimension = 0f

    private var drawerToggle: ActionBarDrawerToggle? = null

    private var checkedMenuItem = Menu.NONE

    
    protected open fun setupDrawer() {

        getDrawerLayout()?.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(this)
        getNavView()?.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(this)

        if (resources.getBoolean(R.bool.use_drawer_background_header)) {
            getDrawerHeaderBackground()?.setImageResource(R.drawable.drawer_header_background)
        }

        if (resources.getBoolean(R.bool.use_drawer_logo)) {
            if (isDrawerLinkEnabled()) {
                getDrawerLinkIcon()?.apply {
                    isVisible = true
                    setOnClickListener { openDrawerLink() }
                }
                getDrawerLinkText()?.apply {
                    isVisible = true
                    setOnClickListener { openDrawerLink() }
                }
            } else {
                getDrawerLogo()?.setImageResource(R.drawable.drawer_logo)
            }
        }

        getNavView()?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    v.rootWindowInsets.displayCutout?.let {
                        getDrawerActiveUser()?.layoutParams?.height =
                            DisplayUtils.getDrawerHeaderHeight(it.safeInsetTop, resources)
                    }
                }
            }

            override fun onViewDetachedFromWindow(v: View) {}
        })
        setupDrawerContent()

        drawerToggle =
            object : ActionBarDrawerToggle(this, getDrawerLayout(), R.string.drawer_open, R.string.drawer_close) {
                                override fun onDrawerClosed(view: View) {
                    super.onDrawerClosed(view)
                    drawerToggle?.isDrawerIndicatorEnabled = false
                    invalidateOptionsMenu()
                }

                                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                    drawerToggle?.isDrawerIndicatorEnabled = true
                    invalidateOptionsMenu()
                }
            }

        getDrawerLayout()?.addDrawerListener(drawerToggle as ActionBarDrawerToggle)
        drawerToggle?.isDrawerIndicatorEnabled = false
    }

    
    private fun setupDrawerContent() {
        val navigationView: NavigationView = getNavView() ?: return

        if (!resources.getBoolean(R.bool.help_enabled)) {
            navigationView.menu.removeItem(R.id.drawer_menu_help)
        }
        if (!resources.getBoolean(R.bool.feedback_enabled)) {
            navigationView.menu.removeItem(R.id.drawer_menu_feedback)
        }
        if (!resources.getBoolean(R.bool.privacy_policy_enabled)) {
            navigationView.menu.removeItem(R.id.drawer_menu_privacy_policy)
        }
        navigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            getDrawerLayout()?.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    val settingsIntent = Intent(applicationContext, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                }
                R.id.drawer_menu_feedback -> openFeedback()
                R.id.drawer_menu_help -> openHelp()
                R.id.drawer_menu_privacy_policy -> openPrivacyPolicy()
                else -> Timber.i("Unknown drawer menu item clicked: %s", menuItem.title)
            }
            true
        }
    }

    fun setCheckedItemAtBottomBar(checkedMenuItem: Int) {
        getBottomNavigationView()?.menu?.findItem(checkedMenuItem)?.isChecked = true
    }

    
    open fun setupNavigationBottomBar(menuItemId: Int) {

        getBottomNavigationView()?.filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(this)
        if (account != null) {
            capabilitiesViewModel.capabilities.observe(this) { event: Event<UIResult<OCCapability>> ->
                setSpacesVisibilityBottomBar(event.peekContent())
            }
        }
        setCheckedItemAtBottomBar(menuItemId)
        getBottomNavigationView()?.setOnNavigationItemSelectedListener { menuItem: MenuItem ->
            bottomBarNavigationTo(menuItem.itemId, getBottomNavigationView()?.selectedItemId == menuItem.itemId)
            true
        }
    }

    private fun setSpacesVisibilityBottomBar(uiResult: UIResult<OCCapability>) {
        if (uiResult is UIResult.Success) {
            val capabilities = uiResult.data
            if (AccountUtils.isSpacesFeatureAllowedForAccount(baseContext, account, capabilities)) {
                getBottomNavigationView()?.menu?.get(0)?.title = getString(R.string.bottom_nav_personal)
                getBottomNavigationView()?.menu?.get(1)?.title = getString(R.string.bottom_nav_shares)
                getBottomNavigationView()?.menu?.get(1)?.icon = AppCompatResources.getDrawable(this, R.drawable.ic_ocis_shares)
                getBottomNavigationView()?.menu?.get(2)?.isVisible = capabilities?.isSpacesProjectsAllowed() == true
            } else {
                getBottomNavigationView()?.menu?.get(0)?.title = getString(R.string.bottom_nav_files)
                getBottomNavigationView()?.menu?.get(2)?.isVisible = false
            }
        }
    }

    private fun bottomBarNavigationTo(menuItemId: Int, isCurrentOptionActive: Boolean) {
        when (menuItemId) {
            R.id.nav_all_files -> navigateToOption(FileListOption.ALL_FILES)
            R.id.nav_spaces -> navigateToOption(FileListOption.SPACES_LIST)
            R.id.nav_uploads -> if (!isCurrentOptionActive) {
                val uploadListIntent = Intent(applicationContext, UploadListActivity::class.java)
                uploadListIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(uploadListIntent)
            }

            R.id.nav_available_offline_files -> navigateToOption(FileListOption.AV_OFFLINE)
            R.id.nav_shared_by_link_files -> navigateToOption(FileListOption.SHARED_BY_LINK)
        }
    }

    private fun openHelp() {
        goToUrl(url = getString(R.string.url_help))
    }

    private fun openFeedback() {
        sendEmailOrOpenFeedbackDialogAction(drawerViewModel.getFeedbackMail())
    }

    private fun openDrawerLink() {
        goToUrl(url = resources.getString(R.string.drawer_link))
    }

    private fun isDrawerLinkEnabled() =
        resources.getString(R.string.drawer_link_label).isNotBlank() && resources.getString(R.string.drawer_link).isNotBlank()

    
    open fun isDrawerOpen(): Boolean = getDrawerLayout()?.isDrawerOpen(GravityCompat.START) ?: false

    
    open fun closeDrawer() {
        getDrawerLayout()?.closeDrawer(GravityCompat.START)
    }

    
    open fun openDrawer() {
        getDrawerLayout()?.openDrawer(GravityCompat.START)
    }

    
    open fun setDrawerLockMode(lockMode: Int) {
        getDrawerLayout()?.setDrawerLockMode(lockMode)
    }

    
    private fun updateQuota() {
        Timber.d("Update Quota")
        val account = drawerViewModel.getCurrentAccount(this) ?: return
        drawerViewModel.getStoredQuota(account.name)
        drawerViewModel.userQuota.observe(this) { event ->
            when (val uiResult = event.peekContent()) {
                is UIResult.Success -> {
                    uiResult.data?.let { userQuota ->
                        when {
                            userQuota.available < 0 -> { // Pending, unknown or unlimited free storage
                                getAccountQuotaBar()?.run {
                                    isVisible = true
                                    progress = 0
                                }
                                getAccountQuotaText()?.text = String.format(
                                    getString(R.string.drawer_unavailable_free_storage),
                                    DisplayUtils.bytesToHumanReadable(userQuota.used, this)
                                )

                            }

                            userQuota.available == 0L -> { // Quota 0, guest users
                                getAccountQuotaBar()?.isVisible = false
                                getAccountQuotaText()?.text = getString(R.string.drawer_unavailable_used_storage)
                            }

                            else -> { // Limited quota

                                getAccountQuotaBar()?.run {
                                    progress = ceil(userQuota.getRelative()).toInt()
                                    isVisible = true
                                }
                                getAccountQuotaText()?.text = String.format(
                                    getString(R.string.drawer_quota),
                                    DisplayUtils.bytesToHumanReadable(userQuota.used, this),
                                    DisplayUtils.bytesToHumanReadable(userQuota.getTotal(), this),
                                    userQuota.getRelative()
                                )
                            }
                        }
                    }
                }

                is UIResult.Loading -> getAccountQuotaText()?.text = getString(R.string.drawer_loading_quota)
                is UIResult.Error -> getAccountQuotaText()?.text = getString(R.string.drawer_unavailable_used_storage)
            }
        }
    }

    override fun setupRootToolbar(title: String, isSearchEnabled: Boolean, isAvatarRequested: Boolean) {
        super.setupRootToolbar(
            title = title,
            isSearchEnabled = isSearchEnabled,
            isAvatarRequested = isAvatarRequested,
        )

        val toolbarLeftIcon = findViewById<ImageView>(R.id.root_toolbar_left_icon)
        toolbarLeftIcon.setOnClickListener { openDrawer() }
    }

    
    protected fun setAccountInDrawer(account: Account) {
        if (getDrawerLayout() != null) {
            getDrawerUserNameFull()?.text = account.name
            try {
                val ocAccount = OwnCloudAccount(account, this)
                getDrawerUserName()?.text = ocAccount.displayName
            } catch (e: Exception) {
                Timber.w("Couldn't read display name of account; using account name instead")
                getDrawerUserName()?.text = drawerViewModel.getUsernameOfAccount(account.name)
            }

            getDrawerCurrentAccount()?.let {
                AvatarUtils().loadAvatarForAccount(
                    imageView = it,
                    account = account,
                    displayRadius = currentAccountAvatarRadiusDimension
                )
                updateQuota()
            }
        }
    }

    
    protected open fun setDrawerMenuItemChecked(menuItemId: Int) {
        val navigationView = getNavView()
        if (navigationView != null && navigationView.menu.findItem(menuItemId) != null) {
            navigationView.menu.findItem(menuItemId).isChecked = true
            checkedMenuItem = menuItemId
        } else {
            Timber.w("setDrawerMenuItemChecked has been called with invalid menu-item-ID")
        }
    }

    private fun setOnAccountsUpdatedListener() {
        val accountManager = AccountManager.get(this)
        accountManager.addOnAccountsUpdatedListener({
            drawerViewModel.removeAccount(this)

            val authority = getString(R.string.document_provider_authority)
            val rootsUri = DocumentsContract.buildRootsUri(authority)
            contentResolver.notifyChange(rootsUri, null)

            if (drawerViewModel.getAccounts(this).isEmpty()) {
                mAccountWasSet = false
            }
        }, Handler(), false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            checkedMenuItem = savedInstanceState.getInt(KEY_CHECKED_MENU_ITEM, Menu.NONE)
        }

        currentAccountAvatarRadiusDimension = resources.getDimension(R.dimen.nav_drawer_header_avatar_radius)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CHECKED_MENU_ITEM, checkedMenuItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        checkedMenuItem = savedInstanceState.getInt(KEY_CHECKED_MENU_ITEM, Menu.NONE)

        if (checkedMenuItem != Menu.NONE) {
            setDrawerMenuItemChecked(checkedMenuItem)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        drawerToggle?.let {
            it.syncState()
            if (isDrawerOpen()) {
                it.isDrawerIndicatorEnabled = true
            }
        }
        updateQuota()
        setOnAccountsUpdatedListener()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        if (isDrawerOpen()) {
            closeDrawer()
            return
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        setDrawerMenuItemChecked(checkedMenuItem)
    }

    override fun onAccountCreationSuccessful(future: AccountManagerFuture<Bundle?>?) {
        super.onAccountCreationSuccessful(future)
        updateQuota()
        restart()
    }

    private fun getDrawerLayout(): DrawerLayout? = findViewById(R.id.drawer_layout)
    private fun getNavView(): NavigationView? = findViewById(R.id.nav_view)
    private fun getBottomNavigationView(): BottomNavigationView? = findViewById(R.id.bottom_nav_view)
    private fun getDrawerLogo(): AppCompatImageView? = findViewById(R.id.drawer_logo)
    private fun getDrawerLinkIcon(): ImageView? = findViewById(R.id.drawer_link_icon)
    private fun getDrawerLinkText(): TextView? = findViewById(R.id.drawer_link_text)
    private fun getAccountQuotaText(): TextView? = findViewById(R.id.account_quota_text)
    private fun getAccountQuotaBar(): ProgressBar? = findViewById(R.id.account_quota_bar)
    private fun getDrawerActiveUser() = findNavigationViewChildById(R.id.drawer_active_user) as ConstraintLayout?
    private fun getDrawerCurrentAccount() = findNavigationViewChildById(R.id.drawer_current_account) as AppCompatImageView?
    private fun getDrawerHeaderBackground() = findNavigationViewChildById(R.id.drawer_header_background) as ImageView?
    private fun getDrawerUserName(): TextView? = findNavigationViewChildById(R.id.drawer_username) as TextView?
    private fun getDrawerUserNameFull(): TextView? = findNavigationViewChildById(R.id.drawer_username_full) as TextView?

    
    private fun findNavigationViewChildById(id: Int): View {
        return (findViewById<View>(R.id.nav_view) as NavigationView).getHeaderView(0).findViewById(id)
    }

    
    open fun addDrawerListener(listener: DrawerListener) {
        getDrawerLayout()?.addDrawerListener(listener)
    }

    abstract fun navigateToOption(fileListOption: FileListOption)

    
    protected abstract fun restart()

    companion object {
        const val CENTRAL_URL = "https://central.owncloud.org/"
        const val TALK_MOBILE_URL = "https://talk.owncloud.com/channel/mobile"
        const val GITHUB_URL = "https://github.com/owncloud/android/issues/new/choose"
        const val SURVEY_URL = "https://owncloud.com/android-app-feedback"
        private const val KEY_IS_ACCOUNT_CHOOSER_ACTIVE = "IS_ACCOUNT_CHOOSER_ACTIVE"
        private const val KEY_CHECKED_MENU_ITEM = "CHECKED_MENU_ITEM"
    }
}
