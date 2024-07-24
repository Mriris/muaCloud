

package com.owncloud.android.utils;

import timber.log.Timber;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    public static String stringToMD5Hash(String stringToTransform) {
        MessageDigest messageDigest;
        String hash = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(stringToTransform.getBytes(), 0, stringToTransform.length());
            hash = new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            Timber.e(e, "It's been not possible to generate the MD5 hash");
        }

        return hash;
    }
}