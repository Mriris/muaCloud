
package com.owncloud.android.lib.common.operations;

import android.accounts.Account;
import android.accounts.AccountsException;

import at.bitfire.dav4jvm.exception.DavException;
import at.bitfire.dav4jvm.exception.HttpException;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.http.HttpConstants;
import com.owncloud.android.lib.common.http.methods.HttpBaseMethod;
import com.owncloud.android.lib.common.network.CertificateCombinedException;
import okhttp3.Headers;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import timber.log.Timber;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoteOperationResult<T>
        implements Serializable {


    private static final long serialVersionUID = 4968939884332372230L;
    private static final String LOCATION = "location";
    private static final String WWW_AUTHENTICATE = "www-authenticate";

    private boolean mSuccess = false;
    private int mHttpCode = -1;
    private String mHttpPhrase = null;
    private Exception mException = null;
    private ResultCode mCode = ResultCode.UNKNOWN_ERROR;
    private String mRedirectedLocation = "";
    private List<String> mAuthenticate = new ArrayList<>();
    private String mLastPermanentLocation = null;
    private T mData = null;


    public RemoteOperationResult(ResultCode code) {
        mCode = code;
        mSuccess = (code == ResultCode.OK || code == ResultCode.OK_SSL ||
                code == ResultCode.OK_NO_SSL ||
                code == ResultCode.OK_REDIRECT_TO_NON_SECURE_CONNECTION);
    }


    public RemoteOperationResult(RemoteOperationResult prevRemoteOperation) {
        mCode = prevRemoteOperation.mCode;
        mHttpCode = prevRemoteOperation.mHttpCode;
        mHttpPhrase = prevRemoteOperation.mHttpPhrase;
        mAuthenticate = prevRemoteOperation.mAuthenticate;
        mException = prevRemoteOperation.mException;
        mLastPermanentLocation = prevRemoteOperation.mLastPermanentLocation;
        mSuccess = prevRemoteOperation.mSuccess;
        mRedirectedLocation = prevRemoteOperation.mRedirectedLocation;
    }


    public RemoteOperationResult(Exception e) {
        mException = e;

        Timber.e("---------------------------------" +
                        "\nCreate RemoteOperationResult from exception." +
                        "\n Message: %s" +
                        "\n Stacktrace: %s" +
                        "\n---------------------------------",
                ExceptionUtils.getMessage(e),
                ExceptionUtils.getStackTrace(e));

        if (e instanceof OperationCancelledException) {
            mCode = ResultCode.CANCELLED;

        } else if (e instanceof SocketException) {
            mCode = ResultCode.WRONG_CONNECTION;

        } else if (e instanceof SocketTimeoutException) {
            mCode = ResultCode.TIMEOUT;

        } else if (e instanceof MalformedURLException) {
            mCode = ResultCode.INCORRECT_ADDRESS;

        } else if (e instanceof UnknownHostException) {
            mCode = ResultCode.HOST_NOT_AVAILABLE;

        } else if (e instanceof AccountUtils.AccountNotFoundException) {
            mCode = ResultCode.ACCOUNT_NOT_FOUND;

        } else if (e instanceof AccountsException) {
            mCode = ResultCode.ACCOUNT_EXCEPTION;

        } else if (e instanceof SSLException || e instanceof RuntimeException) {
            if (e instanceof SSLPeerUnverifiedException) {
                mCode = ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED;
            } else {
                CertificateCombinedException se = getCertificateCombinedException(e);
                if (se != null) {
                    mException = se;
                    if (se.isRecoverable()) {
                        mCode = ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED;
                    }
                } else if (e instanceof RuntimeException) {
                    mCode = ResultCode.HOST_NOT_AVAILABLE;

                } else {
                    mCode = ResultCode.SSL_ERROR;
                }
            }

        } else if (e instanceof FileNotFoundException) {
            mCode = ResultCode.LOCAL_FILE_NOT_FOUND;

        } else if (e instanceof ProtocolException) {
            mCode = ResultCode.NETWORK_ERROR;
        } else {
            mCode = ResultCode.UNKNOWN_ERROR;
        }
    }


    public RemoteOperationResult(HttpBaseMethod httpMethod) throws IOException {
        this(httpMethod.getStatusCode(),
                httpMethod.getStatusMessage(),
                httpMethod.getResponseHeaders()
        );

        if (mHttpCode == HttpConstants.HTTP_BAD_REQUEST) {   // 400
            String bodyResponse = httpMethod.getResponseBodyAsString();

            if (bodyResponse.length() > 0) {
                InputStream is = new ByteArrayInputStream(bodyResponse.getBytes());
                InvalidCharacterExceptionParser xmlParser = new InvalidCharacterExceptionParser();
                try {
                    if (xmlParser.parseXMLResponse(is)) {
                        mCode = ResultCode.INVALID_CHARACTER_DETECT_IN_SERVER;
                    } else {
                        parseErrorMessageAndSetCode(
                                httpMethod.getResponseBodyAsString(),
                                ResultCode.SPECIFIC_BAD_REQUEST
                        );
                    }
                } catch (Exception e) {
                    Timber.w("Error reading exception from server: %s", e.getMessage());

                }
            }
        }

        switch (mHttpCode) {
            case HttpConstants.HTTP_FORBIDDEN:
                parseErrorMessageAndSetCode(
                        httpMethod.getResponseBodyAsString(),
                        ResultCode.SPECIFIC_FORBIDDEN
                );
                break;
            case HttpConstants.HTTP_UNSUPPORTED_MEDIA_TYPE:
                parseErrorMessageAndSetCode(
                        httpMethod.getResponseBodyAsString(),
                        ResultCode.SPECIFIC_UNSUPPORTED_MEDIA_TYPE
                );
                break;
            case HttpConstants.HTTP_SERVICE_UNAVAILABLE:
                parseErrorMessageAndSetCode(
                        httpMethod.getResponseBodyAsString(),
                        ResultCode.SPECIFIC_SERVICE_UNAVAILABLE
                );
                break;
            case HttpConstants.HTTP_METHOD_NOT_ALLOWED:
                parseErrorMessageAndSetCode(
                        httpMethod.getResponseBodyAsString(),
                        ResultCode.SPECIFIC_METHOD_NOT_ALLOWED
                );
                break;
            case HttpConstants.HTTP_TOO_EARLY:
                mCode = ResultCode.TOO_EARLY;
                break;
            default:
                break;
        }
    }


    public RemoteOperationResult(int httpCode, String httpPhrase, Headers headers) {
        this(httpCode, httpPhrase);
        if (headers != null) {
            for (Map.Entry<String, List<String>> header : headers.toMultimap().entrySet()) {
                if (LOCATION.equalsIgnoreCase(header.getKey())) {
                    mRedirectedLocation = header.getValue().get(0);
                    continue;
                }
                if (WWW_AUTHENTICATE.equalsIgnoreCase(header.getKey())) {
                    for (String value : header.getValue()) {
                        mAuthenticate.add(value.toLowerCase());
                    }
                }
            }
        }
    }


    private RemoteOperationResult(int httpCode, String httpPhrase) {
        mHttpCode = httpCode;
        mHttpPhrase = httpPhrase;

        if (httpCode > 0) {
            switch (httpCode) {
                case HttpConstants.HTTP_UNAUTHORIZED:                    // 401
                    mCode = ResultCode.UNAUTHORIZED;
                    break;
                case HttpConstants.HTTP_FORBIDDEN:                       // 403
                    mCode = ResultCode.FORBIDDEN;
                    break;
                case HttpConstants.HTTP_NOT_FOUND:                       // 404
                    mCode = ResultCode.FILE_NOT_FOUND;
                    break;
                case HttpConstants.HTTP_CONFLICT:                        // 409
                    mCode = ResultCode.CONFLICT;
                    break;
                case HttpConstants.HTTP_LOCKED:                          // 423
                    mCode = ResultCode.RESOURCE_LOCKED;
                    break;
                case HttpConstants.HTTP_INTERNAL_SERVER_ERROR:           // 500
                    mCode = ResultCode.INSTANCE_NOT_CONFIGURED;     // assuming too much...
                    break;
                case HttpConstants.HTTP_SERVICE_UNAVAILABLE:             // 503
                    mCode = ResultCode.SERVICE_UNAVAILABLE;
                    break;
                case HttpConstants.HTTP_INSUFFICIENT_STORAGE:            // 507
                    mCode = ResultCode.QUOTA_EXCEEDED;              // surprise!
                    break;
                default:
                    mCode = ResultCode.UNHANDLED_HTTP_CODE;         // UNKNOWN ERROR
                    Timber.d("RemoteOperationResult has processed UNHANDLED_HTTP_CODE: " + mHttpCode + " " + mHttpPhrase);
            }
        }
    }


    private void parseErrorMessageAndSetCode(String bodyResponse, ResultCode resultCode) {
        if (bodyResponse != null && bodyResponse.length() > 0) {
            InputStream is = new ByteArrayInputStream(bodyResponse.getBytes());
            ErrorMessageParser xmlParser = new ErrorMessageParser();
            try {
                String errorMessage = xmlParser.parseXMLResponse(is);
                if (!errorMessage.equals("")) {
                    mCode = resultCode;
                    mHttpPhrase = errorMessage;
                }
            } catch (Exception e) {
                Timber.w("Error reading exception from server: %s\nTrace: %s", e.getMessage(), ExceptionUtils.getStackTrace(e));

            }
        }
    }

    public boolean isSuccess() {
        return mSuccess;
    }

    public void setSuccess(boolean success) {
        this.mSuccess = success;
    }

    public boolean isCancelled() {
        return mCode == ResultCode.CANCELLED;
    }

    public int getHttpCode() {
        return mHttpCode;
    }

    public String getHttpPhrase() {
        return mHttpPhrase;
    }

    public ResultCode getCode() {
        return mCode;
    }

    public Exception getException() {
        return mException;
    }

    public boolean isSslRecoverableException() {
        return mCode == ResultCode.SSL_RECOVERABLE_PEER_UNVERIFIED;
    }

    public boolean isRedirectToNonSecureConnection() {
        return mCode == ResultCode.OK_REDIRECT_TO_NON_SECURE_CONNECTION;
    }

    private CertificateCombinedException getCertificateCombinedException(Exception e) {
        CertificateCombinedException result = null;
        if (e instanceof CertificateCombinedException) {
            return (CertificateCombinedException) e;
        }
        Throwable cause = mException.getCause();
        Throwable previousCause = null;
        while (cause != null && cause != previousCause &&
                !(cause instanceof CertificateCombinedException)) {
            previousCause = cause;
            cause = cause.getCause();
        }
        if (cause instanceof CertificateCombinedException) {
            result = (CertificateCombinedException) cause;
        }
        return result;
    }

    public String getLogMessage() {

        if (mException != null) {
            if (mException instanceof OperationCancelledException) {
                return "Operation cancelled by the caller";

            } else if (mException instanceof SocketException) {
                return "Socket exception";

            } else if (mException instanceof SocketTimeoutException) {
                return "Socket timeout exception";

            } else if (mException instanceof MalformedURLException) {
                return "Malformed URL exception";

            } else if (mException instanceof UnknownHostException) {
                return "Unknown host exception";

            } else if (mException instanceof CertificateCombinedException) {
                if (((CertificateCombinedException) mException).isRecoverable()) {
                    return "SSL recoverable exception";
                } else {
                    return "SSL exception";
                }

            } else if (mException instanceof SSLException) {
                return "SSL exception";

            } else if (mException instanceof DavException) {
                return "Unexpected WebDAV exception";

            } else if (mException instanceof HttpException) {
                return "HTTP violation";

            } else if (mException instanceof IOException) {
                return "Unrecovered transport exception";

            } else if (mException instanceof AccountUtils.AccountNotFoundException) {
                Account failedAccount =
                        ((AccountUtils.AccountNotFoundException) mException).getFailedAccount();
                return mException.getMessage() + " (" +
                        (failedAccount != null ? failedAccount.name : "NULL") + ")";

            } else if (mException instanceof AccountsException) {
                return "Exception while using account";

            } else if (mException instanceof JSONException) {
                return "JSON exception";

            } else {
                return "Unexpected exception";
            }
        }

        if (mCode == ResultCode.INSTANCE_NOT_CONFIGURED) {
            return "The ownCloud server is not configured!";

        } else if (mCode == ResultCode.NO_NETWORK_CONNECTION) {
            return "No network connection";

        } else if (mCode == ResultCode.BAD_OC_VERSION) {
            return "No valid ownCloud version was found at the server";

        } else if (mCode == ResultCode.LOCAL_STORAGE_FULL) {
            return "Local storage full";

        } else if (mCode == ResultCode.LOCAL_STORAGE_NOT_MOVED) {
            return "Error while moving file to final directory";

        } else if (mCode == ResultCode.ACCOUNT_NOT_NEW) {
            return "Account already existing when creating a new one";

        } else if (mCode == ResultCode.ACCOUNT_NOT_THE_SAME) {
            return "Authenticated with a different account than the one updating";

        } else if (mCode == ResultCode.INVALID_CHARACTER_IN_NAME) {
            return "The file name contains an forbidden character";

        } else if (mCode == ResultCode.FILE_NOT_FOUND) {
            return "Local file does not exist";

        } else if (mCode == ResultCode.SYNC_CONFLICT) {
            return "Synchronization conflict";
        }

        return "Operation finished with HTTP status code " + mHttpCode + " (" +
                (isSuccess() ? "success" : "fail") + ")";

    }

    public boolean isServerFail() {
        return (mHttpCode >= HttpConstants.HTTP_INTERNAL_SERVER_ERROR);
    }

    public boolean isException() {
        return (mException != null);
    }

    public boolean isTemporalRedirection() {
        return (mHttpCode == 302 || mHttpCode == 307);
    }

    public String getRedirectedLocation() {
        return mRedirectedLocation;
    }


    public boolean isNonSecureRedirection() {
        return (mRedirectedLocation != null && !(mRedirectedLocation.toLowerCase().startsWith("https://")));
    }

    public List<String> getAuthenticateHeaders() {
        return mAuthenticate;
    }

    public String getLastPermanentLocation() {
        return mLastPermanentLocation;
    }

    public void setLastPermanentLocation(String lastPermanentLocation) {
        mLastPermanentLocation = lastPermanentLocation;
    }

    public T getData() {
        return mData;
    }

    public void setData(T data) {
        mData = data;
    }

    public void setHttpPhrase(String httpPhrase) {
        mHttpPhrase = httpPhrase;
    }

    public enum ResultCode {
        OK,
        OK_SSL,
        OK_NO_SSL,
        UNHANDLED_HTTP_CODE,
        UNAUTHORIZED,
        FILE_NOT_FOUND,
        INSTANCE_NOT_CONFIGURED,
        UNKNOWN_ERROR,
        WRONG_CONNECTION,
        TIMEOUT,
        INCORRECT_ADDRESS,
        HOST_NOT_AVAILABLE,
        NO_NETWORK_CONNECTION,
        SSL_ERROR,
        SSL_RECOVERABLE_PEER_UNVERIFIED,
        BAD_OC_VERSION,
        CANCELLED,
        INVALID_LOCAL_FILE_NAME,
        INVALID_OVERWRITE,
        CONFLICT,
        OAUTH2_ERROR,
        SYNC_CONFLICT,
        LOCAL_STORAGE_FULL,
        LOCAL_STORAGE_NOT_MOVED,
        LOCAL_STORAGE_NOT_COPIED,
        OAUTH2_ERROR_ACCESS_DENIED,
        QUOTA_EXCEEDED,
        ACCOUNT_NOT_FOUND,
        ACCOUNT_EXCEPTION,
        ACCOUNT_NOT_NEW,
        ACCOUNT_NOT_THE_SAME,
        INVALID_CHARACTER_IN_NAME,
        SHARE_NOT_FOUND,
        LOCAL_STORAGE_NOT_REMOVED,
        FORBIDDEN,
        SHARE_FORBIDDEN,
        SPECIFIC_FORBIDDEN,
        OK_REDIRECT_TO_NON_SECURE_CONNECTION,
        INVALID_MOVE_INTO_DESCENDANT,
        INVALID_COPY_INTO_DESCENDANT,
        PARTIAL_MOVE_DONE,
        PARTIAL_COPY_DONE,
        SHARE_WRONG_PARAMETER,
        WRONG_SERVER_RESPONSE,
        INVALID_CHARACTER_DETECT_IN_SERVER,
        DELAYED_FOR_WIFI,
        LOCAL_FILE_NOT_FOUND,
        SERVICE_UNAVAILABLE,
        SPECIFIC_SERVICE_UNAVAILABLE,
        SPECIFIC_UNSUPPORTED_MEDIA_TYPE,
        SPECIFIC_METHOD_NOT_ALLOWED,
        SPECIFIC_BAD_REQUEST,
        TOO_EARLY,
        NETWORK_ERROR,
        RESOURCE_LOCKED
    }
}
