
package com.owncloud.android.lib.common.network;

import android.net.Uri;

import com.owncloud.android.lib.common.http.methods.HttpBaseMethod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WebdavUtils {

    private static final SimpleDateFormat[] DATETIME_FORMATS = {
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
            new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
    };

    public static Date parseResponseDate(String date) {
        Date returnDate;
        SimpleDateFormat format;
        for (SimpleDateFormat datetimeFormat : DATETIME_FORMATS) {
            try {
                format = datetimeFormat;
                synchronized (format) {
                    returnDate = format.parse(date);
                }
                return returnDate;
            } catch (ParseException e) {

            }
        }
        return null;
    }


    public static String encodePath(String remoteFilePath) {
        String encodedPath = Uri.encode(remoteFilePath, "/");
        if (!encodedPath.startsWith("/")) {
            encodedPath = "/" + encodedPath;
        }
        return encodedPath;
    }


    public static String getEtagFromResponse(HttpBaseMethod httpBaseMethod) {
        String eTag = httpBaseMethod.getResponseHeader("OC-ETag");
        if (eTag == null) {
            eTag = httpBaseMethod.getResponseHeader("oc-etag");
        }
        if (eTag == null) {
            eTag = httpBaseMethod.getResponseHeader("ETag");
        }
        if (eTag == null) {
            eTag = httpBaseMethod.getResponseHeader("etag");
        }
        String result = "";
        if (eTag != null) {
            result = eTag;
        }
        return result;
    }

    public static String normalizeProtocolPrefix(String url, boolean isSslConn) {
        if (!url.toLowerCase().startsWith("http://") &&
                !url.toLowerCase().startsWith("https://")) {
            if (isSslConn) {
                return "https://" + url;
            } else {
                return "http://" + url;
            }
        }
        return url;
    }

}