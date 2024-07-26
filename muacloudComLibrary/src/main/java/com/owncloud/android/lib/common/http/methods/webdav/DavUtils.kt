package com.owncloud.android.lib.common.http.methods.webdav

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyUtils.getQuotaPropset
import at.bitfire.dav4jvm.property.CreationDate
import at.bitfire.dav4jvm.property.DisplayName
import at.bitfire.dav4jvm.property.GetContentLength
import at.bitfire.dav4jvm.property.GetContentType
import at.bitfire.dav4jvm.property.GetETag
import at.bitfire.dav4jvm.property.GetLastModified
import at.bitfire.dav4jvm.property.OCId
import at.bitfire.dav4jvm.property.OCPermissions
import at.bitfire.dav4jvm.property.OCPrivatelink
import at.bitfire.dav4jvm.property.OCSize
import at.bitfire.dav4jvm.property.ResourceType
import com.owncloud.android.lib.common.http.methods.webdav.properties.OCShareTypes

object DavUtils {
    @JvmStatic val allPropSet: Array<Property.Name>
        get() = arrayOf(
            DisplayName.NAME,
            GetContentType.NAME,
            ResourceType.NAME,
            GetContentLength.NAME,
            GetLastModified.NAME,
            CreationDate.NAME,
            GetETag.NAME,
            OCPermissions.NAME,
            OCId.NAME,
            OCSize.NAME,
            OCPrivatelink.NAME,
            OCShareTypes.NAME,
        )

    val quotaPropSet: Array<Property.Name>
        get() = getQuotaPropset()
}
