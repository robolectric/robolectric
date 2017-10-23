package org.robolectric.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * An implementation of the Unix strftime with some glibc extensions.
 */
public class Strftime {

  /**
   * Format a date string.
   *
   * @param format The format in strftime syntax.
   * @param date The date to format.
   * @param locale The locale to use for formatting.
   * @param zone The timezone to use for formatting.
   * @return The formatted datetime.
   */
  public static String format(String format, final Date date, Locale locale, TimeZone zone) {
    StringBuilder buffer = new StringBuilder();

    class Formatter {
      SimpleDateFormat formatter;

      public Formatter(
          Date date,
          Locale locale,
          TimeZone timeZone) {
        if (locale != null) {
          formatter = new SimpleDateFormat("", locale);
        } else {
          formatter = new SimpleDateFormat("");
        }
        if (timeZone != null) {
          formatter.setTimeZone(timeZone);
        }
      }

      public String format(String format) {
        formatter.applyPattern(format);
        return formatter.format(date);
      }
    }

    Formatter formatter = new Formatter(date, locale, zone);

    Boolean inside = false;

    Boolean removePad = false;
    Boolean zeroPad = false;
    Boolean spacePad = false;

    Boolean upperCase = false;
    Boolean swapCase = false;

    StringBuilder padWidthBuffer = new StringBuilder();

    for (int i = 0; i < format.length(); i++) {
      Character c = format.charAt(i);

      if (!inside && c == '%') {
        inside = true;
        removePad = false;
        zeroPad = false;
        spacePad = false;
        upperCase = false;
        swapCase = false;
        padWidthBuffer = new StringBuilder();
      } else if(inside) {
        inside = false;
        switch (c) {
          // %a  Abbreviated weekday name according to locale.
          case 'a':
            buffer.append(
                correctCase(
                    formatter.format("EEE"),
                    upperCase,
                    swapCase));
            break;

          // %A  Full weekday name according to locale.
          case 'A':
            buffer.append(
                correctCase(
                    formatter.format("EEEE"),
                    upperCase,
                    swapCase));
            break;

          // %b  Abbreviated month name according to locale.
          case 'b':
            buffer.append(
                correctCase(
                    formatter.format("MMM"),
                    upperCase,
                    swapCase));
            break;

          // %B  Full month name according to locale.
          case 'B':
            buffer.append(
                correctCase(
                    formatter.format("MMMM"),
                    upperCase,
                    swapCase));
            break;

          // %c  Preferred date and time representation for locale.
          case 'c':
            // NOTE: en_US locale
            buffer.append(
                formatter.format("EEE dd MMM yyyy hh:mm:ss aa z"));
            break;

          // %C  Year divided by 100 and truncated to integer (00-99).
          case 'C':
            buffer.append(
                formatter.format("y").substring(0, 2));
            break;

          // %d   Day of the month as decimal number (01-31).
          case 'd':
            buffer.append(
                formatter.format("dd"));
            break;

          // %D  Same as "%m/%d/%y"
          case 'D':
            buffer.append(
                formatter.format("MM/dd/yy"));
            break;

          // %e  Day of the month as decimal number, padded with space.
          case 'e':
            buffer.append(
                correctPad(
                    formatter.format("dd"),
                    zeroPad,
                    true,
                    removePad,
                    (padWidthBuffer.length() <= 0
                        ? new StringBuilder("2")
                        : padWidthBuffer)));
            break;

          // %E  Modifier, use a locale-dependent alternative representation.
          case 'E':
            inside = true;
            throw new UnsupportedOperationException("Not implemented yet");
//            break;

          // %F  ISO 8601 date format: "%Y-%m-%d".
          case 'F':
            buffer.append(
                formatter.format("yyyy-MM-dd"));
            break;

          // %g  2-digit year version of %G, (00-99)
          case 'g':
            buffer.append(
                formatter.format("YY"));
            break;

          // %G  ISO 8601 week-based year.
          case 'G':
            buffer.append(
                formatter.format("YYYY"));
            break;

          // %h  Like %b.
          case 'h':
            buffer.append(
                formatter.format("MMM"));
            break;

          // %H  Hour (24-hour clock) as decimal number (00-23).
          case 'H':
            buffer.append(
                formatter.format("HH"));
            break;

          // %I  Hour (12-hour clock) as decimal number (01-12).
          case 'I':
            buffer.append(
                formatter.format("hh"));
            break;

          // %j  Day of the year as decimal number (001-366).
          case 'j':
            buffer.append(
                formatter.format("DDD"));
            break;

          // %k  Hour (24-hour clock) as decimal number (0-23), space padded.
          case 'k':
            buffer.append(
                correctPad(
                    formatter.format("HH"),
                    zeroPad,
                    spacePad,
                    removePad,
                    (padWidthBuffer.length() <= 0
                        ? new StringBuilder("2")
                        : padWidthBuffer)));
            break;

          // %l  Hour (12-hour clock) as decimal number (1-12), space padded.
          case 'l':
            buffer.append(
                correctPad(
                    formatter.format("hh"),
                    zeroPad,
                    spacePad || !zeroPad,
                    removePad,
                    (padWidthBuffer.length() <= 0
                        ? new StringBuilder("2")
                        : padWidthBuffer)));
            break;

          // %m  Month as decimal number (01-12).
          case 'm':
            buffer.append(
                correctPad(
                    formatter.format("MM"),
                    zeroPad,
                    spacePad,
                    removePad,
                    (padWidthBuffer.length() <= 0
                        ? new StringBuilder("2")
                        : padWidthBuffer)));
            break;

          // %M  Minute as decimal number (00-59).
          case 'M':
            buffer.append(
                correctCase(
                    formatter.format("mm"),
                    upperCase,
                    swapCase));
            break;

          // %n  Newline.
          case 'n':
            buffer.append(
                formatter.format("\n"));
            break;

          // %O  Modifier, use alternative numeric symbols (say, Roman numerals).
          case 'O':
            inside = true;
            throw new UnsupportedOperationException("Not implemented yet");
//            break;

          // %p  "AM", "PM", or locale string. Noon = "PM", midnight = "AM".
          case 'p':
            buffer.append(
                correctCase(
                    formatter.format("a"),
                    upperCase,
                    swapCase));
            break;

          // %P  "am", "pm", or locale string. Noon = "pm", midnight = "am".
          case 'P':
            buffer.append(
                correctCase(
                    formatter.format("a").toLowerCase(),
                    upperCase,
                    swapCase));
            break;

          // %r  12-hour clock time.
          case 'r':
            buffer.append(
                formatter.format("hh:mm:ss a"));
            break;

          // %R  24-hour clock time, "%H:%M".
          case 'R':
            buffer.append(
                formatter.format("HH:mm"));
            break;

          // %s  Number of seconds since Epoch, 1970-01-01 00:00:00 +0000 (UTC).
          case 's':
            buffer.append(
                ((Long) (date.getTime() / 1000)).toString());
            break;

          // %S  Second as decimal number (00-60). 60 for leap seconds.
          case 'S':
            buffer.append(
                formatter.format("ss"));
            break;

          // %t  Tab.
          case 't':
            buffer.append(
                formatter.format("\t"));
            break;

          // %T  24-hour time, "%H:%M:%S".
          case 'T':
            buffer.append(
                formatter.format("HH:mm:ss"));
            break;

          // %u  The day of the week as a decimal, (1-7). Monday being 1.
          case 'u':
            buffer.append(
                formatter.format("u"));
            break;

          // %U  week number of the current year as a decimal number, (00-53).
          // Starting with the first Sunday as the first day of week 01.
          case 'U':
            throw new UnsupportedOperationException("Not implemented yet");
            // buffer.append(
            //     formatter.format("ww"));
            // break;

          // %V  ISO 8601 week number (00-53).
          // Week 1 is the first week that has at least 4 days in the new year.
          case 'V':
            buffer.append(
                formatter.format("ww"));
            break;

          // %w  Day of the week as a decimal, (0-6). Sunday being 0.
          case 'w':
            String dayNumberOfWeek = formatter.format("u"); // (1-7)
            buffer.append(
                (dayNumberOfWeek.equals("7") ? "0" : dayNumberOfWeek));
            break;

          // %W  Week number of the current year as a decimal number, (00-53).
          // Starting with the first Monday as the first day of week 01.
          case 'W':
            throw new UnsupportedOperationException("Not implemented yet");
            // buffer.append(
            //     formatter.format("ww"));
            // break;

          // %x  Locale date without time.
          case 'x':
            buffer.append(
                formatter.format("MM/dd/yyyy"));
            break;

          // %X  Locale time without date.
          case 'X':
            buffer.append(
                formatter.format("hh:mm:ss aa"));
            // buffer.append(
            //     formatter.format("HH:mm:ss"));
            break;

          // %y  Year as decimal number without century (00-99).
          case 'y':
            buffer.append(
                formatter.format("yy"));
            break;

          // %Y  Year as decimal number with century.
          case 'Y':
            buffer.append(
                formatter.format("yyyy"));
            break;

          // %z  Numeric timezone as hour and minute offset from UTC "+hhmm" or "-hhmm".
          case 'z':
            buffer.append(
                formatter.format("Z"));
            break;

          // %Z  Timezone, name, or abbreviation.
          case 'Z':
            buffer.append(
                formatter.format("z"));
            break;

          // %%  Literal '%'.
          case '%':
            buffer.append(
                formatter.format("%"));
            break;

          // glibc extension

          // %^  Force upper case.
          case '^':
            inside = true;
            upperCase = true;
            break;

          // %#  Swap case.
          case '#':
            inside = true;
            swapCase = true;
            break;

          // %-  Remove padding.
          case '-':
            inside = true;
            removePad = true;
            break;

          // %_  Space pad.
          case '_':
            inside = true;
            spacePad = true;
            break;

          // %0  Zero pad.
          //  0  Alternatively if preceded by another digit, defines padding width.
          case '0':
            inside = true;
            if (padWidthBuffer.length() == 0) {
              zeroPad = true;
              spacePad = false;
            } else {
              padWidthBuffer.append(c);
            }
            break;

          // %1  Padding width.
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            inside = true;
            // zeroPad = !spacePad; // Default to zero padding.
            padWidthBuffer.append(c);
            break;

          default:
            buffer.append(c.toString());
            break;
        }
      } else {
        buffer.append(c.toString());
      }
    }

    return buffer.toString();
  }

