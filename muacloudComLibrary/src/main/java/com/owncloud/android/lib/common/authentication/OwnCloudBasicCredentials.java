package com.owncloud.android.lib.common.authentication;

import okhttp3.Credentials;

import static java.nio.charset.StandardCharsets.UTF_8;

public class OwnCloudBasicCredentials implements OwnCloudCredentials {

    private String mUsername;
    private String mPassword;

    public OwnCloudBasicCredentials(String username, String password) {
        mUsername = username != null ? username : "";
        mPassword = password != null ? password : "";
    }

    @Override
    public String getUsername() {
        return mUsername;
    }

    @Override
    public String getAuthToken() {
        return mPassword;
    }

    @Override
    public String getHeaderAuth() {
        return Credentials.basic(mUsername, mPassword, UTF_8);
    }

    @Override
    public boolean authTokenExpires() {
        return false;
    }

    @Override
    public boolean authTokenCanBeRefreshed() {
        return false;
    }
}