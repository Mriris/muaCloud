
package com.owncloud.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.owncloud.android.presentation.settings.security.SettingsSecurityFragment;

public class PreferenceUtils {
    public static boolean shouldDisallowTouchesWithOtherVisibleWindows(Context context) {
        SharedPreferences appPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return !appPrefs.getBoolean(SettingsSecurityFragment.PREFERENCE_TOUCHES_WITH_OTHER_VISIBLE_WINDOWS, false);
    }
}
