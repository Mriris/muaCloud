
package com.owncloud.android.lib.common.network;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;


public class CertificateCombinedException extends RuntimeException {

    
    private static final long serialVersionUID = -8875782030758554999L;

    private X509Certificate mServerCert = null;
    private String mHostInUrl;

    private CertificateExpiredException mCertificateExpiredException = null;
    private CertificateNotYetValidException mCertificateNotYetValidException = null;
    private CertPathValidatorException mCertPathValidatorException = null;
    private CertificateException mOtherCertificateException = null;
    private SSLPeerUnverifiedException mSslPeerUnverifiedException = null;

    public CertificateCombinedException(X509Certificate x509Certificate) {
        mServerCert = x509Certificate;
    }

    public X509Certificate getServerCertificate() {
        return mServerCert;
    }

    public String getHostInUrl() {
        return mHostInUrl;
    }

    public void setHostInUrl(String host) {
        mHostInUrl = host;
    }

    public CertificateExpiredException getCertificateExpiredException() {
        return mCertificateExpiredException;
    }

    public void setCertificateExpiredException(CertificateExpiredException c) {
        mCertificateExpiredException = c;
    }

    public CertificateNotYetValidException getCertificateNotYetValidException() {
        return mCertificateNotYetValidException;
    }

    public void setCertificateNotYetException(CertificateNotYetValidException c) {
        mCertificateNotYetValidException = c;
    }

    public CertPathValidatorException getCertPathValidatorException() {
        return mCertPathValidatorException;
    }

    public void setCertPathValidatorException(CertPathValidatorException c) {
        mCertPathValidatorException = c;
    }

    public CertificateException getOtherCertificateException() {
        return mOtherCertificateException;
    }

    public void setOtherCertificateException(CertificateException c) {
        mOtherCertificateException = c;
    }

    public SSLPeerUnverifiedException getSslPeerUnverifiedException() {
        return mSslPeerUnverifiedException;
    }

    public void setSslPeerUnverifiedException(SSLPeerUnverifiedException s) {
        mSslPeerUnverifiedException = s;
    }

    public boolean isException() {
        return (mCertificateExpiredException != null ||
                mCertificateNotYetValidException != null ||
                mCertPathValidatorException != null ||
                mOtherCertificateException != null ||
                mSslPeerUnverifiedException != null);
    }

    public boolean isRecoverable() {
        return (mCertificateExpiredException != null ||
                mCertificateNotYetValidException != null ||
                mCertPathValidatorException != null ||
                mSslPeerUnverifiedException != null);
    }

}
