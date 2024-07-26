
package com.owncloud.android.ui.adapter;

import android.view.View;
import android.widget.LinearLayout;

import com.owncloud.android.R;
import com.owncloud.android.lib.common.network.CertificateCombinedException;
import com.owncloud.android.ui.dialog.SslUntrustedCertDialog;
import com.owncloud.android.utils.PreferenceUtils;


public class CertificateCombinedExceptionViewAdapter implements SslUntrustedCertDialog.ErrorViewAdapter {

    private final CertificateCombinedException mSslException;

    public CertificateCombinedExceptionViewAdapter(CertificateCombinedException sslException) {
        mSslException = sslException;
    }

    @Override
    public void updateErrorView(View dialogView) {

        LinearLayout manageSpace = dialogView.findViewById(R.id.root);
        manageSpace.setFilterTouchesWhenObscured(
                PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(dialogView.getContext())
        );

        dialogView.findViewById(R.id.reason_no_info_about_error).setVisibility(View.GONE);

        if (mSslException.getCertPathValidatorException() != null) {
            dialogView.findViewById(R.id.reason_cert_not_trusted).setVisibility(View.VISIBLE);
        } else {
            dialogView.findViewById(R.id.reason_cert_not_trusted).setVisibility(View.GONE);
        }

        if (mSslException.getCertificateExpiredException() != null) {
            dialogView.findViewById(R.id.reason_cert_expired).setVisibility(View.VISIBLE);
        } else {
            dialogView.findViewById(R.id.reason_cert_expired).setVisibility(View.GONE);
        }

        if (mSslException.getCertificateNotYetValidException() != null) {
            dialogView.findViewById(R.id.reason_cert_not_yet_valid).setVisibility(View.VISIBLE);
        } else {
            dialogView.findViewById(R.id.reason_cert_not_yet_valid).setVisibility(View.GONE);
        }

        if (mSslException.getSslPeerUnverifiedException() != null) {
            dialogView.findViewById(R.id.reason_hostname_not_verified).setVisibility(View.VISIBLE);
        } else {
            dialogView.findViewById(R.id.reason_hostname_not_verified).setVisibility(View.GONE);
        }

    }
}
