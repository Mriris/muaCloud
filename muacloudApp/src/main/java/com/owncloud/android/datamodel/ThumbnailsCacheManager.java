

package com.owncloud.android.datamodel;

import android.accounts.Account;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import com.owncloud.android.MainApp;
import com.owncloud.android.R;
import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.domain.files.usecases.DisableThumbnailsForFileUseCase;
import com.owncloud.android.domain.files.usecases.GetWebDavUrlForSpaceUseCase;
import com.owncloud.android.domain.spaces.model.SpaceSpecial;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.SingleSessionManager;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.http.HttpConstants;
import com.owncloud.android.lib.common.http.methods.nonwebdav.GetMethod;
import com.owncloud.android.ui.adapter.DiskLruImageCache;
import com.owncloud.android.utils.BitmapUtils;
import kotlin.Lazy;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Locale;

import static org.koin.java.KoinJavaComponent.inject;


public class ThumbnailsCacheManager {

    private static final String CACHE_FOLDER = "thumbnailCache";

    private static final Object mThumbnailsDiskCacheLock = new Object();
    private static DiskLruImageCache mThumbnailCache = null;
    private static boolean mThumbnailCacheStarting = true;

    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final CompressFormat mCompressFormat = CompressFormat.JPEG;
    private static final int mCompressQuality = 70;
    private static OwnCloudClient mClient = null;

    private static final String PREVIEW_URI = "%s%s?x=%d&y=%d&c=%s&preview=1";
    private static final String SPACE_SPECIAL_URI = "%s?scalingup=0&a=1&x=%d&y=%d&c=%s&preview=1";

    public static Bitmap mDefaultImg =
            BitmapFactory.decodeResource(
                    MainApp.Companion.getAppContext().getResources(),
                    R.drawable.file_image
            );

    public static class InitDiskCacheTask extends AsyncTask<File, Void, Void> {

        @Override
        protected Void doInBackground(File... params) {
            synchronized (mThumbnailsDiskCacheLock) {
                mThumbnailCacheStarting = true;

                if (mThumbnailCache == null) {
                    try {


                        final String cachePath =
                                MainApp.Companion.getAppContext().getExternalCacheDir().getPath() +
                                        File.separator + CACHE_FOLDER;
                        Timber.d("create dir: %s", cachePath);
                        final File diskCacheDir = new File(cachePath);
                        mThumbnailCache = new DiskLruImageCache(
                                diskCacheDir,
                                DISK_CACHE_SIZE,
                                mCompressFormat,
                                mCompressQuality
                        );
                    } catch (Exception e) {
                        Timber.e(e, "Thumbnail cache could not be opened ");
                        mThumbnailCache = null;
                    }
                }
                mThumbnailCacheStarting = false; // Finished initialization
                mThumbnailsDiskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }

    public static void addBitmapToCache(String key, Bitmap bitmap) {
        synchronized (mThumbnailsDiskCacheLock) {
            if (mThumbnailCache != null) {
                mThumbnailCache.put(key, bitmap);
            }
        }
    }

    public static void removeBitmapFromCache(String key) {
        synchronized (mThumbnailsDiskCacheLock) {
            if (mThumbnailCache != null) {
                mThumbnailCache.removeKey(key);
            }
        }
    }

    public static Bitmap getBitmapFromDiskCache(String key) {
        synchronized (mThumbnailsDiskCacheLock) {

            while (mThumbnailCacheStarting) {
                try {
                    mThumbnailsDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    Timber.e(e, "Wait in mThumbnailsDiskCacheLock was interrupted");
                }
            }
            if (mThumbnailCache != null) {
                return mThumbnailCache.getBitmap(key);
            }
        }
        return null;
    }

    public static class ThumbnailGenerationTask extends AsyncTask<Object, Void, Bitmap> {
        private final WeakReference<ImageView> mImageViewReference;
        private static Account mAccount;
        private Object mFile;
        private FileDataStorageManager mStorageManager;

        public ThumbnailGenerationTask(ImageView imageView, Account account) {

            mImageViewReference = new WeakReference<>(imageView);
            mAccount = account;
        }

        public ThumbnailGenerationTask(ImageView imageView) {

            mImageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap thumbnail = null;

            try {
                if (mAccount != null) {
                    OwnCloudAccount ocAccount = new OwnCloudAccount(
                            mAccount,
                            MainApp.Companion.getAppContext()
                    );
                    mClient = SingleSessionManager.getDefaultSingleton().
                            getClientFor(ocAccount, MainApp.Companion.getAppContext());
                }

                mFile = params[0];

                if (mFile instanceof OCFile) {
                    thumbnail = doOCFileInBackground();
                } else if (mFile instanceof File) {
                    thumbnail = doFileInBackground();
                } else if (mFile instanceof SpaceSpecial) {
                    thumbnail = doSpaceImageInBackground();

                }

            } catch (Throwable t) {

                Timber.e(t, "Generation of thumbnail for " + mFile + " failed");
                if (t instanceof OutOfMemoryError) {
                    System.gc();
                }
            }

            return thumbnail;
        }

        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = mImageViewReference.get();
                final ThumbnailGenerationTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask) {
                    String tagId = "";
                    if (mFile instanceof OCFile) {
                        tagId = String.valueOf(((OCFile) mFile).getId());
                    } else if (mFile instanceof File) {
                        tagId = String.valueOf(mFile.hashCode());
                    } else if (mFile instanceof SpaceSpecial) {
                        tagId = ((SpaceSpecial) mFile).getId();
                    }
                    if (String.valueOf(imageView.getTag()).equals(tagId)) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }


        private Bitmap addThumbnailToCache(String imageKey, Bitmap bitmap, String path, int px) {

            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, px, px);

            thumbnail = BitmapUtils.rotateImage(thumbnail, path);

            addBitmapToCache(imageKey, thumbnail);

            return thumbnail;
        }

        
        private int getThumbnailDimension() {

            Resources r = MainApp.Companion.getAppContext().getResources();
            return Math.round(r.getDimension(R.dimen.file_icon_size_grid));
        }

