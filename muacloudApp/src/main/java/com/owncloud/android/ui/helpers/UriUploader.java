
package com.owncloud.android.ui.helpers;

import android.accounts.Account;
import android.content.ContentResolver;
import android.net.Uri;

import androidx.fragment.app.FragmentManager;
import com.owncloud.android.R;
import com.owncloud.android.ui.activity.FileActivity;
import com.owncloud.android.ui.asynctasks.CopyAndUploadContentUrisTask;
import com.owncloud.android.ui.fragment.TaskRetainerFragment;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.List;


public class UriUploader {

    private final FileActivity mActivity;
    private final ArrayList<Uri> mUrisToUpload;
    private final CopyAndUploadContentUrisTask.OnCopyTmpFilesTaskListener mCopyTmpTaskListener;

    private final String mUploadPath;
    private final Account mAccount;
    private final String mSpaceId;
    private final boolean mShowWaitingDialog;

    private UriUploaderResultCode mCode = UriUploaderResultCode.OK;

    public enum UriUploaderResultCode {
        OK,
        COPY_THEN_UPLOAD,
        ERROR_UNKNOWN,
        ERROR_NO_FILE_TO_UPLOAD,
        ERROR_READ_PERMISSION_NOT_GRANTED
    }

    public UriUploader(
            FileActivity activity,
            ArrayList<Uri> uris,
            String uploadPath,
            Account account,
            String spaceId,
            boolean showWaitingDialog,
            CopyAndUploadContentUrisTask.OnCopyTmpFilesTaskListener copyTmpTaskListener
    ) {
        mActivity = activity;
        mUrisToUpload = uris;
        mUploadPath = uploadPath;
        mAccount = account;
        mSpaceId = spaceId;
        mShowWaitingDialog = showWaitingDialog;
        mCopyTmpTaskListener = copyTmpTaskListener;
    }

    public UriUploaderResultCode uploadUris() {

        try {
            List<Uri> contentUris = new ArrayList<>();

            int schemeFileCounter = 0;

            for (Uri sourceUri : mUrisToUpload) {
                if (sourceUri != null) {

                    if (ContentResolver.SCHEME_CONTENT.equals(sourceUri.getScheme())) {
                        contentUris.add(sourceUri);

                    } else if (ContentResolver.SCHEME_FILE.equals(sourceUri.getScheme())) {
                        schemeFileCounter++;
                        Timber.w("File with scheme file has been received. We don't support this scheme anymore.");
                    }
                }
            }

            if (!contentUris.isEmpty()) {

                copyThenUpload(contentUris.toArray(new Uri[0]), mUploadPath, mSpaceId);


                mCode = UriUploaderResultCode.COPY_THEN_UPLOAD;

            } else if (schemeFileCounter == 0) {
                mCode = UriUploaderResultCode.ERROR_NO_FILE_TO_UPLOAD;
            }

        } catch (SecurityException e) {
            mCode = UriUploaderResultCode.ERROR_READ_PERMISSION_NOT_GRANTED;
            Timber.e(e, "Permissions fail");

        } catch (Exception e) {
            mCode = UriUploaderResultCode.ERROR_UNKNOWN;
            Timber.e(e, "Unexpected error");

        }
        return mCode;
    }


    private void copyThenUpload(Uri[] sourceUris, String uploadPath, String spaceId) {
        if (mShowWaitingDialog) {
            mActivity.showLoadingDialog(R.string.wait_for_tmp_copy_from_private_storage);
        }

        CopyAndUploadContentUrisTask copyTask = new CopyAndUploadContentUrisTask
                (mCopyTmpTaskListener, mActivity);

        FragmentManager fm = mActivity.getSupportFragmentManager();

        TaskRetainerFragment taskRetainerFragment =
                (TaskRetainerFragment) fm.findFragmentByTag(TaskRetainerFragment.FTAG_TASK_RETAINER_FRAGMENT);

        if (taskRetainerFragment != null) {
            taskRetainerFragment.setTask(copyTask);
        }

        copyTask.execute(
                CopyAndUploadContentUrisTask.makeParamsToExecute(
                        mAccount,
                        sourceUris,
                        uploadPath,
                        mActivity.getContentResolver(),
                        spaceId
                )
        );
    }
}
