

package com.owncloud.android.ui.activity

import android.view.Menu
import android.view.View
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.owncloud.android.R
import com.owncloud.android.presentation.accounts.ManageAccountsDialogFragment
import com.owncloud.android.presentation.accounts.ManageAccountsDialogFragment.Companion.MANAGE_ACCOUNTS_DIALOG
import com.owncloud.android.presentation.authentication.AccountUtils
import com.owncloud.android.presentation.avatar.AvatarUtils


abstract class ToolbarActivity : BaseActivity() {

    // 设置标准工具栏
    open fun setupStandardToolbar(
        title: String?,
        displayHomeAsUpEnabled: Boolean,
        homeButtonEnabled: Boolean,
        displayShowTitleEnabled: Boolean
    ) {
        useStandardToolbar(true) // 使用标准工具栏

        val standardToolbar = getStandardToolbar()

        title?.let { standardToolbar.title = it } // 设置工具栏标题
        setSupportActionBar(standardToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled) // 显示返回按钮
        supportActionBar?.setHomeButtonEnabled(homeButtonEnabled) // 启用返回按钮
        supportActionBar?.setDisplayShowTitleEnabled(displayShowTitleEnabled) // 显示标题
    }

    // 设置根工具栏
    open fun setupRootToolbar(
        title: String,
        isSearchEnabled: Boolean,
        isAvatarRequested: Boolean = false,
    ) {
        useStandardToolbar(false) // 使用根工具栏

        val toolbarTitle = findViewById<TextView>(R.id.root_toolbar_title)
        val searchView = findViewById<SearchView>(R.id.root_toolbar_search_view)
        val avatarView = findViewById<ImageView>(R.id.root_toolbar_avatar)

        toolbarTitle.apply {
            isVisible = true
            text = title
            if (isSearchEnabled) {
                setOnClickListener {
                    toolbarTitle.isVisible = false
                    searchView.isVisible = true
                    searchView.isIconified = false
                }
                toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_search, 0)
            } else {
                setOnClickListener(null)
                toolbarTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }

        searchView.apply {
            isVisible = false
            setOnCloseListener {
                searchView.visibility = View.GONE
                toolbarTitle.visibility = VISIBLE
                false
            }
            val textSearchView = findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            val closeButton = findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            textSearchView.setHintTextColor(ContextCompat.getColor(applicationContext, R.color.search_view_hint_text))
            closeButton.setColorFilter(ContextCompat.getColor(applicationContext, R.color.white))
        }

        // 检查当前账户并加载头像
        AccountUtils.getCurrentOwnCloudAccount(baseContext) ?: return
        if (isAvatarRequested) {
            AvatarUtils().loadAvatarForAccount(
                avatarView,
                AccountUtils.getCurrentOwnCloudAccount(baseContext),
                true,
                baseContext.resources.getDimension(R.dimen.toolbar_avatar_radius)
            )
        }
        avatarView.setOnClickListener {
            val dialog = ManageAccountsDialogFragment.newInstance(AccountUtils.getCurrentOwnCloudAccount(applicationContext))
            dialog.show(supportFragmentManager, MANAGE_ACCOUNTS_DIALOG)
        }
    }

    // 设置工具栏类型
    private fun useStandardToolbar(isToolbarStandard: Boolean) {
        getRootToolbar().isVisible = !isToolbarStandard // 切换根工具栏可见性
        getStandardToolbar().isVisible = isToolbarStandard // 切换标准工具栏可见性
    }

    // 更新标准工具栏
    open fun updateStandardToolbar(
        title: String = getString(R.string.default_display_name_for_root_folder),
        displayHomeAsUpEnabled: Boolean = true,
        homeButtonEnabled: Boolean = true
    ) {
        if (getStandardToolbar().isVisible) {
            supportActionBar?.title = title
            supportActionBar?.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled)
            supportActionBar?.setHomeButtonEnabled(homeButtonEnabled)
        } else {
            setupStandardToolbar(title, displayHomeAsUpEnabled, displayHomeAsUpEnabled, true)
        }
    }

    // 获取根工具栏
    private fun getRootToolbar(): ConstraintLayout = findViewById(R.id.root_toolbar)

    // 获取标准工具栏
    private fun getStandardToolbar(): Toolbar = findViewById(R.id.standard_toolbar)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        (menu.findItem(R.id.action_search).actionView as SearchView).run {
            val searchText = findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            val closeButton = findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            val searchButton = findViewById<ImageView>(androidx.appcompat.R.id.search_button)

            maxWidth = Int.MAX_VALUE

            searchButton.setBackgroundColor(getColor(R.color.actionbar_start_color))
            searchText.setHintTextColor(getColor(R.color.search_view_hint_text))
            closeButton.setColorFilter(getColor(R.color.white))
            background = getDrawable(R.drawable.rounded_search_view)
        }
        return true
    }
}