        private String getPreviewUrl(OCFile ocFile, Account account) {
            String baseUrl = mClient.getBaseUri() + "/remote.php/dav/files/" + AccountUtils.getUserId(account, MainApp.Companion.getAppContext());

            if (ocFile.getSpaceId() != null) {
                Lazy<GetWebDavUrlForSpaceUseCase> getWebDavUrlForSpaceUseCaseLazy = inject(GetWebDavUrlForSpaceUseCase.class);
                baseUrl = getWebDavUrlForSpaceUseCaseLazy.getValue().invoke(
                        new GetWebDavUrlForSpaceUseCase.Params(ocFile.getOwner(), ocFile.getSpaceId())
                );

            }
            return String.format(Locale.ROOT,
                    PREVIEW_URI,
                    baseUrl,
                    Uri.encode(ocFile.getRemotePath(), "/"),
                    getThumbnailDimension(),
                    getThumbnailDimension(),
                    ocFile.getEtag());
        }

        private Bitmap doOCFileInBackground() {
            OCFile file = (OCFile) mFile;

            final String imageKey = String.valueOf(file.getRemoteId());

            Bitmap thumbnail = getBitmapFromDiskCache(imageKey);

            if (thumbnail == null || file.getNeedsToUpdateThumbnail()) {

                int px = getThumbnailDimension();

                if (mClient != null) {
                    GetMethod get;
                    try {
                        String uri = getPreviewUrl(file, mAccount);
                        Timber.d("URI: %s", uri);
                        get = new GetMethod(new URL(uri));
                        int status = mClient.executeHttpMethod(get);
                        if (status == HttpConstants.HTTP_OK) {
                            InputStream inputStream = get.getResponseBodyAsStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            thumbnail = ThumbnailUtils.extractThumbnail(bitmap, px, px);

                            if (file.getMimeType().equalsIgnoreCase("image/png")) {
                                thumbnail = handlePNG(thumbnail, px);
                            }

                            if (thumbnail != null) {
                                addBitmapToCache(imageKey, thumbnail);
                            }
                        } else {
                            mClient.exhaustResponse(get.getResponseBodyAsStream());
                        }
                        if (status == HttpConstants.HTTP_OK || status == HttpConstants.HTTP_NOT_FOUND) {
                            @NotNull Lazy<DisableThumbnailsForFileUseCase> disableThumbnailsForFileUseCaseLazy = inject(DisableThumbnailsForFileUseCase.class);
                            disableThumbnailsForFileUseCaseLazy.getValue().invoke(new DisableThumbnailsForFileUseCase.Params(file.getId()));
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }

            return thumbnail;

        }

        private Bitmap handlePNG(Bitmap bitmap, int px) {
            Bitmap resultBitmap = Bitmap.createBitmap(px,
                    px,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(resultBitmap);

            c.drawColor(ContextCompat.getColor(MainApp.Companion.getAppContext(), R.color.background_color));
            c.drawBitmap(bitmap, 0, 0, null);

            return resultBitmap;
        }

        private Bitmap doFileInBackground() {
            File file = (File) mFile;

            final String imageKey = String.valueOf(file.hashCode());

            Bitmap thumbnail = getBitmapFromDiskCache(imageKey);

            if (thumbnail == null) {

                int px = getThumbnailDimension();

                Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(
                        file.getAbsolutePath(), px, px);

                if (bitmap != null) {
                    thumbnail = addThumbnailToCache(imageKey, bitmap, file.getPath(), px);
                }
            }
            return thumbnail;
        }

        private String getSpaceSpecialUri(SpaceSpecial spaceSpecial) {

            Resources r = MainApp.Companion.getAppContext().getResources();
            Integer spacesThumbnailSize = Math.round(r.getDimension(R.dimen.spaces_thumbnail_height)) * 2;
            return String.format(Locale.ROOT,
                    SPACE_SPECIAL_URI,
                    spaceSpecial.getWebDavUrl(),
                    spacesThumbnailSize,
                    spacesThumbnailSize,
                    spaceSpecial.getETag());
        }

        private Bitmap doSpaceImageInBackground() {
            SpaceSpecial spaceSpecial = (SpaceSpecial) mFile;

            final String imageKey = spaceSpecial.getId();

            Bitmap thumbnail = getBitmapFromDiskCache(imageKey);

            if (thumbnail == null) {
                int px = getThumbnailDimension();

                if (mClient != null) {
                    GetMethod get;
                    try {
                        String uri = getSpaceSpecialUri(spaceSpecial);
                        Timber.d("URI: %s", uri);
                        get = new GetMethod(new URL(uri));
                        int status = mClient.executeHttpMethod(get);
                        if (status == HttpConstants.HTTP_OK) {
                            InputStream inputStream = get.getResponseBodyAsStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            thumbnail = ThumbnailUtils.extractThumbnail(bitmap, px, px);

                            if (spaceSpecial.getFile().getMimeType().equalsIgnoreCase("image/png")) {
                                thumbnail = handlePNG(thumbnail, px);
                            }

                            if (thumbnail != null) {
                                addBitmapToCache(imageKey, thumbnail);
                            }
                        } else {
                            mClient.exhaustResponse(get.getResponseBodyAsStream());
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }

            return thumbnail;

        }
    }

    public static boolean cancelPotentialThumbnailWork(Object file, ImageView imageView) {
        final ThumbnailGenerationTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mFile;

            if (bitmapData == null || bitmapData != file) {

                bitmapWorkerTask.cancel(true);
                Timber.v("Cancelled generation of thumbnail for a reused imageView");
            } else {

                return false;
            }
        }

        return true;
    }

    private static ThumbnailGenerationTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncThumbnailDrawable) {
                final AsyncThumbnailDrawable asyncDrawable = (AsyncThumbnailDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static class AsyncThumbnailDrawable extends BitmapDrawable {
        private final WeakReference<ThumbnailGenerationTask> bitmapWorkerTaskReference;

        public AsyncThumbnailDrawable(
                Resources res, Bitmap bitmap, ThumbnailGenerationTask bitmapWorkerTask
        ) {

            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        ThumbnailGenerationTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}
