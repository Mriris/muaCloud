
package com.owncloud.android.ui.adapter;

import android.net.http.SslCertificate;
import android.view.View;
import android.widget.TextView;

import com.owncloud.android.R;
import com.owncloud.android.ui.dialog.SslUntrustedCertDialog;

import java.text.DateFormat;
import java.util.Date;


public class SslCertificateViewAdapter implements SslUntrustedCertDialog.CertificateViewAdapter {

    private SslCertificate mCertificate;


    public SslCertificateViewAdapter(SslCertificate certificate) {
        mCertificate = certificate;
    }

    @Override
    public void updateCertificateView(View dialogView) {
        TextView nullCerView = dialogView.findViewById(R.id.null_cert);
        if (mCertificate != null) {
            nullCerView.setVisibility(View.GONE);
            showSubject(mCertificate.getIssuedTo(), dialogView);
            showIssuer(mCertificate.getIssuedBy(), dialogView);
            showValidity(mCertificate.getValidNotBeforeDate(), mCertificate.getValidNotAfterDate(), dialogView);
            hideSignature(dialogView);

        } else {
            nullCerView.setVisibility(View.VISIBLE);
        }
    }

    private void showValidity(Date notBefore, Date notAfter, View dialogView) {
        TextView fromView = dialogView.findViewById(R.id.value_validity_from);
        TextView toView = dialogView.findViewById(R.id.value_validity_to);
        DateFormat dateFormat = DateFormat.getDateInstance();
        fromView.setText(dateFormat.format(notBefore));
        toView.setText(dateFormat.format(notAfter));
    }

    private void showSubject(SslCertificate.DName subject, View dialogView) {
        TextView cnView = dialogView.findViewById(R.id.value_subject_CN);
        cnView.setText(subject.getCName());
        cnView.setVisibility(View.VISIBLE);

        TextView oView = dialogView.findViewById(R.id.value_subject_O);
        oView.setText(subject.getOName());
        oView.setVisibility(View.VISIBLE);

        TextView ouView = dialogView.findViewById(R.id.value_subject_OU);
        ouView.setText(subject.getUName());
        ouView.setVisibility(View.VISIBLE);

        dialogView.findViewById(R.id.value_subject_C).setVisibility(View.GONE);
        dialogView.findViewById(R.id.value_subject_ST).setVisibility(View.GONE);
        dialogView.findViewById(R.id.value_subject_L).setVisibility(View.GONE);
        dialogView.findViewById(R.id.label_subject_C).setVisibility(View.GONE);
        dialogView.findViewById(R.id.label_subject_ST).setVisibility(View.GONE);
        dialogView.findViewById(R.id.label_subject_L).setVisibility(View.GONE);
    }

    private void showIssuer(SslCertificate.DName issuer, View dialogView) {
        TextView cnView = dialogView.findViewById(R.id.value_issuer_CN);
        cnView.setText(issuer.getCName());
        cnView.setVisibility(View.VISIBLE);

        TextView oView = dialogView.findViewById(R.id.value_issuer_O);
        oView.setText(issuer.getOName());
        oView.setVisibility(View.VISIBLE);

        TextView ouView = dialogView.findViewById(R.id.value_issuer_OU);
        ouView.setText(issuer.getUName());
        ouView.setVisibility(View.VISIBLE);

        dialogView.findViewById(R.id.value_issuer_C).setVisibility(View.GONE);
        dialogView.findViewById(R.id.value_issuer_ST).setVisibility(View.GONE);
        dialogView.findViewById(R.id.value_issuer_L).setVisibility(View.GONE);
        dialogView.findViewById(R.id.label_issuer_C).setVisibility(View.GONE);
        dialogView.findViewById(R.id.label_issuer_ST).setVisibility(View.GONE);
        dialogView.findViewById(R.id.label_issuer_L).setVisibility(View.GONE);
    }

    private void hideSignature(View dialogView) {
        dialogView.findViewById(R.id.label_signature).setVisibility(View.GONE);
        dialogView.findViewById(R.id.label_signature_algorithm).setVisibility(View.GONE);
        dialogView.findViewById(R.id.value_signature_algorithm).setVisibility(View.GONE);
        dialogView.findViewById(R.id.value_signature).setVisibility(View.GONE);
    }

}
