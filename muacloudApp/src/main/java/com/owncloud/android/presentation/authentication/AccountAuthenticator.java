
package com.owncloud.android.presentation.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.owncloud.android.MainApp;
import com.owncloud.android.R;
import com.owncloud.android.presentation.authentication.oauth.OAuthUtils;
import com.owncloud.android.domain.UseCaseResult;
import com.owncloud.android.domain.authentication.oauth.OIDCDiscoveryUseCase;
import com.owncloud.android.domain.authentication.oauth.RequestTokenUseCase;
import com.owncloud.android.domain.authentication.oauth.model.OIDCServerConfiguration;
import com.owncloud.android.domain.authentication.oauth.model.TokenRequest;
import com.owncloud.android.domain.authentication.oauth.model.TokenResponse;
import com.owncloud.android.lib.common.accounts.AccountTypeUtils;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import kotlin.Lazy;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import java.io.File;

import static com.owncloud.android.data.authentication.AuthenticationConstantsKt.KEY_CLIENT_REGISTRATION_CLIENT_EXPIRATION_DATE;
import static com.owncloud.android.data.authentication.AuthenticationConstantsKt.KEY_CLIENT_REGISTRATION_CLIENT_ID;
import static com.owncloud.android.data.authentication.AuthenticationConstantsKt.KEY_CLIENT_REGISTRATION_CLIENT_SECRET;
import static com.owncloud.android.data.authentication.AuthenticationConstantsKt.KEY_OAUTH2_REFRESH_TOKEN;
import static com.owncloud.android.presentation.authentication.AuthenticatorConstants.KEY_AUTH_TOKEN_TYPE;
import static org.koin.java.KoinJavaComponent.inject;


public class AccountAuthenticator extends AbstractAccountAuthenticator {

    
    private static final String KEY_REQUIRED_FEATURES = "requiredFeatures";
    public static final String KEY_ACCOUNT = "account";

    private final Context mContext;

    AccountAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options) {
        Timber.i("Adding account with type " + accountType + " and auth token " + authTokenType);

        final Bundle bundle = new Bundle();

        AccountManager accountManager = AccountManager.get(mContext);
        Account[] accounts = accountManager.getAccountsByType(MainApp.Companion.getAccountType());

        if (mContext.getResources().getBoolean(R.bool.multiaccount_support) || accounts.length < 1) {
            try {
                validateAccountType(accountType);
            } catch (AuthenticatorException e) {
                Timber.e(e, "Failed to validate account type %s", accountType);
                return e.getFailureBundle();
            }

            final Intent intent = new Intent(mContext, LoginActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType);
            intent.putExtra(KEY_REQUIRED_FEATURES, requiredFeatures);
            intent.putExtra(AuthenticatorConstants.EXTRA_ACTION, AuthenticatorConstants.ACTION_CREATE);

            setIntentFlags(intent);

            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        }

        return bundle;
    }

    
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                     Account account, Bundle options) {
        try {
            validateAccountType(account.type);
        } catch (AuthenticatorException e) {
            Timber.e(e, "Failed to validate account type %s", account.type);
            return e.getFailureBundle();
        }
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        intent.putExtra(KEY_ACCOUNT, account);

        setIntentFlags(intent);

        Bundle resultBundle = new Bundle();
        resultBundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return resultBundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
                                 String accountType) {
        return null;
    }

    
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse,
                               Account account, String authTokenType, Bundle options) {

        try {
            validateAccountType(account.type);
            validateAuthTokenType(authTokenType);
        } catch (AuthenticatorException e) {
            Timber.e(e, "Failed to validate account type %s", account.type);
            return e.getFailureBundle();
        }

        String accessToken;

        final AccountManager accountManager = AccountManager.get(mContext);
        if (authTokenType.equals(AccountTypeUtils.getAuthTokenTypePass(MainApp.Companion.getAccountType()))) {

            accessToken = accountManager.getPassword(account);
        } else {


            accessToken = accountManager.peekAuthToken(account, authTokenType);
            if (accessToken == null && canBeRefreshed(authTokenType) && clientSecretIsValid(accountManager, account)) {
                accessToken = refreshToken(account, authTokenType, accountManager);
            }
        }

        if (accessToken != null) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, MainApp.Companion.getAccountType());
            result.putString(AccountManager.KEY_AUTHTOKEN, accessToken);
            return result;
        }

        return prepareBundleToAccessLoginActivity(accountAuthenticatorResponse, account, authTokenType, options);
    }

    
    private boolean clientSecretIsValid(AccountManager accountManager, Account account) {
        String clientSecretExpiration = accountManager.getUserData(account,
                KEY_CLIENT_REGISTRATION_CLIENT_EXPIRATION_DATE);

        Timber.d("Client secret expiration [" + clientSecretExpiration + "]");
        if (clientSecretExpiration == null) {
            return true;
        }

        long currentTimeStamp = System.currentTimeMillis() / 1000L;
        int clientSecretExpirationInt = Integer.parseInt(clientSecretExpiration);
        boolean clientSecretIsValid = clientSecretExpirationInt == 0 || clientSecretExpirationInt > currentTimeStamp;

        Timber.d("Current time [" + currentTimeStamp + "]");
        Timber.d("Client is valid [" + clientSecretIsValid + "]");

        return clientSecretIsValid;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
                              Account account, String[] features) {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
                                    Account account, String authTokenType, Bundle options) {
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType);
        setIntentFlags(intent);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAccountRemovalAllowed(
            AccountAuthenticatorResponse response, Account account)
            throws NetworkErrorException {
        return super.getAccountRemovalAllowed(response, account);
    }

    private void setIntentFlags(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
    }

    private void validateAccountType(String type)
            throws UnsupportedAccountTypeException {
        if (!type.equals(MainApp.Companion.getAccountType())) {
            throw new UnsupportedAccountTypeException();
        }
    }

    private void validateAuthTokenType(String authTokenType)
            throws UnsupportedAuthTokenTypeException {
        if (!authTokenType.equals(MainApp.Companion.getAuthTokenType()) &&
                !authTokenType.equals(AccountTypeUtils.getAuthTokenTypePass(MainApp.Companion.getAccountType())) &&
                !authTokenType.equals(AccountTypeUtils.getAuthTokenTypeAccessToken(MainApp.Companion.getAccountType())) &&
                !authTokenType.equals(AccountTypeUtils.getAuthTokenTypeRefreshToken(MainApp.Companion.getAccountType()))
        ) {
            throw new UnsupportedAuthTokenTypeException();
        }
    }

    public static class AuthenticatorException extends Exception {
        private static final long serialVersionUID = 1L;
        private final Bundle mFailureBundle;

        AuthenticatorException(int code, String errorMsg) {
            mFailureBundle = new Bundle();
            mFailureBundle.putInt(AccountManager.KEY_ERROR_CODE, code);
            mFailureBundle
                    .putString(AccountManager.KEY_ERROR_MESSAGE, errorMsg);
        }

        Bundle getFailureBundle() {
            return mFailureBundle;
        }
    }

    public static class UnsupportedAccountTypeException extends
            AuthenticatorException {
        private static final long serialVersionUID = 1L;

        UnsupportedAccountTypeException() {
            super(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                    "Unsupported account type");
        }
    }

    public static class UnsupportedAuthTokenTypeException extends
            AuthenticatorException {
        private static final long serialVersionUID = 1L;

        UnsupportedAuthTokenTypeException() {
            super(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                    "Unsupported auth token type");
        }
    }

    private boolean canBeRefreshed(String authTokenType) {
        return (authTokenType.equals(AccountTypeUtils.getAuthTokenTypeAccessToken(MainApp.Companion.
                getAccountType())));
    }

    private String refreshToken(
            Account account,
            String authTokenType,
            AccountManager accountManager
    ) {

        String refreshToken = accountManager.getUserData(account, KEY_OAUTH2_REFRESH_TOKEN);

        if (refreshToken == null || refreshToken.isEmpty()) {
            Timber.w("No refresh token stored for silent renewal of access token");
            return null;
        }

        Timber.d("Ready to exchange for new tokens. Account: [ %s ], Refresh token: [ %s ]", account.name,
                refreshToken);

        String baseUrl = accountManager.getUserData(account, AccountUtils.Constants.KEY_OC_BASE_URL);

        @NotNull Lazy<OIDCDiscoveryUseCase> oidcDiscoveryUseCase = inject(OIDCDiscoveryUseCase.class);
        OIDCDiscoveryUseCase.Params oidcDiscoveryUseCaseParams = new OIDCDiscoveryUseCase.Params(baseUrl);
        UseCaseResult<OIDCServerConfiguration> oidcServerConfigurationUseCaseResult =
                oidcDiscoveryUseCase.getValue().invoke(oidcDiscoveryUseCaseParams);

        String tokenEndpoint;

        String clientId = accountManager.getUserData(account, KEY_CLIENT_REGISTRATION_CLIENT_ID);
        String clientSecret = accountManager.getUserData(account, KEY_CLIENT_REGISTRATION_CLIENT_SECRET);

        if (clientId == null) {
            Timber.d("Client Id not stored. Let's use the hardcoded one");
            clientId = mContext.getString(R.string.oauth2_client_id);
        }
        if (clientSecret == null) {
            Timber.d("Client Secret not stored. Let's use the hardcoded one");
            clientSecret = mContext.getString(R.string.oauth2_client_secret);
        }

        if (oidcServerConfigurationUseCaseResult.isSuccess()) {
            Timber.d("OIDC Discovery success. Server discovery info: [ %s ]",
                    oidcServerConfigurationUseCaseResult.getDataOrNull());

            tokenEndpoint = oidcServerConfigurationUseCaseResult.getDataOrNull().getTokenEndpoint();

        } else {
            Timber.d("OIDC Discovery failed. Server discovery info: [ %s ]",
                    oidcServerConfigurationUseCaseResult.getThrowableOrNull().toString());

            tokenEndpoint = baseUrl + File.separator + mContext.getString(R.string.oauth2_url_endpoint_access);
        }

        String clientAuth = OAuthUtils.Companion.getClientAuth(clientSecret, clientId);

        String scope = mContext.getResources().getString(R.string.oauth2_openid_scope);

        TokenRequest oauthTokenRequest = new TokenRequest.RefreshToken(
                baseUrl,
                tokenEndpoint,
                clientAuth,
                scope,
                refreshToken
        );

        @NotNull Lazy<RequestTokenUseCase> requestTokenUseCase = inject(RequestTokenUseCase.class);
        RequestTokenUseCase.Params requestTokenParams = new RequestTokenUseCase.Params(oauthTokenRequest);
        UseCaseResult<TokenResponse> tokenResponseResult = requestTokenUseCase.getValue().invoke(requestTokenParams);

        TokenResponse safeTokenResponse = tokenResponseResult.getDataOrNull();
        if (safeTokenResponse != null) {
            return handleSuccessfulRefreshToken(safeTokenResponse,
                    account, authTokenType, accountManager, refreshToken);
        } else {
            Timber.e(tokenResponseResult.getThrowableOrNull(), "OAuth request to refresh access token failed. Preparing to access Login Activity");
            return null;
        }
    }

    private String handleSuccessfulRefreshToken(
            TokenResponse tokenResponse,
            Account account,
            String authTokenType,
            AccountManager accountManager,
            String oldRefreshToken
    ) {
            String newAccessToken = tokenResponse.getAccessToken();
            accountManager.setAuthToken(account, authTokenType, newAccessToken);

            String refreshTokenToUseFromNowOn;
            if (tokenResponse.getRefreshToken() != null) {
                refreshTokenToUseFromNowOn = tokenResponse.getRefreshToken();
            } else {
                refreshTokenToUseFromNowOn = oldRefreshToken;
            }
            accountManager.setUserData(account, KEY_OAUTH2_REFRESH_TOKEN, refreshTokenToUseFromNowOn);

            Timber.d("Token refreshed successfully. New access token: [ %s ]. New refresh token: [ %s ]",
                    newAccessToken, refreshTokenToUseFromNowOn);

            return newAccessToken;
        }

    
    private Bundle prepareBundleToAccessLoginActivity(
            AccountAuthenticatorResponse accountAuthenticatorResponse,
            Account account,
            String authTokenType,
            Bundle options
    ) {
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                accountAuthenticatorResponse);
        intent.putExtra(KEY_AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(AuthenticatorConstants.EXTRA_ACCOUNT, account);
        intent.putExtra(
                AuthenticatorConstants.EXTRA_ACTION,
                AuthenticatorConstants.ACTION_UPDATE_EXPIRED_TOKEN
        );

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }
}
