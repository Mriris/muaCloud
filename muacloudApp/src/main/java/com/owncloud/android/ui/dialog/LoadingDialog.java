
package com.owncloud.android.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.owncloud.android.R;
import com.owncloud.android.extensions.DialogExtKt;
import com.owncloud.android.utils.PreferenceUtils;

public class LoadingDialog extends DialogFragment {

    private static final String ARG_MESSAGE_ID = LoadingDialog.class.getCanonicalName() + ".ARG_MESSAGE_ID";
    private static final String ARG_CANCELABLE = LoadingDialog.class.getCanonicalName() + ".ARG_CANCELABLE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setCancelable(false);
    }


    public static LoadingDialog newInstance(int messageId, boolean cancelable) {
        LoadingDialog fragment = new LoadingDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE_ID, messageId);
        args.putBoolean(ARG_CANCELABLE, cancelable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create a view by inflating desired layout
        View v = inflater.inflate(R.layout.loading_dialog, container, false);

        // Allow or disallow touches with other visible windows
        v.setFilterTouchesWhenObscured(
                PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(getContext())
        );

        // set message
        TextView tv = v.findViewById(R.id.loadingText);
        int messageId = getArguments().getInt(ARG_MESSAGE_ID, R.string.placeholder_sentence);
        tv.setText(messageId);

        // set progress wheel color
        ProgressBar progressBar = v.findViewById(R.id.loadingBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(getActivity(), R.color.color_accent),
                PorterDuff.Mode.SRC_IN
        );

        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        /// set cancellation behavior
        boolean cancelable = getArguments().getBoolean(ARG_CANCELABLE, false);
        dialog.setCancelable(cancelable);
        if (!cancelable) {
            // disable the back button
            DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {

                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            };
            dialog.setOnKeyListener(keyListener);
        }
        DialogExtKt.avoidScreenshotsIfNeeded(dialog);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
