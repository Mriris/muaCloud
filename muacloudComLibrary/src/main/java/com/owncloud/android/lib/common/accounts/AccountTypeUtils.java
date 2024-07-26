
package com.owncloud.android.lib.common.accounts;


public class AccountTypeUtils {

    public static String getAuthTokenTypePass(String accountType) {
        return accountType + ".password";
    }

    public static String getAuthTokenTypeAccessToken(String accountType) {
        return accountType + ".oauth2.access_token";
    }

    public static String getAuthTokenTypeRefreshToken(String accountType) {
        return accountType + ".oauth2.refresh_token";
    }
}
