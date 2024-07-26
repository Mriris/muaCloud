

package com.owncloud.android.ui.fragment;

import android.content.Context;

import androidx.fragment.app.Fragment;
import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.ui.activity.ComponentsGetter;

import static com.owncloud.android.usecases.transfers.TransferConstantsKt.DOWNLOAD_ADDED_MESSAGE;
import static com.owncloud.android.usecases.transfers.TransferConstantsKt.DOWNLOAD_FINISH_MESSAGE;
import static com.owncloud.android.usecases.transfers.TransferConstantsKt.UPLOAD_FINISH_MESSAGE;
import static com.owncloud.android.usecases.transfers.TransferConstantsKt.UPLOAD_START_MESSAGE;


public abstract class FileFragment extends Fragment {

    private OCFile mFile;

    protected ContainerActivity mContainerActivity;


    public FileFragment() {
        mFile = null;
    }


    public OCFile getFile() {
        return mFile;
    }

    protected void setFile(OCFile file) {
        mFile = file;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mContainerActivity = (ContainerActivity) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement " +
                    ContainerActivity.class.getSimpleName());
        }
    }


    @Override
    public void onDetach() {
        mContainerActivity = null;
        super.onDetach();
    }

    public void onSyncEvent(String syncEvent, boolean success, OCFile updatedFile) {
        if (syncEvent.equals(UPLOAD_START_MESSAGE)) {
            updateViewForSyncInProgress();
        } else if (syncEvent.equals(UPLOAD_FINISH_MESSAGE)) {
            if (success) {
                if (updatedFile != null) {
                    onFileMetadataChanged(updatedFile);
                } else {
                    onFileMetadataChanged();
                }
            }
            updateViewForSyncOff();

        } else if (syncEvent.equals(DOWNLOAD_ADDED_MESSAGE)) {
            updateViewForSyncInProgress();

        } else if (syncEvent.equals(DOWNLOAD_FINISH_MESSAGE)) {
            if (success) {
                if (updatedFile != null) {
                    onFileMetadataChanged(updatedFile);
                } else {
                    onFileMetadataChanged();
                }
                onFileContentChanged();
            }
            updateViewForSyncOff();
        }
    }

    public abstract void updateViewForSyncInProgress();

    public abstract void updateViewForSyncOff();

    public abstract void onFileMetadataChanged(OCFile updatedFile);

    public abstract void onFileMetadataChanged();

    public abstract void onFileContentChanged();


    public interface ContainerActivity extends ComponentsGetter {


        void showDetails(OCFile file);



    }
}
