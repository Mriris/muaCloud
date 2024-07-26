
package com.owncloud.android.lib.resources.files;

import timber.log.Timber;

import java.io.File;

public class FileUtils {
    public static final String FINAL_CHUNKS_FILE = ".file";
    public static final String MIME_DIR = "DIR";
    public static final String MIME_DIR_UNIX = "httpd/unix-directory";
    public static final String MODE_READ_ONLY = "r";

    static String getParentPath(String remotePath) {
        String parentPath = new File(remotePath).getParent();
        parentPath = parentPath.endsWith(File.separator) ? parentPath : parentPath + File.separator;
        return parentPath;
    }


    public static boolean isValidName(String fileName) {
        boolean result = true;

        Timber.d("fileName =======%s", fileName);
        if (fileName.contains(File.separator)) {
            result = false;
        }
        return result;
    }
}
