

package com.owncloud.android.ui.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class SsoWebView extends WebView {

    public SsoWebView(Context context) {
        super(context);
    }

    public SsoWebView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return false;
    }

}

