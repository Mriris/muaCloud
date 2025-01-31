

package com.owncloud.android.ui.preview;



public class PreviewVideoError {

    private String errorMessage;

    private boolean fileSyncNeeded;

    private boolean parentFolderSyncNeeded;

    public PreviewVideoError(String errorMessage, boolean fileSyncNeeded,
                             boolean parentFolderSyncNeeded) {

        this.errorMessage = errorMessage;
        this.fileSyncNeeded = fileSyncNeeded;
        this.parentFolderSyncNeeded = parentFolderSyncNeeded;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isFileSyncNeeded() {
        return fileSyncNeeded;
    }

    public void setFileSyncNeeded(boolean fileSyncNeeded) {
        this.fileSyncNeeded = fileSyncNeeded;
    }

    public boolean isParentFolderSyncNeeded() {
        return parentFolderSyncNeeded;
    }

    public void setParentFolderSyncNeeded(boolean parentFolderSyncNeeded) {
        this.parentFolderSyncNeeded = parentFolderSyncNeeded;
    }
}
