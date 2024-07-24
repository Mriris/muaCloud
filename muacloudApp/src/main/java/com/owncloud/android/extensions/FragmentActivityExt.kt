

package com.owncloud.android.ui.utils

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.showDialogFragment(
    newFragment: DialogFragment, fragmentTag: String
) {
    val ft = supportFragmentManager.beginTransaction()
    val prev = supportFragmentManager.findFragmentByTag(fragmentTag)

    if (prev != null) {
        ft.remove(prev)
    }
    ft.addToBackStack(null)

    newFragment.show(ft, fragmentTag)
}
