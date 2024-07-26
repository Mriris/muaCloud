
package com.owncloud.android.lib.common.http;


public class HttpConstants {


    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String COOKIE_HEADER = "Cookie";
    public static final String BEARER_AUTHORIZATION_KEY = "Bearer ";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String IF_MATCH_HEADER = "If-Match";
    public static final String IF_NONE_MATCH_HEADER = "If-None-Match";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String OC_TOTAL_LENGTH_HEADER = "OC-Total-Length";
    public static final String OC_X_OC_MTIME_HEADER = "X-OC-Mtime";
    public static final String OC_X_REQUEST_ID = "X-Request-ID";
    public static final String LOCATION_HEADER = "Location";
    public static final String LOCATION_HEADER_LOWER = "location";
    public static final String CONTENT_TYPE_URLENCODED_UTF8 = "application/x-www-form-urlencoded; charset=utf-8";
    public static final String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    public static final String ACCEPT_ENCODING_IDENTITY = "identity";
    public static final String OC_FILE_REMOTE_ID = "OC-FileId";

    public static final String OAUTH_HEADER_AUTHORIZATION_CODE = "code";
    public static final String OAUTH_HEADER_GRANT_TYPE = "grant_type";
    public static final String OAUTH_HEADER_REDIRECT_URI = "redirect_uri";
    public static final String OAUTH_HEADER_REFRESH_TOKEN = "refresh_token";
    public static final String OAUTH_HEADER_CODE_VERIFIER = "code_verifier";
    public static final String OAUTH_HEADER_SCOPE = "scope";


    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_WWW_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JRD_JSON = "application/jrd+json";


    public static final String PARAM_FORMAT = "format";


    public static final String VALUE_FORMAT = "json";

    public static final int HTTP_CONTINUE = 100;

    public static final int HTTP_SWITCHING_PROTOCOLS = 101;

    public static final int HTTP_PROCESSING = 102;

    public static final int HTTP_OK = 200;

    public static final int HTTP_CREATED = 201;

    public static final int HTTP_ACCEPTED = 202;

    public static final int HTTP_NON_AUTHORITATIVE_INFORMATION = 203;

    public static final int HTTP_NO_CONTENT = 204;

    public static final int HTTP_RESET_CONTENT = 205;

    public static final int HTTP_PARTIAL_CONTENT = 206;

    public static final int HTTP_MULTI_STATUS = 207;

    public static final int HTTP_MULTIPLE_CHOICES = 300;

    public static final int HTTP_MOVED_PERMANENTLY = 301;

    public static final int HTTP_MOVED_TEMPORARILY = 302;

    public static final int HTTP_SEE_OTHER = 303;

    public static final int HTTP_NOT_MODIFIED = 304;

    public static final int HTTP_USE_PROXY = 305;

    public static final int HTTP_TEMPORARY_REDIRECT = 307;

    public static final int HTTP_BAD_REQUEST = 400;

    public static final int HTTP_UNAUTHORIZED = 401;

    public static final int HTTP_PAYMENT_REQUIRED = 402;

    public static final int HTTP_FORBIDDEN = 403;

    public static final int HTTP_NOT_FOUND = 404;

    public static final int HTTP_METHOD_NOT_ALLOWED = 405;

    public static final int HTTP_NOT_ACCEPTABLE = 406;

    public static final int HTTP_PROXY_AUTHENTICATION_REQUIRED = 407;

    public static final int HTTP_REQUEST_TIMEOUT = 408;

    public static final int HTTP_CONFLICT = 409;

    public static final int HTTP_GONE = 410;

    public static final int HTTP_LENGTH_REQUIRED = 411;

    public static final int HTTP_PRECONDITION_FAILED = 412;

    public static final int HTTP_REQUEST_TOO_LONG = 413;

    public static final int HTTP_REQUEST_URI_TOO_LONG = 414;

    public static final int HTTP_UNSUPPORTED_MEDIA_TYPE = 415;

    public static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    public static final int HTTP_EXPECTATION_FAILED = 417;


    public static final int HTTP_INSUFFICIENT_SPACE_ON_RESOURCE = 419;

    public static final int HTTP_METHOD_FAILURE = 420;

    public static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    public static final int HTTP_LOCKED = 423;

    public static final int HTTP_FAILED_DEPENDENCY = 424;
    public static final int HTTP_TOO_EARLY = 425;

    public static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    public static final int HTTP_NOT_IMPLEMENTED = 501;

    public static final int HTTP_BAD_GATEWAY = 502;

    public static final int HTTP_SERVICE_UNAVAILABLE = 503;

    public static final int HTTP_GATEWAY_TIMEOUT = 504;

    public static final int HTTP_HTTP_VERSION_NOT_SUPPORTED = 505;

    public static final int HTTP_INSUFFICIENT_STORAGE = 507;



    public static final int DEFAULT_DATA_TIMEOUT = 60000;


    public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
}
