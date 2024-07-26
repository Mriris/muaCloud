
package com.owncloud.android;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.owncloud.android.ui.dialog.RateMeDialog;
import timber.log.Timber;

public class AppRater {
    private static final String DIALOG_RATE_ME_TAG = "DIALOG_RATE_ME";

    private final static int DAYS_UNTIL_PROMPT = 2;
    private final static int LAUNCHES_UNTIL_PROMPT = 2;
    private final static int DAYS_UNTIL_NEUTRAL_CLICK = 1;

    public static final String APP_RATER_PREF_TITLE = "app_rater";
    public static final String APP_RATER_PREF_DONT_SHOW = "don't_show_again";
    private static final String APP_RATER_PREF_LAUNCH_COUNT = "launch_count";
    private static final String APP_RATER_PREF_DATE_FIRST_LAUNCH = "date_first_launch";
    public static final String APP_RATER_PREF_DATE_NEUTRAL = "date_neutral";

    public static void appLaunched(Context mContext, String packageName) {
        SharedPreferences prefs = mContext.getSharedPreferences(APP_RATER_PREF_TITLE, 0);
        if (prefs.getBoolean(APP_RATER_PREF_DONT_SHOW, false)) {
            Timber.d("Do not show the rate dialog again as the user decided");
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        long launchCount = prefs.getLong(APP_RATER_PREF_LAUNCH_COUNT, 0) + 1;
        Timber.d("The app has been launched " + launchCount + " times");
        editor.putLong(APP_RATER_PREF_LAUNCH_COUNT, launchCount);

        long dateFirstLaunch = prefs.getLong(APP_RATER_PREF_DATE_FIRST_LAUNCH, 0);
        if (dateFirstLaunch == 0) {
            dateFirstLaunch = System.currentTimeMillis();
            Timber.d("The app has been launched in " + dateFirstLaunch + " for the first time");
            editor.putLong(APP_RATER_PREF_DATE_FIRST_LAUNCH, dateFirstLaunch);
        }

        long dateNeutralClick = prefs.getLong(APP_RATER_PREF_DATE_NEUTRAL, 0);

        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            Timber.d("The number of launches already exceed " + LAUNCHES_UNTIL_PROMPT +
                    ", the default number of launches, so let's check some dates");
            Timber.d("Current moment is %s", System.currentTimeMillis());
            Timber.d("The date of the first launch + days until prompt is " + dateFirstLaunch +
                    daysToMilliseconds(DAYS_UNTIL_PROMPT));
            Timber.d("The date of the neutral click + days until neutral click is " + dateNeutralClick +
                    daysToMilliseconds(DAYS_UNTIL_NEUTRAL_CLICK));
            if (System.currentTimeMillis() >= Math.max(dateFirstLaunch
                    + daysToMilliseconds(DAYS_UNTIL_PROMPT), dateNeutralClick
                    + daysToMilliseconds(DAYS_UNTIL_NEUTRAL_CLICK))) {
                Timber.d("The current moment is later than any of the days set, so let's show the rate dialog");
                showRateDialog(mContext, packageName);
            }
        }

        editor.apply();
    }

    private static int daysToMilliseconds(int days) {
        return days * 24 * 60 * 60 * 1000;
    }

    private static void showRateDialog(Context mContext, String packageName) {
        RateMeDialog rateMeDialog = RateMeDialog.newInstance(packageName, false);
        FragmentManager fm = ((FragmentActivity) mContext).getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        rateMeDialog.show(ft, DIALOG_RATE_ME_TAG);
    }
}
