

package com.owncloud.android.syncadapter;

import android.accounts.Account;
import android.accounts.AccountsException;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.owncloud.android.R;
import com.owncloud.android.domain.UseCaseResult;
import com.owncloud.android.domain.capabilities.usecases.RefreshCapabilitiesFromServerAsyncUseCase;
import com.owncloud.android.domain.exceptions.UnauthorizedException;
import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.domain.files.usecases.GetPersonalRootFolderForAccountUseCase;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.usecases.synchronization.SynchronizeFolderUseCase;
import com.owncloud.android.utils.NotificationUtils;
import kotlin.Lazy;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import java.io.IOException;

import static com.owncloud.android.utils.NotificationConstantsKt.FILE_SYNC_NOTIFICATION_CHANNEL_ID;
import static org.koin.java.KoinJavaComponent.inject;


public class FileSyncAdapter extends AbstractOwnCloudSyncAdapter {

    public static final String EVENT_FULL_SYNC_START = FileSyncAdapter.class.getName() +
            ".EVENT_FULL_SYNC_START";
    public static final String EVENT_FULL_SYNC_END = FileSyncAdapter.class.getName() +
            ".EVENT_FULL_SYNC_END";
    public static final String EVENT_FULL_SYNC_FOLDER_CONTENTS_SYNCED =
            FileSyncAdapter.class.getName() + ".EVENT_FULL_SYNC_FOLDER_CONTENTS_SYNCED";

    public static final String EXTRA_ACCOUNT_NAME = FileSyncAdapter.class.getName() + ".EXTRA_ACCOUNT_NAME";
    public static final String EXTRA_FOLDER_PATH = FileSyncAdapter.class.getName() + ".EXTRA_FOLDER_PATH";
    public static final String EXTRA_SERVER_VERSION = FileSyncAdapter.class.getName() + ".EXTRA_SERVER_VERSION";
    public static final String EXTRA_RESULT = FileSyncAdapter.class.getName() + ".EXTRA_RESULT";

    
    private boolean mCancellation;

    
    private int mFailedResultsCounter;

    
    private Throwable mLastFailedThrowable;

    
    private SyncResult mSyncResult;

    
    private LocalBroadcastManager mLocalBroadcastManager;

    
    public FileSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    
    @Override
    public synchronized void onPerformSync(Account account, Bundle extras,
                                           String authority, ContentProviderClient providerClient,
                                           SyncResult syncResult) {

        mCancellation = false;
                boolean isManualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        mFailedResultsCounter = 0;
        mSyncResult = syncResult;
        mSyncResult.fullSyncRequested = false;
        mSyncResult.delayUntil = (System.currentTimeMillis() / 1000) + 3 * 60 * 60; // avoid too many automatic


        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());

        this.setAccount(account);

        try {
            this.initClientForCurrentAccount();
        } catch (IOException | AccountsException e) {


            mSyncResult.tooManyRetries = true;
            notifyFailedSynchronization();
            return;
        }

        Timber.d("Synchronization of ownCloud account " + account.name + " starting");
        sendLocalBroadcast(EVENT_FULL_SYNC_START, null, null);  // message to signal the start

