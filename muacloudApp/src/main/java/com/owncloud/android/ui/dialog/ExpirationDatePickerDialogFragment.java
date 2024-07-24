

package com.owncloud.android.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.owncloud.android.R;
import com.owncloud.android.extensions.DialogExtKt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ExpirationDatePickerDialogFragment
        extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {


    public static final String DATE_PICKER_DIALOG = "DATE_PICKER_DIALOG";


    private static final String ARG_CHOSEN_DATE_IN_MILLIS = "CHOSEN_DATE_IN_MILLIS";


    private static final String ARG_MAX_DATE_IN_MILLIS = "MAX_DATE_IN_MILLIS";

    private DatePickerFragmentListener datePickerListener;


    public static ExpirationDatePickerDialogFragment newInstance(long chosenDateInMillis,
                                                                 long maxDateInMillis
    ) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_CHOSEN_DATE_IN_MILLIS, chosenDateInMillis);
        arguments.putLong(ARG_MAX_DATE_IN_MILLIS, maxDateInMillis);

        ExpirationDatePickerDialogFragment dialog = new ExpirationDatePickerDialogFragment();
        dialog.setArguments(arguments);
        return dialog;
    }

    public static DateFormat getDateFormat() {
        return SimpleDateFormat.getDateInstance();
    }


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Chosen date received as an argument must be later than tomorrow ; default to tomorrow in other case
        final Calendar chosenDate = Calendar.getInstance();
        long tomorrowInMillis = chosenDate.getTimeInMillis() + DateUtils.DAY_IN_MILLIS;
        long chosenDateInMillis = getArguments().getLong(ARG_CHOSEN_DATE_IN_MILLIS);
        long maxDateInMillis = getArguments().getLong(ARG_MAX_DATE_IN_MILLIS);

        if (chosenDateInMillis < tomorrowInMillis) {
            chosenDateInMillis = tomorrowInMillis;
        }
        chosenDate.setTimeInMillis(chosenDateInMillis);

        // Create a new instance of DatePickerDialog
        DatePickerDialog dialog = new DatePickerDialog(
                getActivity(),
                this,
                chosenDate.get(Calendar.YEAR),
                chosenDate.get(Calendar.MONTH),
                chosenDate.get(Calendar.DAY_OF_MONTH)
        );

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.share_cancel_public_link_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_NEGATIVE) {
                            // Do Stuff
                            notifyDatePickerListener(null);
                        }
                    }
                });

        // Prevent days in the past may be chosen
        DatePicker picker = dialog.getDatePicker();
        if (maxDateInMillis >= chosenDateInMillis) {
            // the extra second (+1000) is required to prevent a bug of DatePicker that shows
            // an extra header with the selected date if maxDateInMillis == chosenDateInMillis
            picker.setMaxDate(maxDateInMillis + 1000);
        }
        picker.setMinDate(tomorrowInMillis - 1000);

        // Enforce spinners view; ignored by MD-based theme in Android >=5, but calendar is REALLY buggy
        // in Android < 5, so let's be sure it never appears (in tablets both spinners and calendar are
        // shown by default)
        picker.setCalendarViewShown(false);

        DialogExtKt.avoidScreenshotsIfNeeded(dialog);

        return dialog;
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        Calendar chosenDate = Calendar.getInstance();
        chosenDate.set(Calendar.YEAR, year);
        chosenDate.set(Calendar.MONTH, monthOfYear);
        chosenDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        long chosenDateInMillis = chosenDate.getTimeInMillis();

        String formattedDate = getDateFormat().format(new Date(chosenDateInMillis));

        // Call the listener and pass the date back to it
        notifyDatePickerListener(formattedDate);
    }

    public interface DatePickerFragmentListener {

        void onDateSet(String date);

        void onCancelDatePicker();
    }

    public void setDatePickerListener(DatePickerFragmentListener listener) {
        this.datePickerListener = listener;
    }


    protected void notifyDatePickerListener(String date) {
        if (this.datePickerListener != null) {

            if (date != null) {
                this.datePickerListener.onDateSet(date);
            } else {
                this.datePickerListener.onCancelDatePicker();
            }
        }
    }

}
