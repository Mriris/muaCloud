
package com.owncloud.android.lib.common.network;

import com.owncloud.android.lib.common.http.HttpConstants;

import java.util.Arrays;


public class RedirectionPath {

    private int[] mStatuses = null;

    private int mLastStatus = -1;

    private String[] mLocations = null;

    private int mLastLocation = -1;


    public RedirectionPath(int status, int maxRedirections) {
        if (maxRedirections < 0) {
            throw new IllegalArgumentException("maxRedirections MUST BE zero or greater");
        }
        mStatuses = new int[maxRedirections + 1];
        Arrays.fill(mStatuses, -1);
        mStatuses[++mLastStatus] = status;
    }


    public void addLocation(String location) {
        if (mLocations == null) {
            mLocations = new String[mStatuses.length - 1];
        }
        if (mLastLocation < mLocations.length - 1) {
            mLocations[++mLastLocation] = location;
        }
    }


    public void addStatus(int status) {
        if (mLastStatus < mStatuses.length - 1) {
            mStatuses[++mLastStatus] = status;
        }
    }


    public int getLastStatus() {
        return mStatuses[mLastStatus];
    }


    public String getLastPermanentLocation() {
        for (int i = mLastStatus; i >= 0; i--) {
            if (mStatuses[i] == HttpConstants.HTTP_MOVED_PERMANENTLY && i <= mLastLocation) {
                return mLocations[i];
            }
        }
        return null;
    }


    public int getRedirectionsCount() {
        return mLastLocation + 1;
    }

}
