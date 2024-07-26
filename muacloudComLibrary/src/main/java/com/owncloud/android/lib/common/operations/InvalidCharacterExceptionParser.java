package com.owncloud.android.lib.common.operations;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;


public class InvalidCharacterExceptionParser {

    private static final String EXCEPTION_STRING = "OC\\Connector\\Sabre\\Exception\\InvalidPath";
    private static final String EXCEPTION_UPLOAD_STRING = "OCP\\Files\\InvalidPathException";

    private static final String ns = null;

    private static final String NODE_ERROR = "d:error";
    private static final String NODE_EXCEPTION = "s:exception";


    public boolean parseXMLResponse(InputStream is) throws XmlPullParserException,
            IOException {
        boolean result = false;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.nextTag();
            result = readError(parser);

        } finally {
            is.close();
        }
        return result;
    }


    private boolean readError(XmlPullParser parser) throws XmlPullParserException, IOException {
        String exception = "";
        parser.require(XmlPullParser.START_TAG, ns, NODE_ERROR);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equalsIgnoreCase(NODE_EXCEPTION)) {
                exception = readText(parser);
            } else {
                skip(parser);
            }

        }
        return exception.equalsIgnoreCase(EXCEPTION_STRING) ||
                exception.equalsIgnoreCase(EXCEPTION_UPLOAD_STRING);
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
