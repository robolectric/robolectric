package com.xtremelabs.robolectric.shadows;

import android.text.format.DateFormat;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class DateFormatTest {

    @Test
    public void getTimeFormat_returnsATimeFormat() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.HOUR, 7);
        cal.set(Calendar.MINUTE, 48);
        cal.set(Calendar.SECOND, 3);
        Date date = cal.getTime();
        assertEquals("07:48:03", DateFormat.getTimeFormat(null).format(date));
    }

    @Test
    public void getDateFormat_returnsADateFormat() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.DATE, 12);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.YEAR, 1970);
        Date date = cal.getTime();
        assertEquals("Jan-12-1970", DateFormat.getDateFormat(null).format(date));
    }

}
