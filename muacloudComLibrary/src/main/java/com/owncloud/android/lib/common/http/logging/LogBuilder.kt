package com.owncloud.android.lib.common.http.logging

import com.owncloud.android.lib.common.http.HttpConstants.CONTENT_TYPE_JRD_JSON
import com.owncloud.android.lib.common.http.HttpConstants.CONTENT_TYPE_JSON
import com.owncloud.android.lib.common.http.HttpConstants.CONTENT_TYPE_WWW_FORM
import com.owncloud.android.lib.common.http.HttpConstants.CONTENT_TYPE_XML
import okhttp3.MediaType


fun MediaType?.isLoggable(): Boolean =
    this?.let { mediaType ->
        val mediaTypeString = mediaType.toString()
        (mediaType.type == "text" ||
                mediaTypeString.contains(CONTENT_TYPE_XML) ||
                mediaTypeString.contains(CONTENT_TYPE_JSON) ||
                mediaTypeString.contains(CONTENT_TYPE_WWW_FORM) ||
                mediaTypeString.contains(CONTENT_TYPE_JRD_JSON))
    } ?: false
