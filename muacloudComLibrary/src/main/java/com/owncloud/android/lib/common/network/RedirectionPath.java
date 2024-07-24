/* ownCloud Android Library is available under MIT license
 *
 *   @author David A. Velasco
 *
 *   Copyright (C) 2016 ownCloud GmbH.
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

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
