

package com.owncloud.android.extensions

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.Menu
import android.view.MenuItem.SHOW_AS_ACTION_NEVER
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.owncloud.android.R
import com.owncloud.android.domain.appregistry.model.AppRegistryProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun Fragment.showErrorInSnackbar(genericErrorMessageId: Int, throwable: Throwable?) =
    throwable?.let {
        showMessageInSnackbar(it.parseError(getString(genericErrorMessageId), resources))
    }

fun Fragment.showMessageInSnackbar(
    message: CharSequence,
    duration: Int = Snackbar.LENGTH_LONG
) {
    val requiredView = view ?: return
    Snackbar.make(requiredView, message, duration).show()
}

fun Fragment.showAlertDialog(
    title: String,
    message: String,
    positiveButtonText: String = getString(android.R.string.ok),
    positiveButtonListener: ((DialogInterface, Int) -> Unit)? = null,
    negativeButtonText: String = "",
    negativeButtonListener: ((DialogInterface, Int) -> Unit)? = null
) {
    val requiredActivity = activity ?: return
    AlertDialog.Builder(requiredActivity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText, positiveButtonListener)
        .setNegativeButton(negativeButtonText, negativeButtonListener)
        .show()
        .avoidScreenshotsIfNeeded()
}

fun Fragment.hideSoftKeyboard() {
    val focusedView = requireActivity().currentFocus
    focusedView?.let {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            focusedView.windowToken,
            0
        )
    }
}

fun <T> Fragment.collectLatestLifecycleFlow(
    flow: Flow<T>,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    collect: suspend (T) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(lifecycleState) {
            flow.collectLatest(collect)
        }
    }
}

fun Fragment.addOpenInWebMenuOptions(
    menu: Menu,
    openInWebProviders: Map<String, Int> = emptyMap(),
    appRegistryProviders: List<AppRegistryProvider>? = emptyList(),
): Map<String, Int> {
    val newOpenInWebProviders = emptyMap<String, Int>().toMutableMap()

    openInWebProviders.forEach { (_, menuItemId) ->
        menu.removeItem(menuItemId)
    }
    appRegistryProviders?.forEachIndexed { index, appRegistryProvider ->
        menu.add(Menu.NONE, index, 0, getString(R.string.ic_action_open_with_web, appRegistryProvider.name)).also {
            it.setShowAsAction(SHOW_AS_ACTION_NEVER)
            newOpenInWebProviders[appRegistryProvider.name] = it.itemId
        }
    }
    return newOpenInWebProviders
}
