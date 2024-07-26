package com.owncloud.android.lib.common.http.methods.webdav.properties

import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.PropertyFactory
import at.bitfire.dav4jvm.XmlUtils
import org.xmlpull.v1.XmlPullParser
import java.util.LinkedList

abstract class ShareTypeListProperty : Property {

    val shareTypes = LinkedList<String>()

    override fun toString() = "share types =[" + shareTypes.joinToString(", ") + "]"

    abstract class Factory : PropertyFactory {

        fun create(parser: XmlPullParser, list: ShareTypeListProperty): ShareTypeListProperty {
            XmlUtils.readTextPropertyList(parser, Property.Name(XmlUtils.NS_OWNCLOUD, "share-type"), list.shareTypes)
            return list
        }

    }
}
