
package com.owncloud.android.ui.adapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;
import timber.log.Timber;

public class DiskLruImageCache {

    private final DiskLruCache mDiskCache;
    private final CompressFormat mCompressFormat;
    private final int mCompressQuality;
    private static final int CACHE_VERSION = 2;
    private static final int VALUE_COUNT = 1;
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    public DiskLruImageCache(
            File diskCacheDir, int diskCacheSize, CompressFormat compressFormat, int quality
    ) throws IOException {

        mDiskCache = DiskLruCache.open(
                diskCacheDir, CACHE_VERSION, VALUE_COUNT, diskCacheSize
        );
        mCompressFormat = compressFormat;
        mCompressQuality = quality;
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor)
            throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            return bitmap.compress(mCompressFormat, mCompressQuality, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void put(String key, Bitmap data) {

        DiskLruCache.Editor editor = null;
        String validKey = convertToValidKey(key);
        try {
            editor = mDiskCache.edit(validKey);
            if (editor == null) {
                return;
            }

            if (writeBitmapToFile(data, editor)) {
                mDiskCache.flush();
                editor.commit();
                Timber.d("cache_test_DISK_ image put on disk cache %s", validKey);
            } else {
                editor.abort();
                Timber.d("cache_test_DISK_ ERROR on: image put on disk cache %s", validKey);
            }
        } catch (IOException e) {
            Timber.w("cache_test_DISK_ ERROR on: image put on disk cache %s", validKey);
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public Bitmap getBitmap(String key) {

        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        String validKey = convertToValidKey(key);
        try {

            snapshot = mDiskCache.get(validKey);
            if (snapshot == null) {
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                final BufferedInputStream buffIn = new BufferedInputStream(in, IO_BUFFER_SIZE);
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (IOException e) {
            Timber.e(e);
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        Timber.d(bitmap == null ? "not found" : "image read from disk %s", validKey);

        return bitmap;

    }

    private String convertToValidKey(String key) {
        return Integer.toString(key.hashCode());
    }


    public void removeKey(String key) {
        String validKey = convertToValidKey(key);
        try {
            mDiskCache.remove(validKey);
            Timber.d("removeKey from cache: %s", validKey);
        } catch (IOException e) {
            Timber.e(e);
        }
    }
}
