
package com.owncloud.android.lib.common.http;

import android.content.Context;

import com.owncloud.android.lib.common.http.logging.LogInterceptor;
import com.owncloud.android.lib.common.network.AdvancedX509TrustManager;
import com.owncloud.android.lib.common.network.NetworkUtils;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.TlsVersion;
import timber.log.Timber;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;



public class HttpClient {
    private final Context mContext;
    private final HashMap<String, List<Cookie>> mCookieStore = new HashMap<>();
    private final LogInterceptor mLogInterceptor = new LogInterceptor();

    private OkHttpClient mOkHttpClient = null;

    protected HttpClient(Context context) {
        if (context == null) {
            Timber.e("Context may not be NULL!");
            throw new NullPointerException("Context may not be NULL!");
        }
        mContext = context;
    }

    public OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            try {
                final X509TrustManager trustManager = new AdvancedX509TrustManager(
                        NetworkUtils.getKnownServersStore(mContext));

                final SSLContext sslContext = buildSSLContext();
                sslContext.init(null, new TrustManager[]{trustManager}, null);
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                final CookieJar cookieJar = new CookieJarImpl(mCookieStore);
                mOkHttpClient = buildNewOkHttpClient(sslSocketFactory, trustManager, cookieJar);

            } catch (NoSuchAlgorithmException nsae) {
                Timber.e(nsae, "Could not setup SSL system.");
                throw new RuntimeException("Could not setup okHttp client.", nsae);
            } catch (Exception e) {
                Timber.e(e, "Could not setup okHttp client.");
                throw new RuntimeException("Could not setup okHttp client.", e);
            }
        }
        return mOkHttpClient;
    }

    private SSLContext buildSSLContext() throws NoSuchAlgorithmException {
        try {
            return SSLContext.getInstance(TlsVersion.TLS_1_3.javaName());
        } catch (NoSuchAlgorithmException tlsv13Exception) {
            try {
                Timber.w("TLSv1.3 is not supported in this device; falling through TLSv1.2");
                return SSLContext.getInstance(TlsVersion.TLS_1_2.javaName());
            } catch (NoSuchAlgorithmException tlsv12Exception) {
                try {
                    Timber.w("TLSv1.2 is not supported in this device; falling through TLSv1.1");
                    return SSLContext.getInstance(TlsVersion.TLS_1_1.javaName());
                } catch (NoSuchAlgorithmException tlsv11Exception) {
                    Timber.w("TLSv1.1 is not supported in this device; falling through TLSv1.0");
                    return SSLContext.getInstance(TlsVersion.TLS_1_0.javaName());


                }
            }
        }
    }

    private OkHttpClient buildNewOkHttpClient(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager,
                                              CookieJar cookieJar) {
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(getLogInterceptor())
                .addNetworkInterceptor(DebugInterceptorFactory.INSTANCE.getInterceptor())
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .readTimeout(HttpConstants.DEFAULT_DATA_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(HttpConstants.DEFAULT_DATA_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(HttpConstants.DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .followRedirects(false)
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier((asdf, usdf) -> true)
                .cookieJar(cookieJar)
                .build();
    }

    public Context getContext() {
        return mContext;
    }

    public LogInterceptor getLogInterceptor() {
        return mLogInterceptor;
    }

    public List<Cookie> getCookiesFromUrl(HttpUrl httpUrl) {
        return mCookieStore.get(httpUrl.host());
    }

    public void clearCookies() {
        mCookieStore.clear();
    }
}
