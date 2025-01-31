

package com.owncloud.android.ui.preview;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.extractor.DefaultExtractorsFactory;
import com.owncloud.android.MainApp;
import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.domain.files.usecases.GetWebDavUrlForSpaceUseCase;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.authentication.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.authentication.OwnCloudBearerCredentials;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentials;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.utils.UriUtilsKt;
import kotlin.Lazy;
import timber.log.Timber;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static org.koin.java.KoinJavaComponent.inject;


@OptIn(markerClass = UnstableApi.class)
public class PrepareVideoPlayerAsyncTask extends AsyncTask<Object, Void, MediaSource> {

    private final Context mContext;
    private final WeakReference<OnPrepareVideoPlayerTaskListener> mListener;
    private final OCFile mFile;
    private final Account mAccount;

    private static DefaultBandwidthMeter BANDWIDTH_METER;

    public PrepareVideoPlayerAsyncTask(Context context, OnPrepareVideoPlayerTaskListener listener, OCFile file, Account account) {
        mContext = context;
        mListener = new WeakReference<>(listener);
        mFile = file;
        mAccount = account;
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(mContext).build();
    }

    @Override
    protected MediaSource doInBackground(Object... params) {

        MediaSource mediaSource = null;

        try {

            Uri uri;
            if (mFile.isAvailableLocally()) {
                uri = UriUtilsKt.INSTANCE.getStorageUriForFile(mFile);
            } else {
                Lazy<GetWebDavUrlForSpaceUseCase> getWebDavUrlForSpaceUseCaseLazy = inject(GetWebDavUrlForSpaceUseCase.class);
                String spaceWebDavUrl = getWebDavUrlForSpaceUseCaseLazy.getValue().invoke(
                        new GetWebDavUrlForSpaceUseCase.Params(mFile.getOwner(), mFile.getSpaceId())
                );
                String webDavUrl = spaceWebDavUrl == null ? AccountUtils.getWebDavUrlForAccount(mContext, mAccount) : spaceWebDavUrl;
                uri = Uri.parse(webDavUrl + WebdavUtils.encodePath(mFile.getRemotePath()));
            }

            HttpDataSource.Factory httpDataSourceFactory =
                    buildHttpDataSourceFactory(BANDWIDTH_METER, mFile, mAccount);

            DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(mContext,
                    BANDWIDTH_METER, httpDataSourceFactory);

            mediaSource = buildMediaSource(mediaDataSourceFactory, uri);

        } catch (AccountUtils.AccountNotFoundException e) {
            Timber.e(e);
        }

        return mediaSource;
    }


    private MediaSource buildMediaSource(DataSource.Factory mediaDataSourceFactory, Uri uri) {
        return new ProgressiveMediaSource.Factory(mediaDataSourceFactory, new DefaultExtractorsFactory()).createMediaSource(MediaItem.fromUri(uri));
    }


    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter, OCFile file, Account account) {

        if (file.isAvailableLocally()) {

            return new DefaultHttpDataSource.Factory();

        } else {

            try {
                OwnCloudCredentials credentials = AccountUtils.getCredentialsForAccount(MainApp.Companion.getAppContext(), account);

                String login = credentials.getUsername();
                String password = credentials.getAuthToken();

                Map<String, String> params = new HashMap<>(1);

                if (credentials instanceof OwnCloudBasicCredentials) { // Basic auth
                    String cred = login + ":" + password;
                    String auth = "Basic " + Base64.encodeToString(cred.getBytes(), Base64.URL_SAFE);
                    params.put("Authorization", auth);
                } else if (credentials instanceof OwnCloudBearerCredentials) { // OAuth
                    String bearerToken = credentials.getAuthToken();
                    String auth = "Bearer " + bearerToken;
                    params.put("Authorization", auth);
                }

                return new CustomHttpDataSourceFactory(MainApp.Companion.getUserAgent(), bandwidthMeter, params);

            } catch (AuthenticatorException | IOException | OperationCanceledException e) {
                Timber.e(e);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(MediaSource mediaSource) {
        super.onPostExecute(mediaSource);
        if (mediaSource != null) {
            OnPrepareVideoPlayerTaskListener listener = mListener.get();
            if (listener != null) {
                listener.OnPrepareVideoPlayerTaskCallback(mediaSource);
            }
        }
    }

        public interface OnPrepareVideoPlayerTaskListener {

        void OnPrepareVideoPlayerTaskCallback(MediaSource mediaSource);
    }
}