  private static String correctCase(
      String simple,
      Boolean upperCase,
      Boolean swapCase) {
    if (upperCase) {
      return simple.toUpperCase();
    }

    if (!swapCase) {
      return simple;
    }

    // swap case
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < simple.length(); i++) {
      Character c = simple.charAt(i);
      buffer.append(
          (Character.isLowerCase(c)
              ? Character.toUpperCase(c)
              : Character.toLowerCase(c))
          );
    }

    return buffer.toString();
  }

  private static String correctPad(
      String simple,
      Boolean zeroPad,
      Boolean spacePad,
      Boolean removePad,
      StringBuilder padWidthBuffer) {
    String unpadded = simple.replaceFirst("^(0+| +)(?!$)", "");

    if (removePad) {
      return unpadded;
    }

    int padWidth = 0;
    if (padWidthBuffer.length() > 0) {
      padWidth = (
          Integer.parseInt(padWidthBuffer.toString()) - unpadded.length());
    }

    if (spacePad || zeroPad) {
      StringBuilder buffer = new StringBuilder();
      char padChar = (spacePad ? ' ' : '0');
      for (int i = 0 ; i < padWidth ; i++) {
        buffer.append(padChar);
      }
      buffer.append(unpadded);
      return buffer.toString();
    }

    return simple;
  }
}
