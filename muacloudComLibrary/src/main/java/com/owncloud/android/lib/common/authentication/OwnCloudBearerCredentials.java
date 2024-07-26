package com.owncloud.android.lib.common.authentication;

import com.owncloud.android.lib.common.http.HttpConstants;

public class OwnCloudBearerCredentials implements OwnCloudCredentials {

    private String mUsername;
    private String mAccessToken;

    public OwnCloudBearerCredentials(String username, String accessToken) {
        mUsername = username != null ? username : "";
        mAccessToken = accessToken != null ? accessToken : "";
    }

    @Override
    public String getUsername() {

        return mUsername;
    }

    @Override
    public String getAuthToken() {
        return mAccessToken;
    }

    @Override
    public String getHeaderAuth() {
        return HttpConstants.BEARER_AUTHORIZATION_KEY + mAccessToken;
    }

    @Override
    public boolean authTokenExpires() {
        return true;
    }

    @Override
    public boolean authTokenCanBeRefreshed() {
        return true;
    }
}