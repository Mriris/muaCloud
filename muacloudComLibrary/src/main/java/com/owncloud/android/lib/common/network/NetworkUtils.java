
package com.owncloud.android.lib.common.network;

import android.content.Context;

import timber.log.Timber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class NetworkUtils {

    private static String LOCAL_TRUSTSTORE_FILENAME = "knownServers.bks";

    private static String LOCAL_TRUSTSTORE_PASSWORD = "password";

    private static KeyStore mKnownServersStore = null;


    public static KeyStore getKnownServersStore(Context context)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (mKnownServersStore == null) {

            mKnownServersStore = KeyStore.getInstance(KeyStore.getDefaultType());
            File localTrustStoreFile = new File(context.getFilesDir(), LOCAL_TRUSTSTORE_FILENAME);
            Timber.d("Searching known-servers store at %s", localTrustStoreFile.getAbsolutePath());
            if (localTrustStoreFile.exists()) {
                InputStream in = new FileInputStream(localTrustStoreFile);
                try {
                    mKnownServersStore.load(in, LOCAL_TRUSTSTORE_PASSWORD.toCharArray());
                } finally {
                    in.close();
                }
            } else {

                mKnownServersStore.load(null, LOCAL_TRUSTSTORE_PASSWORD.toCharArray());
            }
        }
        return mKnownServersStore;
    }

    public static void addCertToKnownServersStore(Certificate cert, Context context)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        KeyStore knownServers = getKnownServersStore(context);
        knownServers.setCertificateEntry(Integer.toString(cert.hashCode()), cert);
        try (FileOutputStream fos = context.openFileOutput(LOCAL_TRUSTSTORE_FILENAME, Context.MODE_PRIVATE)) {
            knownServers.store(fos, LOCAL_TRUSTSTORE_PASSWORD.toCharArray());
        }
    }

}
