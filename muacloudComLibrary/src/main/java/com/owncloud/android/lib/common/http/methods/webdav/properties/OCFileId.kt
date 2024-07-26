
package com.owncloud.android.lib.common.http.methods.webdav.properties

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import org.xmlpull.v1.XmlPullParser

data class OCFileId(val fileId: String) : Property {
    class Factory : PropertyFactory {
        override fun getName() = NAME

        override fun create(parser: XmlPullParser): OCFileId? {
            XmlUtils.readText(parser)?.let {
                return OCFileId(it)
            }
            return null
        }
    }

    companion object {
        @JvmField
        val NAME = Property.Name(XmlUtils.NS_OWNCLOUD, "fileid")
    }
}
