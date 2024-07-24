package com.owncloud.android.ui.preview;



import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.datasource.TransferListener;

import java.util.Map;


@OptIn(markerClass = UnstableApi.class)
public final class CustomHttpDataSourceFactory extends HttpDataSource.BaseFactory {

    private final String userAgent;
    private final TransferListener listener;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final boolean allowCrossProtocolRedirects;
    private final Map<String, String> headers;


    public CustomHttpDataSourceFactory(
            String userAgent, TransferListener listener, Map<String,
            String> params) {
        this(userAgent, listener, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, false, params);
    }


    public CustomHttpDataSourceFactory(String userAgent,
                                       TransferListener listener,
                                       int connectTimeoutMillis, int readTimeoutMillis,
                                       boolean allowCrossProtocolRedirects,
                                       Map<String, String> params) {
        this.userAgent = userAgent;
        this.listener = listener;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
        this.headers = params;
    }

    @Override
    protected HttpDataSource createDataSourceInternal(HttpDataSource.RequestProperties defaultRequestProperties) {
        DefaultHttpDataSource defaultHttpDataSource = new DefaultHttpDataSource.Factory()
                .setUserAgent(userAgent)
                .setTransferListener(listener)
                .setConnectTimeoutMs(connectTimeoutMillis)
                .setReadTimeoutMs(readTimeoutMillis)
                .setAllowCrossProtocolRedirects(allowCrossProtocolRedirects)
                .createDataSource();

        // Set headers in http data source
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            defaultHttpDataSource.setRequestProperty(entry.getKey(), entry.getValue());
        }

        return defaultHttpDataSource;
    }
}

