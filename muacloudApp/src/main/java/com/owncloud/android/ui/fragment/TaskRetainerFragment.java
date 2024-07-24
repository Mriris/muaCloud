
package com.owncloud.android.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import com.owncloud.android.ui.activity.ReceiveExternalFilesActivity;
import com.owncloud.android.ui.asynctasks.CopyAndUploadContentUrisTask;


public class TaskRetainerFragment extends Fragment {

    public static final String FTAG_TASK_RETAINER_FRAGMENT = "TASK_RETAINER_FRAGMENT";

    private CopyAndUploadContentUrisTask mTask;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mTask != null) {
            if (context instanceof ReceiveExternalFilesActivity) {
                mTask.setListener((CopyAndUploadContentUrisTask.OnCopyTmpFilesTaskListener) context);
            } else {
                mTask.setListener(null);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);    // the key point
    }


    public void setTask(CopyAndUploadContentUrisTask task) {
        if (mTask != null) {
            mTask.setListener(null);
        }
        mTask = task;
        Context context = getContext();
        if (mTask != null && context != null) {
            if (context instanceof ReceiveExternalFilesActivity) {
                task.setListener((CopyAndUploadContentUrisTask.OnCopyTmpFilesTaskListener) context);
            } else {
                task.setListener(null);
            }
        }
    }
}
