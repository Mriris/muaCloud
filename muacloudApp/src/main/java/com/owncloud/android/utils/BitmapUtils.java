
package com.owncloud.android.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import com.owncloud.android.presentation.authentication.AccountUtils;
import timber.log.Timber;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import static com.owncloud.android.domain.files.model.MimeTypeConstantsKt.MIME_PREFIX_IMAGE;


public class BitmapUtils {

    
    public static Bitmap decodeSampledBitmapFromFile(String srcPath, int reqWidth, int reqHeight) {

        // set desired options that will affect the size of the bitmap
        final Options options = new Options();
        options.inScaled = true;
        options.inPurgeable = true;
        options.inPreferQualityOverSpeed = false;
        options.inMutable = false;

        // make a false load of the bitmap to get its dimensions
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(srcPath, options);

        // calculate factor to subsample the bitmap
        options.inSampleSize = calculateSampleFactor(options, reqWidth, reqHeight);

        // decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(srcPath, options);
    }

    
    private static int calculateSampleFactor(Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // calculates the largest inSampleSize value (for smallest sample) that is a power of 2 and keeps both
            // height and width **larger** than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    
    public static Bitmap rotateImage(final Bitmap bitmap, final String storagePath) {
        try {
            ExifInterface exifInterface = new ExifInterface(storagePath);
            final int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix matrix = new Matrix();
            // 1: nothing to do

            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1.0f, 1.0f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1.0f, -1.0f);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.postRotate(-90);
                    matrix.postScale(1.0f, -1.0f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.postRotate(90);
                    matrix.postScale(1.0f, -1.0f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
            }

            // Rotate the bitmap
            final Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (resultBitmap != bitmap) {
                bitmap.recycle();
            }
            return resultBitmap;
        } catch (Exception exception) {
            Timber.w("Could not rotate the image: %s", storagePath);
            return bitmap;
        }
    }

    private static float fixRawHSLValue(final float value, final float upperBound, final float scale) {
        return (value > upperBound) ? upperBound
                : (value < 0f) ? 0f
                : value * scale;
    }

    
    public static int[] HSLtoRGB(final float h, final float s, final float l, final float alpha) {
        if (s < 0.0f || s > 100.0f) {
            Timber.w("Color parameter outside of expected range - Saturation");
        }

        if (l < 0.0f || l > 100.0f) {
            Timber.w("Color parameter outside of expected range - Luminance");
        }

        if (alpha < 0.0f || alpha > 1.0f) {
            Timber.w("Color parameter outside of expected range - Alpha");
        }

        //  Formula needs all values between 0 - 1.

        final float hr = (h % 360.0f) / 360f;
        final float sr = fixRawHSLValue(s, 100f, 1 / 100f);
        final float lr = fixRawHSLValue(s, 100f, 1 / 100f);

        final float q = (lr < 0.5)
                ? lr * (1 + sr)
                : (lr + sr) - (lr * sr);
        final float p = 2 * lr - q;
        final int r = Math.round(Math.max(0, HueToRGB(p, q, hr + (1.0f / 3.0f)) * 256));
        final int g = Math.round(Math.max(0, HueToRGB(p, q, hr) * 256));
        final int b = Math.round(Math.max(0, HueToRGB(p, q, hr - (1.0f / 3.0f)) * 256));

        return new int[]{r, g, b};
    }

    private static float HueToRGB(final float p, final float q, final float h) {
        final float hr = (h < 0) ? h + 1
                : (h > 1) ? h - 1
                : h;

        if (6 * hr < 1) {
            return p + ((q - p) * 6 * h);
        }
        if (2 * hr < 1) {
            return q;
        }
        if (3 * hr < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }
        return p;
    }

    
    public static boolean isImage(File file) {
        final Uri selectedUri = Uri.fromFile(file);
        final String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString().toLowerCase());
        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);

        return (mimeType != null && mimeType.startsWith(MIME_PREFIX_IMAGE));
    }

    
    public static int[] calculateAvatarBackgroundRGB(String accountName)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // using adapted algorithm from /core/js/placeholder.js:50
        final String username = AccountUtils.getUsernameOfAccount(accountName);
        final byte[] seed = username.getBytes(StandardCharsets.UTF_8);
        final MessageDigest md = MessageDigest.getInstance("MD5");
        // Integer seedMd5Int = Math.abs(new String(Hex.encodeHex(seedMd5)).hashCode());
        final Integer seedMd5Int = String.format(Locale.ROOT, "%032x",
                new BigInteger(1, md.digest(seed))).hashCode();

        final double maxRange = Integer.MAX_VALUE;
        final float hue = (float) (seedMd5Int / maxRange * 360);

        return BitmapUtils.HSLtoRGB(hue, 90.0f, 65.0f, 1.0f);
    }

    
    public static RoundedBitmapDrawable bitmapToCircularBitmapDrawable(Resources resources, Bitmap bitmap) {
        RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(resources, bitmap);
        roundedBitmap.setCircular(true);
        return roundedBitmap;
    }
}
