
package com.owncloud.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.owncloud.android.MainApp;
import com.owncloud.android.R;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.IDN;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class DisplayUtils {

    private static final String OWNCLOUD_APP_NAME = "ownCloud";

    private static final String[] sizeSuffixes = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
    private static final int[] sizeScales = {0, 0, 1, 1, 1, 2, 2, 2, 2};

    private static final Map<String, String> mimeType2HumanReadable;

    static {
        mimeType2HumanReadable = new HashMap<>();

        mimeType2HumanReadable.put("image/jpeg", "JPEG image");
        mimeType2HumanReadable.put("image/jpg", "JPEG image");
        mimeType2HumanReadable.put("image/png", "PNG image");
        mimeType2HumanReadable.put("image/bmp", "Bitmap image");
        mimeType2HumanReadable.put("image/gif", "GIF image");
        mimeType2HumanReadable.put("image/svg+xml", "JPEG image");
        mimeType2HumanReadable.put("image/tiff", "TIFF image");

        mimeType2HumanReadable.put("audio/mpeg", "MP3 music file");
        mimeType2HumanReadable.put("application/ogg", "OGG music file");
    }


    public static String bytesToHumanReadable(long bytes, Context context) {
        if (bytes < 0) {
            return context.getString(R.string.common_pending);

        } else {
            double result = bytes;
            int attachedSuff = 0;
            while (result >= 1024 && attachedSuff < sizeSuffixes.length) {
                result /= 1024.;
                attachedSuff++;
            }

            BigDecimal readableResult = new BigDecimal(result).setScale(
                    sizeScales[attachedSuff],
                    RoundingMode.HALF_UP
            ).stripTrailingZeros();

            return (readableResult.scale() < 0 ?
                    readableResult.setScale(0) :
                    readableResult
            ) + " " + sizeSuffixes[attachedSuff];
        }
    }


    public static String convertMIMEtoPrettyPrint(String mimetype) {
        if (mimeType2HumanReadable.containsKey(mimetype)) {
            return mimeType2HumanReadable.get(mimetype);
        }
        if (mimetype.split("/").length >= 2) {
            return mimetype.split("/")[1].toUpperCase() + " file";
        }
        return "Unknown type";
    }


    public static String unixTimeToHumanReadable(long milliseconds) {
        Date date = new Date(milliseconds);
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(date);
    }

    public static int getSeasonalIconId() {
        if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) >= 354 &&
                MainApp.Companion.getAppContext().getString(R.string.app_name).equals(OWNCLOUD_APP_NAME)) {
            return R.drawable.winter_holidays_icon;
        } else {
            return R.mipmap.icon;
        }
    }


    public static String convertIdn(String url, boolean toASCII) {

        String urlNoDots = url;
        String dots = "";
        while (urlNoDots.startsWith(".")) {
            urlNoDots = url.substring(1);
            dots = dots + ".";
        }

        int hostStart = 0;
        if (urlNoDots.contains("//")) {
            hostStart = url.indexOf("//") + "//".length();
        } else if (url.contains("@")) {
            hostStart = url.indexOf("@") + "@".length();
        }

        int hostEnd = url.substring(hostStart).indexOf("/");

        hostEnd = (hostEnd == -1 ? urlNoDots.length() : hostStart + hostEnd);

        String host = urlNoDots.substring(hostStart, hostEnd);
        host = (toASCII ? IDN.toASCII(host) : IDN.toUnicode(host));

        return dots + urlNoDots.substring(0, hostStart) + host + urlNoDots.substring(hostEnd);
    }


    public static CharSequence getRelativeTimestamp(Context context, long modificationTimestamp) {
        return getRelativeDateTimeString(context, modificationTimestamp, DateUtils.SECOND_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS, 0);
    }

    public static CharSequence getRelativeDateTimeString(
            Context c, long time, long minResolution, long transitionResolution, int flags
    ) {

        CharSequence dateString;

        if (time > System.currentTimeMillis()) {
            return DisplayUtils.unixTimeToHumanReadable(time);
        }

        else if ((System.currentTimeMillis() - time) < 60 * 1000) {
            return c.getString(R.string.file_list_seconds_ago);
        } else {
            dateString = DateUtils.getRelativeDateTimeString(c, time, minResolution, transitionResolution, flags);
        }

        String[] parts = dateString.toString().split(",");
        if (parts.length == 2) {
            if (parts[1].contains(":") && !parts[0].contains(":")) {
                return parts[0];
            } else if (parts[0].contains(":") && !parts[1].contains(":")) {
                return parts[1];
            }
        }

        return dateString.toString();
    }


    public static String getPathWithoutLastSlash(String path) {

        if (path.length() > 1 && path.charAt(path.length() - 1) == File.separator.charAt(0)) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }


    public static void colorSnackbar(Context context, Snackbar snackbar) {

        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.white));
    }

    public static int getDrawerHeaderHeight(int displayCutout, Resources resources) {
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int displayCutoutDP =
                    displayCutout / (resources.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
            return (int) resources.getDimension(R.dimen.nav_drawer_header_height) + displayCutoutDP;
        } else {
            return (int) resources.getDimension(R.dimen.nav_drawer_header_height);
        }
    }
}
