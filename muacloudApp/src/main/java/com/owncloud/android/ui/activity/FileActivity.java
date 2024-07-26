

package com.owncloud.android.ui.activity;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.snackbar.Snackbar;
import com.owncloud.android.R;
import com.owncloud.android.domain.files.model.FileListOption;
import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.lib.common.network.CertificateCombinedException;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.presentation.authentication.AccountUtils;
import com.owncloud.android.presentation.authentication.AuthenticatorConstants;
import com.owncloud.android.presentation.authentication.LoginActivity;
import com.owncloud.android.services.OperationsService;
import com.owncloud.android.services.OperationsService.OperationsServiceBinder;
import com.owncloud.android.ui.dialog.ConfirmationDialogFragment;
import com.owncloud.android.ui.dialog.SslUntrustedCertDialog;
import com.owncloud.android.ui.errorhandling.ErrorMessageAdapter;
import com.owncloud.android.ui.helpers.FileOperationsHelper;
import timber.log.Timber;


public class FileActivity extends DrawerActivity
        implements OnRemoteOperationListener, ComponentsGetter, SslUntrustedCertDialog.OnSslUntrustedCertListener {

    public static final String EXTRA_FILE = "com.owncloud.android.ui.activity.FILE";
    public static final String EXTRA_ACCOUNT = "com.owncloud.android.ui.activity.ACCOUNT";
    public static final String EXTRA_FROM_NOTIFICATION =
            "com.owncloud.android.ui.activity.FROM_NOTIFICATION";
    public static final String EXTRA_FILE_LIST_OPTION = "EXTRA_FILE_LIST_OPTION";

    private static final String KEY_WAITING_FOR_OP_ID = "WAITING_FOR_OP_ID";
    private static final String KEY_ACTION_BAR_TITLE = "ACTION_BAR_TITLE";

    public static final int REQUEST_CODE__UPDATE_CREDENTIALS = 0;
    public static final int REQUEST_CODE__LAST_SHARED = REQUEST_CODE__UPDATE_CREDENTIALS;

    protected static final long DELAY_TO_REQUEST_OPERATIONS_LATER = 200;

        private static final String DIALOG_UNTRUSTED_CERT = "DIALOG_UNTRUSTED_CERT";
    private static final String DIALOG_CERT_NOT_SAVED = "DIALOG_CERT_NOT_SAVED";


    private OCFile mFile;


    private boolean mFromNotification;


    private Handler mHandler;

    private FileOperationsHelper mFileOperationsHelper;

    private ServiceConnection mOperationsServiceConnection = null;

    private OperationsServiceBinder mOperationsServiceBinder = null;

    private boolean mResumed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mFileOperationsHelper = new FileOperationsHelper(this);
        Account account = null;
        if (savedInstanceState != null) {
            mFile = savedInstanceState.getParcelable(FileActivity.EXTRA_FILE);
            mFromNotification = savedInstanceState.getBoolean(FileActivity.EXTRA_FROM_NOTIFICATION);
            mFileOperationsHelper.setOpIdWaitingFor(
                    savedInstanceState.getLong(KEY_WAITING_FOR_OP_ID, Long.MAX_VALUE)
            );
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(savedInstanceState.getString(KEY_ACTION_BAR_TITLE));
            }
        } else {
            account = getIntent().getParcelableExtra(FileActivity.EXTRA_ACCOUNT);
            mFile = getIntent().getParcelableExtra(FileActivity.EXTRA_FILE);
            mFromNotification = getIntent().getBooleanExtra(FileActivity.EXTRA_FROM_NOTIFICATION,
                    false);
        }

        AccountUtils.updateAccountVersion(this); // best place, before any access to AccountManager


        setAccount(account, savedInstanceState != null);

        mOperationsServiceConnection = new OperationsServiceConnection();
        bindService(new Intent(this, OperationsService.class), mOperationsServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
        if (mOperationsServiceBinder != null) {
            doOnResumeAndBound();
        }
    }

    @Override
    protected void onPause() {
        if (mOperationsServiceBinder != null) {
            mOperationsServiceBinder.removeOperationListener(this);
        }
        mResumed = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mOperationsServiceConnection != null) {
            unbindService(mOperationsServiceConnection);
            mOperationsServiceBinder = null;
        }
        super.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FileActivity.EXTRA_FILE, mFile);
        outState.putBoolean(FileActivity.EXTRA_FROM_NOTIFICATION, mFromNotification);
        outState.putLong(KEY_WAITING_FOR_OP_ID, mFileOperationsHelper.getOpIdWaitingFor());
        if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {


            outState.putString(KEY_ACTION_BAR_TITLE, getSupportActionBar().getTitle().toString());
        }
    }


    public OCFile getFile() {
        return mFile;
    }


    public void setFile(OCFile file) {
        mFile = file;
    }


    public boolean fromNotification() {
        return mFromNotification;
    }

    public OperationsServiceBinder getOperationsServiceBinder() {
        return mOperationsServiceBinder;
    }

    public OnRemoteOperationListener getRemoteOperationListener() {
        return this;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public FileOperationsHelper getFileOperationsHelper() {
        return mFileOperationsHelper;
    }


    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        Timber.d("Received result of operation in FileActivity - common behaviour for all the FileActivities");

        mFileOperationsHelper.setOpIdWaitingFor(Long.MAX_VALUE);

        dismissLoadingDialog();

        if (!result.isSuccess() && (
                result.getCode() == ResultCode.UNAUTHORIZED ||
                        (result.isException() && result.getException() instanceof AuthenticatorException)
        )) {

            requestCredentialsUpdate();

            if (result.getCode() == ResultCode.UNAUTHORIZED) {
                showSnackMessage(
                        ErrorMessageAdapter.Companion.getResultMessage(result, operation, getResources())
                );
            }

        } else if (!result.isSuccess() && ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED.equals(result.getCode())) {

            showUntrustedCertDialog(result);

        }
    }

    protected void showRequestAccountChangeNotice(String errorMessage, boolean mustChange) {
        if (mustChange) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.auth_failure_snackbar_action)
                    .setMessage(errorMessage)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> requestCredentialsUpdate())
                    .setIcon(R.drawable.common_error_grey)
                    .setCancelable(false)
                    .show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.auth_oauth_failure_snackbar_action, v ->
                            requestCredentialsUpdate())
                    .show();
        }
    }

    protected void showRequestRegainAccess() {
        Snackbar.make(findViewById(android.R.id.content), R.string.auth_oauth_failure, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.auth_oauth_failure_snackbar_action, v ->
                        requestCredentialsUpdate())
                .show();
    }


    protected void requestCredentialsUpdate() {
        requestCredentialsUpdate(null);
    }


    protected void requestCredentialsUpdate(Account account) {

        if (account == null) {
            account = getAccount();
        }

        Intent updateAccountCredentials = new Intent(this, LoginActivity.class);
        updateAccountCredentials.putExtra(AuthenticatorConstants.EXTRA_ACCOUNT, account);
        updateAccountCredentials.putExtra(
                AuthenticatorConstants.EXTRA_ACTION,
                AuthenticatorConstants.ACTION_UPDATE_EXPIRED_TOKEN);
        updateAccountCredentials.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(updateAccountCredentials, REQUEST_CODE__UPDATE_CREDENTIALS);
    }


    public void showUntrustedCertDialog(RemoteOperationResult result) {

        FragmentManager fm = getSupportFragmentManager();
        SslUntrustedCertDialog dialog = (SslUntrustedCertDialog) fm.findFragmentByTag(DIALOG_UNTRUSTED_CERT);
        if (dialog == null) {
            dialog = SslUntrustedCertDialog.newInstanceForFullSslError(
                    (CertificateCombinedException) result.getException());
            FragmentTransaction ft = fm.beginTransaction();
            dialog.show(ft, DIALOG_UNTRUSTED_CERT);
        }
    }


    public void showUntrustedCertDialogForThrowable(Throwable throwable) {

        FragmentManager fm = getSupportFragmentManager();
        SslUntrustedCertDialog dialog = (SslUntrustedCertDialog) fm.findFragmentByTag(DIALOG_UNTRUSTED_CERT);
        if (dialog == null) {
            dialog = SslUntrustedCertDialog.newInstanceForFullSslError(
                    (CertificateCombinedException) throwable);
            FragmentTransaction ft = fm.beginTransaction();
            dialog.show(ft, DIALOG_UNTRUSTED_CERT);
        }
    }

    protected void updateFileFromDB() {
        OCFile file = getFile();
        if (file != null) {
            file = getStorageManager().getFileByPath(file.getRemotePath(), file.getSpaceId());
            setFile(file);
        }
    }

    private void doOnResumeAndBound() {
        mOperationsServiceBinder.addOperationListener(FileActivity.this, mHandler);
        long waitingForOpId = mFileOperationsHelper.getOpIdWaitingFor();
        if (waitingForOpId <= Integer.MAX_VALUE) {
            boolean wait = mOperationsServiceBinder.dispatchResultIfFinished((int) waitingForOpId,
                    this);
            if (!wait) {
                dismissLoadingDialog();
            }
        }
    }


    private class OperationsServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName component, IBinder service) {
            if (component.equals(new ComponentName(FileActivity.this, OperationsService.class))) {
                Timber.d("Operations service connected");
                mOperationsServiceBinder = (OperationsServiceBinder) service;
                if (mResumed) {
                    doOnResumeAndBound();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName component) {
            if (component.equals(new ComponentName(FileActivity.this, OperationsService.class))) {
                Timber.d("Operations service disconnected");
                mOperationsServiceBinder = null;

            }
        }
    }

    @Override
    public void restart() {
        Intent i = new Intent(this, FileDisplayActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(EXTRA_FILE_LIST_OPTION, (Parcelable) FileListOption.ALL_FILES);
        startActivity(i);
    }

    @Override
    public void navigateToOption(FileListOption fileListOption) {
        Intent intent;
        switch (fileListOption) {
            case ALL_FILES:
                restart();
                break;
            case SPACES_LIST:
                intent = new Intent(this, FileDisplayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(EXTRA_FILE_LIST_OPTION, (Parcelable) FileListOption.SPACES_LIST);
                startActivity(intent);
                break;
            case SHARED_BY_LINK:
                intent = new Intent(this, FileDisplayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(EXTRA_FILE_LIST_OPTION, (Parcelable) FileListOption.SHARED_BY_LINK);
                startActivity(intent);
                break;
            case AV_OFFLINE:
                intent = new Intent(this, FileDisplayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(EXTRA_FILE_LIST_OPTION, (Parcelable) FileListOption.AV_OFFLINE);
                startActivity(intent);
                break;
        }
    }

    protected OCFile getCurrentDir() {
        OCFile file = getFile();
        if (file != null) {
            if (file.isFolder()) {
                return file;
            } else if (getStorageManager() != null) {
                String parentPath = file.getParentRemotePath();
                return getStorageManager().getFileByPath(parentPath, file.getSpaceId());
            }
        }
        return null;
    }

    
    @Override
    public void onSavedCertificate() {

    }

    @Override
    public void onFailedSavingCertificate() {
        ConfirmationDialogFragment dialog = ConfirmationDialogFragment.newInstance(
                R.string.ssl_validator_not_saved, new String[]{}, 0, android.R.string.ok, -1, -1
        );
        dialog.show(getSupportFragmentManager(), DIALOG_CERT_NOT_SAVED);
    }

    @Override
    public void onCancelCertificate() {

    }
}
