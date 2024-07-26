
package com.owncloud.android.testing

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import com.owncloud.android.R
import com.owncloud.android.ui.activity.BaseActivity


open class SingleFragmentActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = R.id.container
        }
        setContentView(content)
    }
}
