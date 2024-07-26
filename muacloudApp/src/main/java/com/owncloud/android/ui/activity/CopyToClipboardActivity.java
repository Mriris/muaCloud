

package com.owncloud.android.ui.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.owncloud.android.R;
import timber.log.Timber;


@SuppressWarnings("deprecation")
public class CopyToClipboardActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);

            Intent intent = getIntent();
            CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);

            if (text != null && text.length() > 0) {

                ClipData clip = ClipData.newPlainText(
                        getString(R.string.clipboard_label, getString(R.string.app_name)),
                        text
                );
                clipboardManager.setPrimaryClip(clip);



                Toast.makeText(this, R.string.clipboard_text_copied, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.clipboard_no_text_to_copy, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, R.string.clipboard_uxexpected_error, Toast.LENGTH_SHORT).show();
            Timber.e(e, "Exception caught while copying to clipboard");
        }

        finish();
    }

}
