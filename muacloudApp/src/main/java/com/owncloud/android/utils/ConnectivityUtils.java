

package com.owncloud.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import timber.log.Timber;

public class ConnectivityUtils {

    public static boolean isAppConnectedViaWiFi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean result =
                cm != null && cm.getActiveNetworkInfo() != null
                        && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI
                        && cm.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED;
        Timber.d("isAppConnectedViaWifi returns %s", result);
        return result;
    }

    public static boolean isAppConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static boolean isNetworkActive(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }
}
