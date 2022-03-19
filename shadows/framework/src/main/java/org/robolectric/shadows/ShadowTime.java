package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.os.SystemClock;
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
  @RealObject private Time time;

  @Implementation(maxSdk = KITKAT_WATCH)
  protected void setToNow() {
    time.set(SystemClock.currentThreadTimeMillis());
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
    // long millis = (julianDay - EPOCH_JULIAN_DAY) * DateUtils.DAY_IN_MILLIS;
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
        c.get(Calendar.YEAR));
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
    if ("UTC".equals(time.timezone)) {
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
  protected boolean nativeParse3339(String s) {
    // In lollipop, the native implementation was replaced with java
    // this is a copy of the aosp-pie implementation
    int len = s.length();
    if (len < 10) {
      throwTimeFormatException("String too short --- expected at least 10 characters.");
    }
    boolean inUtc = false;

    // year
    int n = getChar(s, 0, 1000);
    n += getChar(s, 1, 100);
    n += getChar(s, 2, 10);
    n += getChar(s, 3, 1);
    time.year = n;

    checkChar(s, 4, '-');

    // month
    n = getChar(s, 5, 10);
    n += getChar(s, 6, 1);
    --n;
    time.month = n;

    checkChar(s, 7, '-');

    // day
    n = getChar(s, 8, 10);
    n += getChar(s, 9, 1);
    time.monthDay = n;

    if (len >= 19) {
      // T
      checkChar(s, 10, 'T');
      time.allDay = false;

      // hour
      n = getChar(s, 11, 10);
      n += getChar(s, 12, 1);

      // Note that this.hour is not set here. It is set later.
      int hour = n;

      checkChar(s, 13, ':');

      // minute
      n = getChar(s, 14, 10);
      n += getChar(s, 15, 1);
      // Note that this.minute is not set here. It is set later.
      int minute = n;

      checkChar(s, 16, ':');

      // second
      n = getChar(s, 17, 10);
      n += getChar(s, 18, 1);
      time.second = n;

      // skip the '.XYZ' -- we don't care about subsecond precision.

      int tzIndex = 19;
      if (tzIndex < len && s.charAt(tzIndex) == '.') {
        do {
          tzIndex++;
        } while (tzIndex < len && Character.isDigit(s.charAt(tzIndex)));
      }

      int offset = 0;
      if (len > tzIndex) {
        char c = s.charAt(tzIndex);
        // NOTE: the offset is meant to be subtracted to get from local time
        // to UTC.  we therefore use 1 for '-' and -1 for '+'.
        switch (c) {
          case 'Z':
            // Zulu time -- UTC
            offset = 0;
            break;
          case '-':
            offset = 1;
            break;
          case '+':
            offset = -1;
            break;
          default:
            throwTimeFormatException(
                String.format(
                    "Unexpected character 0x%02d at position %d.  Expected + or -",
                    (int) c, tzIndex));
        }
        inUtc = true;

        if (offset != 0) {
          if (len < tzIndex + 6) {
            throwTimeFormatException(
                String.format("Unexpected length; should be %d characters", tzIndex + 6));
          }

          // hour
          n = getChar(s, tzIndex + 1, 10);
          n += getChar(s, tzIndex + 2, 1);
          n *= offset;
          hour += n;

          // minute
          n = getChar(s, tzIndex + 4, 10);
          n += getChar(s, tzIndex + 5, 1);
          n *= offset;
          minute += n;
        }
      }
      time.hour = hour;
      time.minute = minute;

      if (offset != 0) {
        time.normalize(false);
      }
    } else {
      time.allDay = true;
      time.hour = 0;
      time.minute = 0;
      time.second = 0;
    }

    time.weekDay = 0;
    time.yearDay = 0;
    time.isDst = -1;
    time.gmtoff = 0;
    return inUtc;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int getChar(String s, int spos, int mul) {
    char c = s.charAt(spos);
    if (Character.isDigit(c)) {
      return Character.getNumericValue(c) * mul;
    } else {
      throwTimeFormatException("Parse error at pos=" + spos);
    }
    return -1;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void checkChar(String s, int spos, char expected) {
    char c = s.charAt(spos);
    if (c != expected) {
      throwTimeFormatException(
          String.format(
              "Unexpected character 0x%02d at pos=%d.  Expected 0x%02d (\'%c\').",
              (int) c, spos, (int) expected, expected));
    }
  }

  private static void throwTimeFormatException(String optionalMessage) {
    throw ReflectionHelpers.callConstructor(
        TimeFormatException.class,
        ReflectionHelpers.ClassParameter.from(
            String.class, optionalMessage == null ? "fail" : optionalMessage));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected String format(String format) {
    return Strftime.format(
        format,
        new Date(toMillis(false)),
        Locale.getDefault(),
        TimeZone.getTimeZone(time.timezone));
  }

  private Calendar getCalendar() {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone(time.timezone));
    c.set(time.year, time.month, time.monthDay, time.hour, time.minute, time.second);
    c.set(Calendar.MILLISECOND, 0);
    return c;
  }
}
