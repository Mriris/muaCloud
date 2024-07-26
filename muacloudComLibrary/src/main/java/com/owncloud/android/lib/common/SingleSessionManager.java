
package com.owncloud.android.lib.common;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;

import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentials;
import timber.log.Timber;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;



public class SingleSessionManager {

    private static SingleSessionManager sDefaultSingleton;
    private static String sUserAgent;
    private static ConnectionValidator sConnectionValidator;

    private final ConcurrentMap<String, OwnCloudClient> mClientsWithKnownUsername = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, OwnCloudClient> mClientsWithUnknownUsername = new ConcurrentHashMap<>();

    public static SingleSessionManager getDefaultSingleton() {
        if (sDefaultSingleton == null) {
            sDefaultSingleton = new SingleSessionManager();
        }
        return sDefaultSingleton;
    }

    public static void setConnectionValidator(ConnectionValidator connectionValidator) {
        sConnectionValidator = connectionValidator;
    }

    public static ConnectionValidator getConnectionValidator() {
        return sConnectionValidator;
    }

    public static String getUserAgent() {
        return sUserAgent;
    }

    public static void setUserAgent(String userAgent) {
        sUserAgent = userAgent;
    }

    private static OwnCloudClient createOwnCloudClient(Uri uri,
                                                       Context context,
                                                       ConnectionValidator connectionValidator,
                                                       SingleSessionManager singleSessionManager) {
        OwnCloudClient client = new OwnCloudClient(uri, connectionValidator, true, singleSessionManager, context);
        return client;
    }

    public OwnCloudClient getClientFor(OwnCloudAccount account,
                                       Context context) throws OperationCanceledException,
            AuthenticatorException, IOException {
        return getClientFor(account, context, getConnectionValidator());
    }

    public OwnCloudClient getClientFor(OwnCloudAccount account,
                                       Context context,
                                       ConnectionValidator connectionValidator) throws OperationCanceledException,
            AuthenticatorException, IOException {

        Timber.d("getClientFor starting ");
        if (account == null) {
            throw new IllegalArgumentException("Cannot get an OwnCloudClient for a null account");
        }

        OwnCloudClient client = null;
        String accountName = account.getName();
        String sessionName = account.getCredentials() == null ? "" :
                AccountUtils.buildAccountName(account.getBaseUri(), account.getCredentials().getAuthToken());

        if (accountName != null) {
            client = mClientsWithKnownUsername.get(accountName);
        }
        boolean reusingKnown = false;    // just for logs
        if (client == null) {
            if (accountName != null) {
                client = mClientsWithUnknownUsername.remove(sessionName);
                if (client != null) {
                    Timber.v("reusing client for session %s", sessionName);

                    mClientsWithKnownUsername.put(accountName, client);
                    Timber.v("moved client to account %s", accountName);
                }
            } else {
                client = mClientsWithUnknownUsername.get(sessionName);
            }
        } else {
            Timber.v("reusing client for account %s", accountName);
            if (client.getAccount() != null &&
                    client.getAccount().getCredentials() != null &&
                    (client.getAccount().getCredentials().getAuthToken() == null || client.getAccount().getCredentials().getAuthToken().isEmpty())
            ) {
                Timber.i("Client " + client.getAccount().getName() + " needs to refresh credentials");


                client.clearCookies();
                client.clearCredentials();

                client.setAccount(account);

                account.loadCredentials(context);
                client.setCredentials(account.getCredentials());

                Timber.i("Client " + account.getName() + " with credentials size" + client.getAccount().getCredentials().getAuthToken().length());
            }
            reusingKnown = true;
        }

        if (client == null) {

            client = createOwnCloudClient(
                    account.getBaseUri(),
                    context,
                    connectionValidator,
                    this); // TODO remove dependency on OwnCloudClientFactory


            client.clearCookies();
            client.clearCredentials();

            client.setAccount(account);

            account.loadCredentials(context);
            client.setCredentials(account.getCredentials());

            if (accountName != null) {
                mClientsWithKnownUsername.put(accountName, client);
                Timber.v("new client for account %s", accountName);

            } else {
                mClientsWithUnknownUsername.put(sessionName, client);
                Timber.v("new client for session %s", sessionName);
            }
        } else {
            if (!reusingKnown) {
                Timber.v("reusing client for session %s", sessionName);
            }

            keepUriUpdated(account, client);
        }
        Timber.d("getClientFor finishing ");
        return client;
    }

    public void removeClientFor(OwnCloudAccount account) {
        Timber.d("removeClientFor starting ");

        if (account == null) {
            return;
        }

        OwnCloudClient client;
        String accountName = account.getName();
        if (accountName != null) {
            client = mClientsWithKnownUsername.remove(accountName);
            if (client != null) {
                Timber.v("Removed client for account %s", accountName);
                return;
            } else {
                Timber.v("No client tracked for  account %s", accountName);
            }
        }

        mClientsWithUnknownUsername.clear();

        Timber.d("removeClientFor finishing ");
    }

    public void refreshCredentialsForAccount(String accountName, OwnCloudCredentials credentials) {
        OwnCloudClient ownCloudClient = mClientsWithKnownUsername.get(accountName);
        if (ownCloudClient == null) {
            return;
        }
        ownCloudClient.setCredentials(credentials);
        mClientsWithKnownUsername.replace(accountName, ownCloudClient);
    }


    private void keepUriUpdated(OwnCloudAccount account, OwnCloudClient reusedClient) {
        Uri recentUri = account.getBaseUri();
        if (!recentUri.equals(reusedClient.getBaseUri())) {
            reusedClient.setBaseUri(recentUri);
        }
    }
}
