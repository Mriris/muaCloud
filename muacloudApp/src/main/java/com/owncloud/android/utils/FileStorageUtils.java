
package com.owncloud.android.utils;

import android.annotation.SuppressLint;
import android.webkit.MimeTypeMap;

import com.owncloud.android.data.providers.LocalStorageProvider;
import kotlin.Lazy;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import java.io.File;

import static org.koin.java.KoinJavaComponent.inject;


public class FileStorageUtils {

    public static final int SORT_NAME = 0;
    public static final int SORT_DATE = 1;
    public static final int SORT_SIZE = 2;
    public static final int FILE_DISPLAY_SORT = 3;
    public static Integer mSortOrderFileDisp = SORT_NAME;
    public static Boolean mSortAscendingFileDisp = true;


    private static LocalStorageProvider getLocalStorageProvider() {
        @NotNull Lazy<LocalStorageProvider> localStorageProvider = inject(LocalStorageProvider.class);
        return localStorageProvider.getValue();
    }


    public static String getTemporalPath(String accountName, String spaceId) {
        return getLocalStorageProvider().getTemporalPath(accountName, spaceId);
    }


    @SuppressLint("UsableSpace")
    public static long getUsableSpace() {
        return getLocalStorageProvider().getUsableSpace();
    }


    public static String getMimeTypeFromName(String path) {
        String extension = "";
        int pos = path.lastIndexOf('.');
        if (pos >= 0) {
            extension = path.substring(pos + 1);
        }
        String result = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        return (result != null) ? result : "";
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        Timber.w("File NOT deleted %s", child);
                        return false;
                    } else {
                        Timber.d("File deleted %s", child);
                    }
                }
            } else {
                return false;
            }
        }

        return dir.delete();
    }
}
