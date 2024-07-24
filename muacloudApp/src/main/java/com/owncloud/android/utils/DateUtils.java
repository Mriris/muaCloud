

package com.owncloud.android.utils;

import java.util.Calendar;
import java.util.Date;


public class DateUtils {


    public static Date addDaysToDate(Date defaultDate, int days) {

        Calendar c = Calendar.getInstance();
        c.setTime(defaultDate);
        c.add(Calendar.DATE, days);
        defaultDate = c.getTime();

        return defaultDate;
    }
}
