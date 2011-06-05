package com.xtremelabs.robolectric.shadows;

import android.text.format.DateFormat;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class DateFormatTest {

    @Test
    public void getTimeFormat_returnsATimeFormat() {
        Date date = new Date();
        date.setTime(1000083987);
        assertEquals("07:48:03", DateFormat.getTimeFormat(null).format(date));
    }

    @Test
    public void getDateFormat_returnsADateFormat() {
        Date date = new Date();
        date.setTime(1000083987);
        assertEquals("Jan-12-1970", DateFormat.getDateFormat(null).format(date));
    }

}
