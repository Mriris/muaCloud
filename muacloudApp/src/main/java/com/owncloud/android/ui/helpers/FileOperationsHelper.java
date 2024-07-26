

package com.owncloud.android.ui.helpers;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.owncloud.android.R;
import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.domain.sharing.shares.model.OCShare;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.presentation.common.ShareSheetHelper;
import com.owncloud.android.presentation.sharing.ShareActivity;
import com.owncloud.android.services.OperationsService;
import com.owncloud.android.ui.activity.FileActivity;
import com.owncloud.android.usecases.synchronization.SynchronizeFileUseCase;
import com.owncloud.android.usecases.synchronization.SynchronizeFolderUseCase;
import com.owncloud.android.utils.UriUtilsKt;
import kotlin.Lazy;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import static org.koin.java.KoinJavaComponent.inject;

public class FileOperationsHelper {

    private static final String FTAG_CHOOSER_DIALOG = "CHOOSER_DIALOG";

    private final FileActivity mFileActivity;

    private long mWaitingForOpId = Long.MAX_VALUE;

    public FileOperationsHelper(FileActivity fileActivity) {
        mFileActivity = fileActivity;
    }

    private Intent getIntentForSavedMimeType(Uri data, String type, boolean hasWritePermission) {
        Intent intentForSavedMimeType = new Intent(Intent.ACTION_VIEW);
        intentForSavedMimeType.setDataAndType(data, type);
        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        if (hasWritePermission) {
            flags = flags | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        }
        intentForSavedMimeType.setFlags(flags);
        return intentForSavedMimeType;
    }

    private Intent getIntentForGuessedMimeType(String storagePath, String type, Uri data, boolean hasWritePermission) {
        Intent intentForGuessedMimeType = null;

        if (storagePath != null && storagePath.lastIndexOf('.') >= 0) {
            String guessedMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(storagePath.substring(storagePath.lastIndexOf('.') + 1));

            if (guessedMimeType != null && !guessedMimeType.equals(type)) {
                intentForGuessedMimeType = new Intent(Intent.ACTION_VIEW);
                intentForGuessedMimeType.setDataAndType(data, guessedMimeType);
                int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                if (hasWritePermission) {
                    flags = flags | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                }
                intentForGuessedMimeType.setFlags(flags);
            }
        }
        return intentForGuessedMimeType;
    }

    public void openFile(OCFile ocFile) {
        if (ocFile != null) {
            Intent intentForSavedMimeType = getIntentForSavedMimeType(UriUtilsKt.INSTANCE.getExposedFileUriForOCFile(mFileActivity, ocFile),
                    ocFile.getMimeType(), ocFile.getHasWritePermission());

            Intent intentForGuessedMimeType = getIntentForGuessedMimeType(ocFile.getStoragePath(), ocFile.getMimeType(),
                    UriUtilsKt.INSTANCE.getExposedFileUriForOCFile(mFileActivity, ocFile), ocFile.getHasWritePermission());

            openFileWithIntent(intentForSavedMimeType, intentForGuessedMimeType);

        } else {
            Timber.e("Trying to open a NULL OCFile");
        }
    }

    private void openFileWithIntent(Intent intentForSavedMimeType, Intent intentForGuessedMimeType) {
        Intent openFileWithIntent;

        if (intentForGuessedMimeType != null) {
            openFileWithIntent = intentForGuessedMimeType;
        } else {
            openFileWithIntent = intentForSavedMimeType;
        }
        try {
            mFileActivity.startActivity(Intent.createChooser(openFileWithIntent, mFileActivity.getString(R.string.actionbar_open_with)));
        } catch (ActivityNotFoundException anfe) {
            mFileActivity.showSnackMessage(mFileActivity.getString(
                    R.string.file_list_no_app_for_file_type
            ));
        }
    }


    public void copyOrSendPrivateLink(OCFile file) {

        String privateLink = file.getPrivateLink();

        if (privateLink == null || privateLink.isEmpty()) {
            mFileActivity.showSnackMessage(
                    mFileActivity.getString(R.string.file_private_link_error)
            );
            return;
        }

        shareLink(privateLink);
    }


    public void copyOrSendPublicLink(OCShare share) {
        String link = share.getShareLink();
        if (link.length() <= 0) {
            mFileActivity.showSnackMessage(
                    mFileActivity.getString(R.string.share_no_link_in_this_share)
            );
            return;
        }

        shareLink(link);
    }


    public void showShareFile(OCFile file) {
        Intent intent = new Intent(mFileActivity, ShareActivity.class);
        intent.putExtra(FileActivity.EXTRA_FILE, file);
        intent.putExtra(FileActivity.EXTRA_ACCOUNT, mFileActivity.getAccount());
        mFileActivity.startActivity(intent);

    }


    @Deprecated
    public void syncFile(OCFile file) {
        if (!file.isFolder()) {
            @NotNull Lazy<SynchronizeFileUseCase> synchronizeFileUseCaseLazy = inject(SynchronizeFileUseCase.class);
            synchronizeFileUseCaseLazy.getValue().invoke(
                    new SynchronizeFileUseCase.Params(file)
            );
        } else {
            @NotNull Lazy<SynchronizeFolderUseCase> synchronizeFolderUseCaseLazy = inject(SynchronizeFolderUseCase.class);
            synchronizeFolderUseCaseLazy.getValue().invoke(
                    new SynchronizeFolderUseCase.Params(
                            file.getRemotePath(),
                            file.getOwner(),
                            file.getSpaceId(),
                            SynchronizeFolderUseCase.SyncFolderMode.SYNC_FOLDER_RECURSIVELY,
                            false)
            );
        }
    }

    public long getOpIdWaitingFor() {
        return mWaitingForOpId;
    }

    public void setOpIdWaitingFor(long waitingForOpId) {
        mWaitingForOpId = waitingForOpId;
    }


    public void checkCurrentCredentials(Account account) {
        Intent service = new Intent(mFileActivity, OperationsService.class);
        service.setAction(OperationsService.ACTION_CHECK_CURRENT_CREDENTIALS);
        service.putExtra(OperationsService.EXTRA_ACCOUNT, account);
        mWaitingForOpId = mFileActivity.getOperationsServiceBinder().queueNewOperation(service);

        mFileActivity.showLoadingDialog(R.string.wait_checking_credentials);
    }


    private void shareLink(String link) {
        Intent intentToShareLink = new Intent(Intent.ACTION_SEND);
        intentToShareLink.putExtra(Intent.EXTRA_TEXT, link);
        intentToShareLink.setType("text/plain");
        String displayName = AccountManager.get(mFileActivity.getApplicationContext()).getUserData(
                mFileActivity.getAccount(),
                AccountUtils.Constants.KEY_DISPLAY_NAME
        );

        if (displayName != null) {
            intentToShareLink.putExtra(
                    Intent.EXTRA_SUBJECT,
                    mFileActivity.getString(
                            R.string.subject_user_shared_with_you,
                            displayName,
                            mFileActivity.getFile().getFileName()
                    )
            );
        } else {
            intentToShareLink.putExtra(
                    Intent.EXTRA_SUBJECT,
                    mFileActivity.getString(
                            R.string.subject_shared_with_you,
                            mFileActivity.getFile().getFileName()
                    )
            );
        }

        String[] packagesToExclude = new String[]{mFileActivity.getPackageName()};

        Intent shareSheetIntent = new ShareSheetHelper().getShareSheetIntent(
                intentToShareLink,
                mFileActivity.getApplicationContext(),
                R.string.activity_chooser_title,
                packagesToExclude
        );

        mFileActivity.startActivity(shareSheetIntent);
    }
}
