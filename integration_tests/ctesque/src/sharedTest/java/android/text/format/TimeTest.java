package android.text.format;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.util.TimeFormatException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.util.Arrays;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class TimeTest {

  @Test
  public void shouldParseRfc3339() {
    for (String tz : Arrays.asList("Europe/Berlin", "America/Los Angeles", "Australia/Adelaide")) {
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
      assertTrue(desc, t.parse3339("2008-10-13T16:30:50.000+07:00"));
      assertEquals(desc, 2008, t.year);
      assertEquals(desc, 9, t.month);
      assertEquals(desc, 13, t.monthDay);
      assertEquals(desc, 9, t.hour);
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

  private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getDefault();

  @After
  public void tearDown() {
    // Just in case any of the tests mess with the system-wide
    // default time zone, make sure we've set it back to what
    // it should be.
    TimeZone.setDefault(DEFAULT_TIMEZONE);
  }

  @Test
  public void shouldHaveNoArgsConstructor() {
    Time t = new Time();
    assertNotNull(t.timezone);
  }

  @Test
  public void shouldHaveCopyConstructor() {
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
  public void shouldHaveSetTime() {
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
  public void shouldHaveSet3Args() {
    Time t = new Time();
    t.set(1, 1, 2000);
    assertEquals(t.year, 2000);
    assertEquals(t.month, 1);
    assertEquals(t.monthDay, 1);
  }

  @Test
  public void shouldHaveSet6Args() {
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
  public void shouldHaveTimeZoneConstructor() {
    Time t = new Time("UTC");
    assertEquals(t.timezone, "UTC");
  }

  @Test
  public void shouldClear() {
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
  public void shouldHaveToMillis() {
    Time t = new Time();
    t.set(86400 * 1000);
    assertEquals(86400 * 1000, t.toMillis(false));
  }

  @Test
  public void shouldHaveCurrentTimeZone() {
    assertNotNull(Time.getCurrentTimezone());
  }

  @Test
  public void shouldSwitchTimeZones() {
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
  public void shouldHaveCompareAndBeforeAfter() {
    Time a = new Time();
    Time b = new Time();

    assertThat(Time.compare(a, b)).isEqualTo(0);
    assertThat(a.before(b)).isFalse();
    assertThat(a.after(b)).isFalse();

    a.year = 2000;
    assertThat(Time.compare(a, b)).isAtLeast(0);
    assertThat(a.after(b)).isTrue();
    assertThat(b.before(a)).isTrue();

    b.year = 2001;
    assertThat(Time.compare(a, b)).isAtMost(0);
    assertThat(b.after(a)).isTrue();
    assertThat(a.before(b)).isTrue();
  }

  @Test
  public void shouldHaveParse() {
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
  public void shouldThrowTimeFormatException() {
    Time t = new Time();
    assertThrows(TimeFormatException.class, () -> t.parse("BLARGH"));
  }

  @Test
  public void shouldHaveParseShort() {
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
  @SdkSuppress(minSdkVersion = JELLY_BEAN_MR1)
  public void shouldFormat() {
    Time t = new Time(Time.TIMEZONE_UTC);
    t.set(3600000L);

    assertEquals("Hello epoch 01 1970 01", t.format("Hello epoch %d %Y %d"));
    assertEquals("Hello epoch  1:00 AM", t.format("Hello epoch %l:%M %p"));
  }

  @Test
  @SdkSuppress(minSdkVersion = JELLY_BEAN_MR1)
  public void shouldFormatAndroidStrings() {
    Time t = new Time("UTC");
    // NOTE: month is zero-based.
    t.set(12, 13, 14, 8, 8, 1987);

    assertEquals(1987, t.year);
    assertEquals(8, t.month);
    assertEquals(8, t.monthDay);
    assertEquals(14, t.hour);
    assertEquals(13, t.minute);
    assertEquals(12, t.second);

    // ICS

    // date_and_time
    assertEquals(
        "Sep 8, 1987, 2:13:12 PM",
        t.format("%b %-e, %Y, %-l:%M:%S %p"));

    // hour_minute_cap_ampm
    assertEquals(
        "2:13PM",
        t.format("%-l:%M%^p"));
  }

  @Test
  public void shouldFormat2445() {
    Time t = new Time();
    t.timezone = "PST";
    assertEquals("19700101T000000", t.format2445());

    t.timezone = Time.TIMEZONE_UTC;
    //2445 formatted date should hava a Z postfix
    assertEquals("19700101T000000Z",t.format2445());
  }

  @Test
  public void shouldFormat3339() {
    Time t = new Time("Europe/Berlin");
    assertEquals("1970-01-01T00:00:00.000+00:00", t.format3339(false));
    assertEquals("1970-01-01", t.format3339(true));
  }

  @Test
  public void testIsEpoch() {
    Time t = new Time(Time.TIMEZONE_UTC);
    assertThat(Time.isEpoch(t)).isTrue();
  }

  @Test
  public void testGetJulianDay() {
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
  public void testSetJulianDay() {
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
