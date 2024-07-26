
package com.owncloud.android.ui.adapter;

import android.net.http.SslError;
import android.view.View;
import android.widget.LinearLayout;

import com.owncloud.android.R;
import com.owncloud.android.ui.dialog.SslUntrustedCertDialog;
import com.owncloud.android.utils.PreferenceUtils;


public class SslErrorViewAdapter implements SslUntrustedCertDialog.ErrorViewAdapter {

    private final SslError mSslError;

    public SslErrorViewAdapter(SslError sslError) {
        mSslError = sslError;
    }

    @Override
    public void updateErrorView(View dialogView) {

        LinearLayout manageSpace = dialogView.findViewById(R.id.root);
        manageSpace.setFilterTouchesWhenObscured(
                PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(dialogView.getContext())
        );

        dialogView.findViewById(R.id.reason_no_info_about_error).setVisibility(View.GONE);

        if (mSslError.hasError(SslError.SSL_UNTRUSTED)) {
            dialogView.findViewById(R.id.reason_cert_not_trusted).setVisibility(View.VISIBLE);
        } else {
            dialogView.findViewById(R.id.reason_cert_not_trusted).setVisibility(View.GONE);
        }

        if (mSslError.hasError(SslError.SSL_EXPIRED)) {
            dialogView.findViewById(R.id.reason_cert_expired).setVisibility(View.VISIBLE);
        } else {
            dialogView.findViewById(R.id.reason_cert_expired).setVisibility(View.GONE);
        }

        if (mSslError.getPrimaryError() == SslError.SSL_NOTYETVALID) {
            dialogView.findViewById(R.id.reason_cert_not_yet_valid).setVisibility(View.VISIBLE);
        } else {
            dialogView.findViewById(R.id.reason_cert_not_yet_valid).setVisibility(View.GONE);
        }

        if (mSslError.getPrimaryError() == SslError.SSL_IDMISMATCH) {
            dialogView.findViewById(R.id.reason_hostname_not_verified).setVisibility(View.VISIBLE);
        } else {
            dialogView.findViewById(R.id.reason_hostname_not_verified).setVisibility(View.GONE);
        }
    }

}
