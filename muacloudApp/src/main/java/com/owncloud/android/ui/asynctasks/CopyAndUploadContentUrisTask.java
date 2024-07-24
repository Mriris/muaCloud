
package com.owncloud.android.ui.asynctasks;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.owncloud.android.R;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.usecases.transfers.uploads.UploadFilesFromSystemUseCase;
import com.owncloud.android.utils.FileStorageUtils;
import com.owncloud.android.utils.UriUtils;
import kotlin.Lazy;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static org.koin.java.KoinJavaComponent.inject;


public class CopyAndUploadContentUrisTask extends AsyncTask<Object, Void, ResultCode> {

    
    public static Object[] makeParamsToExecute(
            Account account,
            Uri[] sourceUris,
            String uploadPath,
            ContentResolver contentResolver,
            String spaceId
    ) {

        return new Object[]{
                account,
                sourceUris,
                uploadPath,
                contentResolver,
                spaceId
        };
    }

    
    private WeakReference<OnCopyTmpFilesTaskListener> mListener;

    
    private final Context mAppContext;

    public CopyAndUploadContentUrisTask(
            OnCopyTmpFilesTaskListener listener,
            Context context
    ) {
        mListener = new WeakReference<>(listener);
        mAppContext = context.getApplicationContext();
    }

    
    @Override
    protected ResultCode doInBackground(Object[] params) {

        ResultCode result = ResultCode.UNKNOWN_ERROR;

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        String fullTempPath = null;
        Uri currentUri = null;

        try {
            Account account = (Account) params[0];
            Uri[] uris = (Uri[]) params[1];
            String uploadPath = (String) params[2];
            ContentResolver leakedContentResolver = (ContentResolver) params[3];
            String spaceId = (String) params[4];

            String currentRemotePath;
            ArrayList<String> filesToUpload = new ArrayList<>();

            for (int i = 0; i < uris.length; i++) {
                currentUri = uris[i];
                currentRemotePath = uploadPath + UriUtils.getDisplayNameForUri(currentUri, mAppContext);

                fullTempPath = FileStorageUtils.getTemporalPath(account.name, spaceId) + currentRemotePath;
                inputStream = leakedContentResolver.openInputStream(currentUri);
                File cacheFile = new File(fullTempPath);
                File tempDir = cacheFile.getParentFile();
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                cacheFile.createNewFile();
                outputStream = new FileOutputStream(fullTempPath);
                byte[] buffer = new byte[4096];

                int count;
                while ((count = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                }

                filesToUpload.add(fullTempPath);
                @NotNull Lazy<UploadFilesFromSystemUseCase> uploadFilesFromSystemUseCaseLazy = inject(UploadFilesFromSystemUseCase.class);
                UploadFilesFromSystemUseCase uploadFilesFromSystemUseCase = uploadFilesFromSystemUseCaseLazy.getValue();
                UploadFilesFromSystemUseCase.Params useCaseParams = new UploadFilesFromSystemUseCase.Params(
                        account.name,
                        filesToUpload,
                        uploadPath,
                        spaceId
                );
                uploadFilesFromSystemUseCase.invoke(useCaseParams);
                fullTempPath = null;
                filesToUpload.clear();
            }

            result = ResultCode.OK;

        } catch (ArrayIndexOutOfBoundsException e) {
            Timber.e(e, "Wrong number of arguments received");

        } catch (ClassCastException e) {
            Timber.e(e, "Wrong parameter received");

        } catch (FileNotFoundException e) {
            Timber.e(e, "Could not find source file %s", currentUri);
            result = ResultCode.LOCAL_FILE_NOT_FOUND;

        } catch (SecurityException e) {
            Timber.e(e, "Not enough permissions to read source file %s", currentUri);
            result = ResultCode.FORBIDDEN;

        } catch (Exception e) {
            Timber.e(e, "Exception while copying " + currentUri + " to temporary file");
            result = ResultCode.LOCAL_STORAGE_NOT_COPIED;

            // clean
            if (fullTempPath != null) {
                File f = new File(fullTempPath);
                if (f.exists()) {
                    if (!f.delete()) {
                        Timber.e("Could not delete temporary file %s", fullTempPath);
                    }
                }
            }

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Timber.w("Ignoring exception of inputStream closure");
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    Timber.w("Ignoring exception of outStream closure");
                }
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(ResultCode result) {
        OnCopyTmpFilesTaskListener listener = mListener.get();
        if (listener != null) {
            listener.onTmpFilesCopied(result);

        } else {
            Timber.i("User left the caller activity before the temporal copies were finished");
            if (result != ResultCode.OK) {
                // if the user left the app, report background error in a Toast
                String message;
                switch (result) {
                    case LOCAL_FILE_NOT_FOUND:
                        message = mAppContext.getString(R.string.uploader_error_message_source_file_not_found);
                        break;
                    case LOCAL_STORAGE_NOT_COPIED:
                        message = mAppContext.getString(R.string.uploader_error_message_source_file_not_copied);
                        break;
                    case FORBIDDEN:
                        String appName = mAppContext.getString(R.string.app_name);
                        message = mAppContext.getString(R.string.uploader_error_message_read_permission_not_granted, appName);
                        break;
                    default:
                        message = mAppContext.getString(R.string.common_error_unknown);
                }
                Toast.makeText(mAppContext, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    
    public void setListener(OnCopyTmpFilesTaskListener listener) {
        mListener = new WeakReference<>(listener);
    }

    
    public interface OnCopyTmpFilesTaskListener {
        void onTmpFilesCopied(ResultCode result);
    }
}
