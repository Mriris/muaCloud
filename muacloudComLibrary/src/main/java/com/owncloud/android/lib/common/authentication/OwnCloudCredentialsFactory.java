
package com.owncloud.android.lib.common.authentication;

public class OwnCloudCredentialsFactory {

    public static final String CREDENTIAL_CHARSET = "UTF-8";

    private static OwnCloudAnonymousCredentials sAnonymousCredentials;

    public static OwnCloudCredentials newBasicCredentials(String username, String password) {
        return new OwnCloudBasicCredentials(username, password);
    }

    public static OwnCloudCredentials newBearerCredentials(String username, String authToken) {
        return new OwnCloudBearerCredentials(username, authToken);
    }

    public static final OwnCloudCredentials getAnonymousCredentials() {
        if (sAnonymousCredentials == null) {
            sAnonymousCredentials = new OwnCloudAnonymousCredentials();
        }
        return sAnonymousCredentials;
    }

    public static final class OwnCloudAnonymousCredentials implements OwnCloudCredentials {

        private OwnCloudAnonymousCredentials() {
        }

        @Override
        public String getAuthToken() {
            return "";
        }

        @Override
        public String getHeaderAuth() {
            return "";
        }

        @Override
        public boolean authTokenExpires() {
            return false;
        }

        @Override
        public boolean authTokenCanBeRefreshed() {
            return false;
        }

        @Override
        public String getUsername() {

            return null;
        }
    }
}