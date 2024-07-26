

package com.owncloud.android.ui.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import com.owncloud.android.R;
import com.owncloud.android.utils.FileStorageUtils;
import timber.log.Timber;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class FilesUploadHelper implements Parcelable {

    private String capturedPhotoPath;
    private File image = null;

    private Activity activity;
    private String accountName;

    public FilesUploadHelper(Activity activity, String accountName) {
        this.activity = activity;
        this.accountName = accountName;
    }

    protected FilesUploadHelper(Parcel in) {
        this.capturedPhotoPath = in.readString();
        this.image = (File) in.readSerializable();
    }

    public void init(Activity activity, String accountName) {
        this.activity = activity;
        this.accountName = accountName;
    }

    public interface OnCheckAvailableSpaceListener {
        void onCheckAvailableSpaceStart();

        void onCheckAvailableSpaceFinished(boolean hasEnoughSpace, String[] capturedFilePaths);
    }


    private class CheckAvailableSpaceTask extends AsyncTask<Void, Void, Boolean> {

        private final String[] checkedFilePaths;
        private final OnCheckAvailableSpaceListener callback;

        public CheckAvailableSpaceTask(String[] checkedFilePaths,
                                       OnCheckAvailableSpaceListener listener) {
            super();
            this.checkedFilePaths = checkedFilePaths;
            this.callback = listener;
        }


        @Override
        protected void onPreExecute() {
            callback.onCheckAvailableSpaceStart();
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            long total = 0;
            for (int i = 0; checkedFilePaths != null && i < checkedFilePaths.length; i++) {
                String localPath = checkedFilePaths[i];
                File localFile = new File(localPath);
                total += localFile.length();
            }
            return (FileStorageUtils.getUsableSpace() >= total);
        }


        @Override
        protected void onPostExecute(Boolean hasEnoughSpace) {
            callback.onCheckAvailableSpaceFinished(hasEnoughSpace, checkedFilePaths);
        }
    }

    public void checkIfAvailableSpace(String[] checkedFilePaths,
                                      OnCheckAvailableSpaceListener listener) {
        new CheckAvailableSpaceTask(checkedFilePaths, listener).execute();
    }

    public static String getCapturedImageName() {
        Calendar calendar = Calendar.getInstance();
        String year = "" + calendar.get(Calendar.YEAR);
        String month = ("0" + (calendar.get(Calendar.MONTH) + 1));
        String day = ("0" + calendar.get(Calendar.DAY_OF_MONTH));
        String hour = ("0" + calendar.get(Calendar.HOUR_OF_DAY));
        String minute = ("0" + calendar.get(Calendar.MINUTE));
        String second = ("0" + calendar.get(Calendar.SECOND));
        month = month.length() == 3 ? month.substring(1) : month;
        day = day.length() == 3 ? day.substring(1) : day;
        hour = hour.length() == 3 ? hour.substring(1) : hour;
        minute = minute.length() == 3 ? minute.substring(1) : minute;
        second = second.length() == 3 ? second.substring(1) : second;
        String newImageName = "IMG_" + year + month + day + "_" + hour + minute + second;
        return newImageName;
    }

    public File getCapturedImageFile() {
        File capturedImage = new File(capturedPhotoPath);
        File parent = capturedImage.getParentFile();
        File newImage = new File(parent, getCapturedImageName() + ".jpg");
        capturedImage.renameTo(newImage);
        capturedImage.delete();
        capturedPhotoPath = newImage.getAbsolutePath();
        return newImage;
    }

    private File createImageFile() {
        try {
            File storageDir = activity.getExternalCacheDir();
            image = File.createTempFile(getCapturedImageName(), ".jpg", storageDir);
            capturedPhotoPath = image.getAbsolutePath();
        } catch (IOException exception) {
            Timber.e(exception, exception.toString());
        }
        return image;
    }


    public void uploadFromCamera(final int requestCode) {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile();
        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(activity.getApplicationContext(),
                    activity.getResources().getString(R.string.file_provider_authority), photoFile);
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        }
        activity.startActivityForResult(pictureIntent, requestCode);
    }

    public void onActivityResult(final OnCheckAvailableSpaceListener callback) {
        checkIfAvailableSpace(new String[]{getCapturedImageFile().getAbsolutePath()}, callback);
    }

    public void deleteImageFile() {
        if (image != null) {
            image.delete();
            Timber.d("File deleted");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.capturedPhotoPath);
        dest.writeSerializable(this.image);
    }

    public static final Parcelable.Creator<FilesUploadHelper> CREATOR = new Parcelable.Creator<FilesUploadHelper>() {
        @Override
        public FilesUploadHelper createFromParcel(Parcel source) {
            return new FilesUploadHelper(source);
        }

        @Override
        public FilesUploadHelper[] newArray(int size) {
            return new FilesUploadHelper[size];
        }
    };
}
