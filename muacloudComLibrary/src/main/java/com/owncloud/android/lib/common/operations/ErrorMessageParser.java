package com.owncloud.android.lib.common.operations;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;


public class ErrorMessageParser {

    private static final String ns = null;

    private static final String NODE_ERROR = "d:error";
    private static final String NODE_MESSAGE = "s:message";


    public String parseXMLResponse(InputStream is) throws XmlPullParserException,
            IOException {
        String errorMessage = "";

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.nextTag();
            errorMessage = readError(parser);

        } finally {
            is.close();
        }
        return errorMessage;
    }


    private String readError(XmlPullParser parser) throws XmlPullParserException, IOException {
        String errorMessage = "";
        parser.require(XmlPullParser.START_TAG, ns, NODE_ERROR);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equalsIgnoreCase(NODE_MESSAGE)) {
                errorMessage = readText(parser);
            } else {
                skip(parser);
            }
        }
        return errorMessage;
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