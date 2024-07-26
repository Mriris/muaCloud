
package com.owncloud.android.ui.errorhandling;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import timber.log.Timber;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    private final Activity mContext;
    private final String LINE_SEPARATOR = "\n";

    public ExceptionHandler(Activity context) {
        mContext = context;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        Timber.e(exception, "ExceptionHandler caught UncaughtException");
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        String errorReport = "************ CAUSE OF ERROR ************\n\n" +
                stackTrace +
                "\n************ DEVICE INFORMATION ***********\n" +
                "Brand: " +
                Build.BRAND +
                LINE_SEPARATOR +
                "Device: " +
                Build.DEVICE +
                LINE_SEPARATOR +
                "Model: " +
                Build.MODEL +
                LINE_SEPARATOR +
                "Id: " +
                Build.ID +
                LINE_SEPARATOR +
                "Product: " +
                Build.PRODUCT +
                LINE_SEPARATOR +
                "\n************ FIRMWARE ************\n" +
                "SDK: " +
                Build.VERSION.SDK_INT +
                LINE_SEPARATOR +
                "Release: " +
                Build.VERSION.RELEASE +
                LINE_SEPARATOR +
                "Incremental: " +
                Build.VERSION.INCREMENTAL +
                LINE_SEPARATOR;

        Timber.e(exception, "An exception was thrown and handled by ExceptionHandler:");

        Intent intent = new Intent(mContext, ErrorShowActivity.class);
        intent.putExtra("error", errorReport);
        mContext.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1000);
    }

}