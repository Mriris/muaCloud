

package com.owncloud.android.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.owncloud.android.R;
import com.owncloud.android.extensions.DialogExtKt;

public class ConfirmationDialogFragment extends DialogFragment {

    public final static String ARG_MESSAGE_RESOURCE_ID = "resource_id";
    public final static String ARG_MESSAGE_ARGUMENTS = "string_array";
    public static final String ARG_TITLE_ID = "title_id";

    public final static String ARG_POSITIVE_BTN_RES = "positive_btn_res";
    public final static String ARG_NEUTRAL_BTN_RES = "neutral_btn_res";
    public final static String ARG_NEGATIVE_BTN_RES = "negative_btn_res";

    public static final String FTAG_CONFIRMATION = "CONFIRMATION_FRAGMENT";

    private ConfirmationDialogFragmentListener mListener;


    public static ConfirmationDialogFragment newInstance(
            int messageResId,
            String[] messageArguments,
            int titleResId,
            int posBtn,
            int neuBtn,
            int negBtn
    ) {

        if (messageResId == -1) {
            throw new IllegalStateException("Calling confirmation dialog without message resource");
        }

        ConfirmationDialogFragment frag = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE_RESOURCE_ID, messageResId);
        args.putStringArray(ARG_MESSAGE_ARGUMENTS, messageArguments);
        args.putInt(ARG_TITLE_ID, titleResId);
        args.putInt(ARG_POSITIVE_BTN_RES, posBtn);
        args.putInt(ARG_NEUTRAL_BTN_RES, neuBtn);
        args.putInt(ARG_NEGATIVE_BTN_RES, negBtn);
        frag.setArguments(args);
        return frag;
    }

    public void setOnConfirmationListener(ConfirmationDialogFragmentListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Object[] messageArguments = getArguments().getStringArray(ARG_MESSAGE_ARGUMENTS);
        int messageId = getArguments().getInt(ARG_MESSAGE_RESOURCE_ID, -1);
        int titleId = getArguments().getInt(ARG_TITLE_ID, -1);
        int posBtn = getArguments().getInt(ARG_POSITIVE_BTN_RES, -1);
        int neuBtn = getArguments().getInt(ARG_NEUTRAL_BTN_RES, -1);
        int negBtn = getArguments().getInt(ARG_NEGATIVE_BTN_RES, -1);

        if (messageArguments == null) {
            messageArguments = new String[]{};
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_warning)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(String.format(getString(messageId), messageArguments));

        if (titleId == 0) {
            builder.setTitle(android.R.string.dialog_alert_title);
        } else if (titleId != -1) {
            builder.setTitle(titleId);
        }

        if (posBtn != -1) {
            builder.setPositiveButton(posBtn,
                    (dialog, whichButton) -> {
                        if (mListener != null) {
                            mListener.onConfirmation(getTag());
                        }
                        dialog.dismiss();
                    });
        }
        if (neuBtn != -1) {
            builder.setNeutralButton(neuBtn,
                    (dialog, whichButton) -> {
                        if (mListener != null) {
                            mListener.onNeutral(getTag());
                        }
                        dialog.dismiss();
                    });
        }
        if (negBtn != -1) {
            builder.setNegativeButton(negBtn,
                    (dialog, which) -> {
                        if (mListener != null) {
                            mListener.onCancel(getTag());
                        }
                        dialog.dismiss();
                    });
        }
        Dialog d = builder.create();
        DialogExtKt.avoidScreenshotsIfNeeded(d);
        return d;
    }

    public interface ConfirmationDialogFragmentListener {
        void onConfirmation(String callerTag);

        void onNeutral(String callerTag);

        void onCancel(String callerTag);
    }
}
