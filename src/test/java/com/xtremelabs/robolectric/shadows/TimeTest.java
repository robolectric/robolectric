package com.xtremelabs.robolectric.shadows;

import android.text.format.Time;
import android.util.TimeFormatException;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class TimeTest {
    @Test
    public void shouldSetToNow() throws Exception {
        Time t = new Time();
        t.setToNow();
        assertThat(t.toMillis(false), not(equalTo(0l)));
    }

    @Test
    public void shouldHaveNoArgsConstructor() throws Exception {
        Time t = new Time();
        assertNotNull(t.timezone);
    }

    @Test
    public void shouldHaveCopyConstructor() throws Exception {
        Time t = new Time();
        t.setToNow();
        Time t2 = new Time(t);
        assertEquals(t.timezone, t2.timezone);
        assertEquals(t.year, t2.year);
        assertEquals(t.month, t2.month);
        assertEquals(t.monthDay, t2.monthDay);
        assertEquals(t.hour, t2.hour);
        assertEquals(t.minute, t2.minute);
        assertEquals(t.second, t2.second);
    }

    @Test
    public void shouldHaveSetTime() throws Exception {
        Time t = new Time();
        t.setToNow();
        Time t2 = new Time();
        t2.set(t);
        assertEquals(t.timezone, t2.timezone);
        assertEquals(t.year, t2.year);
        assertEquals(t.month, t2.month);
        assertEquals(t.monthDay, t2.monthDay);
        assertEquals(t.hour, t2.hour);
        assertEquals(t.minute, t2.minute);
        assertEquals(t.second, t2.second);
    }

    @Test
    public void shouldHaveSet3Args() throws Exception {
        Time t = new Time();
        t.set(1, 1, 2000);
        assertEquals(t.year, 2000);
        assertEquals(t.month, 1);
        assertEquals(t.monthDay, 1);
    }

    @Test
    public void shouldHaveSet6Args() throws Exception {
        Time t = new Time();
        t.set(1, 1, 1, 1, 1, 2000);
        assertEquals(t.year, 2000);
        assertEquals(t.month, 1);
        assertEquals(t.monthDay, 1);
        assertEquals(t.second, 1);
        assertEquals(t.minute, 1);
        assertEquals(t.hour, 1);
    }

    @Test
    public void shouldHaveTimeZoneConstructor() throws Exception {
        Time t = new Time("UTC");
        assertEquals(t.timezone, "UTC");
    }

    @Test
    public void shouldClear() throws Exception {
        Time t = new Time();
        t.setToNow();
        t.clear("UTC");
        assertEquals("UTC", t.timezone);
        assertEquals(0, t.year);
        assertEquals(0, t.month);
        assertEquals(0, t.monthDay);
        assertEquals(0, t.hour);
        assertEquals(0, t.minute);
        assertEquals(0, t.second);
        assertEquals(0, t.weekDay);
        assertEquals(0, t.yearDay);
        assertEquals(0, t.gmtoff);
        assertEquals(-1, t.isDst);
    }

    @Test
    public void shouldHaveToMillis() throws Exception {
        Time t = new Time();
        t.set(86400 * 1000);
        assertEquals(86400 * 1000, t.toMillis(false));
    }

    @Test
    public void shouldHaveCurrentTimeZone() throws Exception {
        assertNotNull(Time.getCurrentTimezone());
    }

    @Test
    public void shouldHaveCompareAndBeforeAfter() throws Exception {
        Time a = new Time();
        Time b = new Time();
        assertEquals(0, Time.compare(a, b));
        assertFalse(a.before(b));
        assertFalse(a.after(b));
        a.year = 2000;
        assertEquals(1, Time.compare(a, b));
        assertTrue(a.after(b));
        assertTrue(b.before(a));
        b.year = 2001;
        assertEquals(-1, Time.compare(a, b));
        assertTrue(b.after(a));
        assertTrue(a.before(b));
    }

    @Test
    public void shouldHaveParse() throws Exception {
        Time t = new Time("Europe/Berlin");
        assertFalse(t.parse("20081013T160000"));
        assertEquals(2008, t.year);
        assertEquals(9, t.month);
        assertEquals(13, t.monthDay);
        assertEquals(16, t.hour);
        assertEquals(0, t.minute);
        assertEquals(0, t.second);

        assertTrue(t.parse("20081013T160000Z"));
        assertEquals(2008, t.year);
        assertEquals(9, t.month);
        assertEquals(13, t.monthDay);
        assertEquals(16, t.hour);
        assertEquals(0, t.minute);
        assertEquals(0, t.second);
    }

    @Test(expected = TimeFormatException.class)
    public void shouldThrowTimeFormatException() throws Exception {
        Time t = new Time();
        t.parse("BLARGH");
    }

    @Test
    public void shouldHaveParseShort() throws Exception {
        Time t = new Time();
        t.parse("20081013");
        assertEquals(2008, t.year);
        assertEquals(9, t.month);
        assertEquals(13, t.monthDay);
        assertEquals(0, t.hour);
        assertEquals(0, t.minute);
        assertEquals(0, t.second);
    }

    @Test
    public void shouldFormat() throws Exception {
        Time t = new Time();
        assertEquals("Hallo epoch 01 1970 01", t.format("Hallo epoch %d %Y %d"));
    }

    @Test
    public void shouldFormat2445() throws Exception {
        Time t = new Time();
        assertEquals("19700101T000000", t.format2445());
    }

    @Test
    public void shouldFormat3339() throws Exception {
        Time t = new Time("Europe/Berlin");
        assertEquals("1970-01-01T00:00:00.000+00:00", t.format3339(false));
        assertEquals("1970-01-01", t.format3339(true));
    }

    @Test
    public void testIsEpoch() throws Exception {
        Time t = new Time();
        boolean isEpoch = Time.isEpoch(t);
        assertEquals(true, isEpoch);
    }

    @Test
    public void testGetJulianDay() throws Exception {
        Time time = new Time();

        time.set(0, 0, 0, 12, 5, 2008);
        time.timezone = "Australia/Sydney";
        long millis = time.normalize(true);

        // This is the Julian day for 12am for this day of the year
        int julianDay = Time.getJulianDay(millis, time.gmtoff);

        // Change the time during the day and check that we get the same
        // Julian day.
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                time.set(0, minute, hour, 12, 5, 2008);
                millis = time.normalize(true);
                int day = Time.getJulianDay(millis, time.gmtoff);

                assertEquals(day, julianDay);
            }
        }
    }

    @Test
    public void testSetJulianDay() throws Exception {
        Time time = new Time();
        time.set(0, 0, 0, 12, 5, 2008);
        time.timezone = "Australia/Sydney";
        long millis = time.normalize(true);

        int julianDay = Time.getJulianDay(millis, time.gmtoff);
        time.setJulianDay(julianDay);

        assertTrue(time.hour == 0 || time.hour == 1);
        assertEquals(0, time.minute);
        assertEquals(0, time.second);

        millis = time.toMillis(false);
        int day = Time.getJulianDay(millis, time.gmtoff);

        assertEquals(day, julianDay);
    }
}