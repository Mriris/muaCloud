package com.owncloud.android.lib.common.http.methods.webdav.properties

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.XmlUtils
import org.xmlpull.v1.XmlPullParser

class OCShareTypes : ShareTypeListProperty() {

    class Factory : ShareTypeListProperty.Factory() {

        override fun create(parser: XmlPullParser) =
            create(parser, OCShareTypes())

        override fun getName(): Property.Name = NAME
    }

    companion object {
        @JvmField
        val NAME = Property.Name(XmlUtils.NS_OWNCLOUD, "share-types")
    }
}
