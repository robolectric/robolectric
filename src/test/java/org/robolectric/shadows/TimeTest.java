package org.robolectric.shadows;

import java.util.Arrays;
import java.util.TimeZone;

import android.os.SystemClock;
import android.text.format.Time;
import android.util.TimeFormatException;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class TimeTest {
  
  static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();
  
  @After
  public void tearDown() {
    // Just in case any of the tests mess with the system-wide
    // default time zone, make sure we've set it back to what
    // it should be.
    TimeZone.setDefault(DEFAULT_TIMEZONE);
  }
  
  @Test
  public void shouldSetToNow() throws Exception {
    Time t = new Time();
    SystemClock.setCurrentTimeMillis(1000);
    t.setToNow();
    assertThat(t.toMillis(false)).isEqualTo(1000);
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
  public void shouldSwitchTimeZones() throws Exception {
    Time t = new Time("UTC");
  	
    t.set(1414213562373L);
    assertThat(t.timezone).isEqualTo("UTC");
    assertThat(t.gmtoff).isEqualTo(0);
    assertThat(t.format3339(false)).isEqualTo("2014-10-25T05:06:02.000Z");
    
    t.switchTimezone("America/New_York");
    assertThat(t.format3339(false)).isEqualTo("2014-10-25T01:06:02.000-04:00");
    assertThat(t.timezone).isEqualTo("America/New_York");
    assertThat(t.gmtoff).isEqualTo(-14400L);
    assertThat(t.toMillis(true)).isEqualTo(1414213562000L);
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
  
  @Test
  public void shouldParseRfc3339() {
    for (String tz : Arrays.asList("Europe/Berlin",
                                   "America/Los Angeles",
                                   "Australia/Adelaide")) {
      String desc = "Eval when local timezone is " + tz;
      TimeZone.setDefault(TimeZone.getTimeZone(tz));

      Time t = new Time("Europe/Berlin");
      assertTrue(desc, t.parse3339("2008-10-13T16:30:50Z"));
      assertEquals(desc, 2008, t.year);
      assertEquals(desc, 9, t.month);
      assertEquals(desc, 13, t.monthDay);
      assertEquals(desc, 16, t.hour);
      assertEquals(desc, 30, t.minute);
      assertEquals(desc, 50, t.second);
      assertEquals(desc, "UTC", t.timezone);
      assertFalse(desc, t.allDay);

      t = new Time("Europe/Berlin");
      assertTrue(desc, t.parse3339("2008-10-13T16:30:50.1000+07:00"));
      assertEquals(desc, 2008, t.year);
      assertEquals(desc, 9, t.month);
      assertEquals(desc, 13, t.monthDay);
      assertEquals(desc, 9, t.hour);
      assertEquals(desc, 30, t.minute);
      assertEquals(desc, 51, t.second);
      assertEquals(desc, "UTC", t.timezone);
      assertFalse(desc, t.allDay);
    
      t = new Time("Europe/Berlin");
      assertTrue(desc, t.parse3339("2008-10-13T16:30:50.999-03"));
      assertEquals(desc, 2008, t.year);
      assertEquals(desc, 9, t.month);
      assertEquals(desc, 13, t.monthDay);
      assertEquals(desc, 19, t.hour);
      assertEquals(desc, 30, t.minute);
      assertEquals(desc, 50, t.second);
      assertEquals(desc, "UTC", t.timezone);
      assertFalse(desc, t.allDay);
    
      t = new Time("Europe/Berlin");
      assertFalse(desc, t.parse3339("2008-10-13"));
      assertEquals(desc, 2008, t.year);
      assertEquals(desc, 9, t.month);
      assertEquals(desc, 13, t.monthDay);
      assertEquals(desc, 0, t.hour);
      assertEquals(desc, 0, t.minute);
      assertEquals(desc, 0, t.second);
      assertEquals(desc, "Europe/Berlin", t.timezone);
      assertTrue(desc, t.allDay);
    }
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
    Time t = new Time(Time.TIMEZONE_UTC);
    t.set(3600000L);
    assertEquals("Hello epoch 01 1970 01", t.format("Hello epoch %d %Y %d"));
    assertEquals("Hello epoch 1:00 AM", t.format("Hello epoch %l:%M %p"));
  }

  @Test
  public void shouldFormat2445() throws Exception {
    Time t = new Time();
    assertEquals("19700101T000000", t.format2445());
    t.timezone = Time.TIMEZONE_UTC;
    //2445 formatted date should hava a Z postfix
    assertEquals("19700101T000000Z",t.format2445());
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
