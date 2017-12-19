package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.text.format.Time;
import android.util.TimeFormatException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Strftime;

@Implements(value = Time.class)
public class ShadowTime {
  @RealObject
  private Time time;

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void setToNow() {
    time.set(ShadowSystemClock.currentTimeMillis());
  }

  private static final long SECOND_IN_MILLIS = 1000;
  private static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
  private static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
  private static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void __constructor__() {
    __constructor__(getCurrentTimezone());
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void __constructor__(String timezone) {
    if (timezone == null) {
      throw new NullPointerException("timezone is null!");
    }
    time.timezone = timezone;
    time.year = 1970;
    time.monthDay = 1;
    time.isDst = -1;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void __constructor__(Time other) {
    set(other);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void set(Time other) {
    time.timezone = other.timezone;
    time.second = other.second;
    time.minute = other.minute;
    time.hour = other.hour;
    time.monthDay = other.monthDay;
    time.month = other.month;
    time.year = other.year;
    time.weekDay = other.weekDay;
    time.yearDay = other.yearDay;
    time.isDst = other.isDst;
    time.gmtoff = other.gmtoff;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean isEpoch(Time time) {
    long millis = time.toMillis(true);
    return getJulianDay(millis, 0) == Time.EPOCH_JULIAN_DAY;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int getJulianDay(long millis, long gmtoff) {
    long offsetMillis = gmtoff * 1000;
    long julianDay = (millis + offsetMillis) / DAY_IN_MILLIS;
    return (int) julianDay + Time.EPOCH_JULIAN_DAY;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected long setJulianDay(int julianDay) {
    // Don't bother with the GMT offset since we don't know the correct
    // value for the given Julian day.  Just get close and then adjust
    // the day.
    //long millis = (julianDay - EPOCH_JULIAN_DAY) * DateUtils.DAY_IN_MILLIS;
    long millis = (julianDay - Time.EPOCH_JULIAN_DAY) * DAY_IN_MILLIS;
    set(millis);

    // Figure out how close we are to the requested Julian day.
    // We can't be off by more than a day.
    int approximateDay = getJulianDay(millis, time.gmtoff);
    int diff = julianDay - approximateDay;
    time.monthDay += diff;

    // Set the time to 12am and re-normalize.
    time.hour = 0;
    time.minute = 0;
    time.second = 0;
    millis = time.normalize(true);
    return millis;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void set(long millis) {
    Calendar c = getCalendar();
    c.setTimeInMillis(millis);
    set(
        c.get(Calendar.SECOND),
        c.get(Calendar.MINUTE),
        c.get(Calendar.HOUR_OF_DAY),
        c.get(Calendar.DAY_OF_MONTH),
        c.get(Calendar.MONTH),
        c.get(Calendar.YEAR)
        );
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected long toMillis(boolean ignoreDst) {
    Calendar c = getCalendar();
    return c.getTimeInMillis();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void set(int second, int minute, int hour, int monthDay, int month, int year) {
    time.second = second;
    time.minute = minute;
    time.hour = hour;
    time.monthDay = monthDay;
    time.month = month;
    time.year = year;
    time.weekDay = 0;
    time.yearDay = 0;
    time.isDst = -1;
    time.gmtoff = 0;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void set(int monthDay, int month, int year) {
    set(0, 0, 0, monthDay, month, year);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void clear(String timezone) {
    if (timezone == null) {
      throw new NullPointerException("timezone is null!");
    }
    time.timezone = timezone;
    time.allDay = false;
    time.second = 0;
    time.minute = 0;
    time.hour = 0;
    time.monthDay = 0;
    time.month = 0;
    time.year = 0;
    time.weekDay = 0;
    time.yearDay = 0;
    time.gmtoff = 0;
    time.isDst = -1;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static String getCurrentTimezone() {
    return TimeZone.getDefault().getID();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void switchTimezone(String timezone) {
    long date = toMillis(true);
    long gmtoff = TimeZone.getTimeZone(timezone).getOffset(date);
    set(date + gmtoff);
    time.timezone = timezone;
    time.gmtoff = (gmtoff / 1000);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int compare(Time a, Time b) {
    long ams = a.toMillis(false);
    long bms = b.toMillis(false);
    if (ams == bms) {
      return 0;
    } else if (ams < bms) {
      return -1;
    } else {
      return 1;
    }
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected boolean before(Time other) {
    return Time.compare(time, other) < 0;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected boolean after(Time other) {
    return Time.compare(time, other) > 0;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected boolean parse(String timeString) {
    TimeZone tz;
    if (timeString.endsWith("Z")) {
      timeString = timeString.substring(0, timeString.length() - 1);
      tz = TimeZone.getTimeZone("UTC");
    } else {
      tz = TimeZone.getTimeZone(time.timezone);
    }
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.ENGLISH);
    SimpleDateFormat dfShort = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    df.setTimeZone(tz);
    dfShort.setTimeZone(tz);
    time.timezone = tz.getID();
    try {
      set(df.parse(timeString).getTime());
    } catch (ParseException e) {
      try {
        set(dfShort.parse(timeString).getTime());
      } catch (ParseException e2) {
        throwTimeFormatException(e2.getLocalizedMessage());
      }
    }
    return "UTC".equals(tz.getID());
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected String format2445() {
    String value = format("%Y%m%dT%H%M%S");
    if ( "UTC".equals(time.timezone)){
      value += "Z";
    }
    return value;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected String format3339(boolean allDay) {
    if (allDay) {
      return format("%Y-%m-%d");
    } else if ("UTC".equals(time.timezone)) {
      return format("%Y-%m-%dT%H:%M:%S.000Z");
    } else {
      String base = format("%Y-%m-%dT%H:%M:%S.000");
      String sign = (time.gmtoff < 0) ? "-" : "+";
      int offset = (int) Math.abs(time.gmtoff);
      int minutes = (offset % 3600) / 60;
      int hours = offset / 3600;
      return String.format("%s%s%02d:%02d", base, sign, hours, minutes);
    }
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected boolean parse3339(String rfc3339String) {
    SimpleDateFormat formatter =  new SimpleDateFormat();
    // Special case Date without time first
    if (rfc3339String.matches("\\d{4}-\\d{2}-\\d{2}")) {
      final TimeZone tz = TimeZone.getTimeZone(time.timezone);
      formatter.applyLocalizedPattern("yyyy-MM-dd");
      // Make sure we inferFromValue the date in the context of the specified time zone
      // instead of the system default time zone.
      formatter.setTimeZone(tz);
      Calendar calendar = Calendar.getInstance(tz, Locale.getDefault());
      try {
        calendar.setTime(formatter.parse(rfc3339String));
      } catch (java.text.ParseException e) {
        throwTimeFormatException(e.getLocalizedMessage());
      }
      time.second = time.minute = time.hour = 0;
      time.monthDay = calendar.get(Calendar.DAY_OF_MONTH);
      time.month = calendar.get(Calendar.MONTH);
      time.year = calendar.get(Calendar.YEAR);
      time.weekDay = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
      time.yearDay = calendar.get(Calendar.DAY_OF_YEAR);
      time.isDst = calendar.get(Calendar.DST_OFFSET) != 0 ? 1 : 0;
      time.allDay = true;
      return false;
    }

    // Store a string normalized for SimpleDateFormat;
    String dateString = rfc3339String
        // Look-ahead to remove the colon followed by minutes in timezone
        .replaceFirst(":(?=\\d{2}$)", "")
        // Look-behind to pad with minutes any timezone only defines hours
        .replaceFirst("(?<=[+-]\\d{2})$", "00")
        // If it ends with a Z, just replace it with no offset
        .replaceFirst("(Z)$", "+0000");

    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    formatter.applyLocalizedPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    long millisInUtc = time.toMillis(false);
    try {
      millisInUtc = formatter.parse(dateString).getTime();
    } catch (java.text.ParseException e1) {
      // Try again with fractional seconds.
      formatter.applyLocalizedPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      formatter.setLenient(true);
      try {
        millisInUtc = formatter.parse(dateString).getTime();
      } catch (java.text.ParseException e2) {
        throwTimeFormatException(e2.getLocalizedMessage());
      }
    }
    // Clear to UTC, then set time;
    clear("UTC");
    set(millisInUtc);
    return true;
  }

  private void throwTimeFormatException(String optionalMessage) {
    throw ReflectionHelpers.callConstructor(TimeFormatException.class, from(String.class, optionalMessage == null ? "fail" : optionalMessage));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected String format(String format) {
    return Strftime.format(format, new Date(toMillis(false)), Locale.getDefault(), TimeZone.getTimeZone(time.timezone));
  }

  private Calendar getCalendar() {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone(time.timezone));
    c.set(time.year, time.month, time.monthDay, time.hour, time.minute, time.second);
    c.set(Calendar.MILLISECOND, 0);
    return c;
  }
}
