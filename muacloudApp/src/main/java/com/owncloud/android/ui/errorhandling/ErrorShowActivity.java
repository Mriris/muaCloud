
package com.owncloud.android.ui.errorhandling;

import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import com.owncloud.android.R;
import com.owncloud.android.ui.activity.BaseActivity;
import com.owncloud.android.utils.PreferenceUtils;
import timber.log.Timber;

public class ErrorShowActivity extends BaseActivity {

    TextView mError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.e("ErrorShowActivity was called. See above for StackTrace.");
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.errorhandling_showerror);

        ScrollView errorHandlingShowErrorScrollView = findViewById(R.id.errorHandlingShowErrorScrollView);
        errorHandlingShowErrorScrollView.setFilterTouchesWhenObscured(
                PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(this)
        );

        mError = findViewById(R.id.errorTextView);
        mError.setText(getIntent().getStringExtra("error"));
    }
}