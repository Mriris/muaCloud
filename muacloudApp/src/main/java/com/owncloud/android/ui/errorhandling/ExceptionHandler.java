
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
        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());

        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK_INT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);

        Timber.e(exception, "An exception was thrown and handled by ExceptionHandler:");

        Intent intent = new Intent(mContext, ErrorShowActivity.class);
        intent.putExtra("error", errorReport.toString());
        mContext.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1000);
    }

}