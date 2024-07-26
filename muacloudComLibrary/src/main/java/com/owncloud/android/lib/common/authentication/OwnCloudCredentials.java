
package com.owncloud.android.lib.common.authentication;

public interface OwnCloudCredentials {

    String getUsername();

    String getAuthToken();

    String getHeaderAuth();

    boolean authTokenExpires();

    boolean authTokenCanBeRefreshed();
}
