

package com.owncloud.android.presentation.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.owncloud.android.MainApp;
import com.owncloud.android.domain.capabilities.model.OCCapability;
import com.owncloud.android.lib.common.accounts.AccountUtils.Constants;
import timber.log.Timber;

import java.util.Locale;

import static com.owncloud.android.data.authentication.AuthenticationConstantsKt.KEY_FEATURE_ALLOWED;
import static com.owncloud.android.data.authentication.AuthenticationConstantsKt.KEY_FEATURE_SPACES;
import static com.owncloud.android.data.authentication.AuthenticationConstantsKt.SELECTED_ACCOUNT;
import static com.owncloud.android.lib.common.accounts.AccountUtils.Constants.OAUTH_SUPPORTED_TRUE;

public class AccountUtils {

    private static final int ACCOUNT_VERSION = 1;


    public static Account getCurrentOwnCloudAccount(Context context) {
        Account[] ocAccounts = getAccounts(context);
        Account defaultAccount = null;

        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String accountName = appPreferences.getString(SELECTED_ACCOUNT, null);

        if (accountName != null) {
            for (Account account : ocAccounts) {
                if (account.name.equals(accountName)) {
                    defaultAccount = account;
                    break;
                }
            }
        }

        if (defaultAccount == null && ocAccounts.length != 0) {

            defaultAccount = ocAccounts[0];
        }

        return defaultAccount;
    }

    public static Account[] getAccounts(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        return accountManager.getAccountsByType(MainApp.Companion.getAccountType());
    }

    public static void deleteAccounts(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = getAccounts(context);
        for (Account account : accounts) {
            accountManager.removeAccount(account, null, null, null);
        }
    }

    public static boolean exists(String accountName, Context context) {
        Account[] ocAccounts = getAccounts(context);

        if (accountName != null) {
            int lastAtPos = accountName.lastIndexOf("@");
            String hostAndPort = accountName.substring(lastAtPos + 1);
            String username = accountName.substring(0, lastAtPos);
            String otherHostAndPort, otherUsername;
            Locale currentLocale = context.getResources().getConfiguration().locale;
            for (Account otherAccount : ocAccounts) {
                lastAtPos = otherAccount.name.lastIndexOf("@");
                otherHostAndPort = otherAccount.name.substring(lastAtPos + 1);
                otherUsername = otherAccount.name.substring(0, lastAtPos);
                if (otherHostAndPort.equals(hostAndPort) &&
                        otherUsername.toLowerCase(currentLocale).
                                equals(username.toLowerCase(currentLocale))) {
                    return true;
                }
            }
        }
        return false;
    }


    public static String getUsernameOfAccount(String accountName) {
        if (accountName != null) {
            return accountName.substring(0, accountName.lastIndexOf("@"));
        } else {
            return null;
        }
    }


    public static Account getOwnCloudAccountByName(Context context, String accountName) {
        Account[] ocAccounts = AccountManager.get(context).getAccountsByType(
                MainApp.Companion.getAccountType());
        for (Account account : ocAccounts) {
            if (account.name.equals(accountName)) {
                return account;
            }
        }
        return null;
    }

    public static boolean isSpacesFeatureAllowedForAccount(Context context, Account account, OCCapability capability) {
        if (capability == null || !capability.isSpacesAllowed()) {
            return false;
        }
        AccountManager accountManager = AccountManager.get(context);
        String spacesFeatureValue = accountManager.getUserData(account, KEY_FEATURE_SPACES);
        return KEY_FEATURE_ALLOWED.equals(spacesFeatureValue);
    }

    public static boolean setCurrentOwnCloudAccount(Context context, String accountName) {
        boolean result = false;
        if (accountName != null) {
            boolean found;
            for (Account account : getAccounts(context)) {
                found = (account.name.equals(accountName));
                if (found) {
                    SharedPreferences.Editor appPrefs = PreferenceManager
                            .getDefaultSharedPreferences(context).edit();
                    appPrefs.putString(SELECTED_ACCOUNT, accountName);

                    appPrefs.apply();
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    public static void updateAccountVersion(Context context) {
        Account currentAccount = AccountUtils.getCurrentOwnCloudAccount(context);
        AccountManager accountMgr = AccountManager.get(context);

        if (currentAccount != null) {
            String currentAccountVersion = accountMgr.getUserData(currentAccount, Constants.KEY_OC_ACCOUNT_VERSION);

            if (currentAccountVersion == null) {
                Timber.i("Upgrading accounts to account version #%s", ACCOUNT_VERSION);
                Account[] ocAccounts = accountMgr.getAccountsByType(MainApp.Companion.getAccountType());
                String serverUrl, username, newAccountName, password;
                Account newAccount;
                for (Account account : ocAccounts) {

                    serverUrl = accountMgr.getUserData(account, Constants.KEY_OC_BASE_URL);
                    username = com.owncloud.android.lib.common.accounts.AccountUtils.
                            getUsernameForAccount(account);
                    newAccountName = com.owncloud.android.lib.common.accounts.AccountUtils.
                            buildAccountName(Uri.parse(serverUrl), username);

                    if (!newAccountName.equals(account.name)) {
                        Timber.d("Upgrading " + account.name + " to " + newAccountName);

                        newAccount = new Account(newAccountName, MainApp.Companion.getAccountType());
                        password = accountMgr.getPassword(account);
                        accountMgr.addAccountExplicitly(newAccount, (password != null) ? password : "", null);

                        accountMgr.setUserData(newAccount, Constants.KEY_OC_BASE_URL, serverUrl);

                        String isOauthStr = accountMgr.getUserData(account, Constants.KEY_SUPPORTS_OAUTH2);
                        boolean isOAuth = OAUTH_SUPPORTED_TRUE.equals(isOauthStr);
                        if (isOAuth) {
                            accountMgr.setUserData(newAccount, Constants.KEY_SUPPORTS_OAUTH2, OAUTH_SUPPORTED_TRUE);
                        }

                        if (currentAccount.name.equals(account.name)) {
                            AccountUtils.setCurrentOwnCloudAccount(context, newAccountName);
                        }

                        accountMgr.removeAccount(account, null, null);


                    } else {

                        Timber.d("%s needs no upgrade ", account.name);
                        newAccount = account;
                    }

                    Timber.d("Setting version " + ACCOUNT_VERSION + " to " + newAccountName);
                    accountMgr.setUserData(
                            newAccount, Constants.KEY_OC_ACCOUNT_VERSION, Integer.toString(ACCOUNT_VERSION)
                    );

                }
            }
        }
    }
}