        try {
            updateCapabilities();
            if (!mCancellation) {
                @NotNull Lazy<GetPersonalRootFolderForAccountUseCase> getRootFolderPersonalUseCaseLazy =
                        inject(GetPersonalRootFolderForAccountUseCase.class);
                GetPersonalRootFolderForAccountUseCase.Params params = new GetPersonalRootFolderForAccountUseCase.Params(account.name);

                OCFile rootFolder = getRootFolderPersonalUseCaseLazy.getValue().invoke(params);
                if (rootFolder != null) {
                    synchronizeFolder(rootFolder);
                }

            } else {
                Timber.d("Leaving synchronization before synchronizing the root folder because cancelation request");
            }

        } finally {



            if (mFailedResultsCounter > 0 && isManualSync) {



                mSyncResult.tooManyRetries = true;

                notifyFailedSynchronization();
            }
        }

    }

    
    @Override
    public void onSyncCanceled() {
        Timber.d("Synchronization of " + getAccount().name + " has been requested to cancel");
        mCancellation = true;
        super.onSyncCanceled();
    }

    
    private void updateCapabilities() {
        @NotNull Lazy<RefreshCapabilitiesFromServerAsyncUseCase> refreshCapabilitiesFromServerAsyncUseCase =
                inject(RefreshCapabilitiesFromServerAsyncUseCase.class);
        RefreshCapabilitiesFromServerAsyncUseCase.Params params = new RefreshCapabilitiesFromServerAsyncUseCase.Params(getAccount().name);
        UseCaseResult<Unit> useCaseResult = refreshCapabilitiesFromServerAsyncUseCase.getValue().invoke(params);

        if (useCaseResult.isError()) {
            mLastFailedThrowable = useCaseResult.getThrowableOrNull();
        }
    }

    
    private void synchronizeFolder(OCFile folder) {

        @NotNull Lazy<SynchronizeFolderUseCase> synchronizeFolderUseCase =
                inject(SynchronizeFolderUseCase.class);
        SynchronizeFolderUseCase.Params params = new SynchronizeFolderUseCase.Params(
                folder.getRemotePath(),
                folder.getOwner(),
                folder.getSpaceId(),
                SynchronizeFolderUseCase.SyncFolderMode.REFRESH_FOLDER_RECURSIVELY,
                false);
        UseCaseResult<Unit> useCaseResult;

        useCaseResult = synchronizeFolderUseCase.getValue().invoke(params);

        if (useCaseResult.getThrowableOrNull() != null) {
            if (useCaseResult.getThrowableOrNull() instanceof UnauthorizedException) {
                mSyncResult.stats.numAuthExceptions++;
            }
            mFailedResultsCounter++;
        }
    }

    
    private void sendLocalBroadcast(String event, String dirRemotePath, RemoteOperationResult result) {
        Timber.d("Send broadcast %s", event);
        Intent intent = new Intent(event);
        intent.putExtra(FileSyncAdapter.EXTRA_ACCOUNT_NAME, getAccount().name);
        if (dirRemotePath != null) {
            intent.putExtra(FileSyncAdapter.EXTRA_FOLDER_PATH, dirRemotePath);
        }
        if (result != null) {
            intent.putExtra(FileSyncAdapter.EXTRA_RESULT, result);
        }
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    
    private void notifyFailedSynchronization() {
        NotificationCompat.Builder notificationBuilder = createNotificationBuilder();
        boolean needsToUpdateCredentials = (
                mLastFailedThrowable != null &&
                        mLastFailedThrowable instanceof UnauthorizedException
        );
        if (needsToUpdateCredentials) {

            PendingIntent pendingIntentToRefreshCredentials =
                    NotificationUtils.INSTANCE.composePendingIntentToRefreshCredentials(getContext(), getAccount());

            notificationBuilder
                    .setTicker(i18n(R.string.sync_fail_ticker_unauthorized))
                    .setContentTitle(i18n(R.string.sync_fail_ticker_unauthorized))
                    .setContentIntent(pendingIntentToRefreshCredentials)
                    .setContentText(i18n(R.string.sync_fail_content_unauthorized, getAccount().name));
        } else {
            notificationBuilder
                    .setTicker(i18n(R.string.sync_fail_ticker))
                    .setContentTitle(i18n(R.string.sync_fail_ticker))
                    .setContentText(i18n(R.string.sync_fail_content, getAccount().name));
        }

        showNotification(R.string.sync_fail_ticker, notificationBuilder);
    }

    
    private NotificationCompat.Builder createNotificationBuilder() {
        NotificationCompat.Builder notificationBuilder = NotificationUtils.newNotificationBuilder(getContext(),
                FILE_SYNC_NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true);
        return notificationBuilder;
    }

    
    private void showNotification(int id, NotificationCompat.Builder builder) {

        NotificationManager mNotificationManager = ((NotificationManager) getContext().
                getSystemService(Context.NOTIFICATION_SERVICE));

        mNotificationManager.notify(id, builder.build());
    }

    
    private String i18n(int key, Object... args) {
        return getContext().getString(key, args);
    }
}
