/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2016 ownCloud GmbH.
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.common.network;

import timber.log.Timber;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;


public class AdvancedX509TrustManager implements X509TrustManager {

    private X509TrustManager mStandardTrustManager;
    private KeyStore mKnownServersKeyStore;


    public AdvancedX509TrustManager(KeyStore knownServersKeyStore) throws NoSuchAlgorithmException, KeyStoreException {
        super();
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        mStandardTrustManager = findX509TrustManager(factory);

        mKnownServersKeyStore = knownServersKeyStore;
    }


    private X509TrustManager findX509TrustManager(TrustManagerFactory factory) {
        TrustManager[] tms = factory.getTrustManagers();
        for (TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        return null;
    }


    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        mStandardTrustManager.checkClientTrusted(certificates, authType);
    }


    public void checkServerTrusted(X509Certificate[] certificates, String authType) {
        if (!isKnownServer(certificates[0])) {
            CertificateCombinedException result = new CertificateCombinedException(certificates[0]);
            try {
                certificates[0].checkValidity();
            } catch (CertificateExpiredException c) {
                result.setCertificateExpiredException(c);

            } catch (CertificateNotYetValidException c) {
                result.setCertificateNotYetException(c);
            }

            try {
                mStandardTrustManager.checkServerTrusted(certificates, authType);
            } catch (CertificateException c) {
                Throwable cause = c.getCause();
                Throwable previousCause = null;
                while (cause != null && cause != previousCause && !(cause instanceof CertPathValidatorException)) {     // getCause() is not funny
                    previousCause = cause;
                    cause = cause.getCause();
                }
                if (cause instanceof CertPathValidatorException) {
                    result.setCertPathValidatorException((CertPathValidatorException) cause);
                } else {
                    result.setOtherCertificateException(c);
                }
            }

            if (result.isException()) {
                throw result;
            }

        }
    }


    public X509Certificate[] getAcceptedIssuers() {
        return mStandardTrustManager.getAcceptedIssuers();
    }

    public boolean isKnownServer(X509Certificate cert) {
        try {
            return (mKnownServersKeyStore.getCertificateAlias(cert) != null);
        } catch (KeyStoreException e) {
            Timber.e(e, "Fail while checking certificate in the known-servers store");
            return false;
        }
    }

}