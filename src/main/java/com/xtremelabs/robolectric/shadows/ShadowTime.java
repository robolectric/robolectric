package com.xtremelabs.robolectric.shadows;


import android.text.format.Time;
import android.util.TimeFormatException;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Implements(Time.class)
public class ShadowTime {
    @RealObject
    private Time time;

    private static final long SECOND_IN_MILLIS = 1000;
    private static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    private static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    private static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

    public void __constructor__() {
        __constructor__(getCurrentTimezone());
    }

    public void __constructor__(String timezone) {
        if (timezone == null) {
            throw new NullPointerException("timezone is null!");
        }
        time.timezone = timezone;
        time.year = 1970;
        time.monthDay = 1;
        time.isDst = -1;
    }

    public void __constructor__(Time other) {
        set(other);
    }

    @Implementation
    public void set(Time other) {
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

    @Implementation
    public void setToNow() {
        set(System.currentTimeMillis());
    }


    @Implementation
    public static boolean isEpoch(Time time) {
        long millis = time.toMillis(true);
        return getJulianDay(millis, 0) == Time.EPOCH_JULIAN_DAY;
    }


    @Implementation
    public static int getJulianDay(long millis, long gmtoff) {
        long offsetMillis = gmtoff * 1000;
        long julianDay = (millis + offsetMillis) / DAY_IN_MILLIS;
        return (int) julianDay + Time.EPOCH_JULIAN_DAY;
    }

    @Implementation
    public long setJulianDay(int julianDay) {
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

    @Implementation
    public void set(long millis) {
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

    @Implementation
    public long toMillis(boolean ignoreDst) {
        Calendar c = getCalendar();
        return c.getTimeInMillis();
    }

    @Implementation
    public void set(int second, int minute, int hour, int monthDay, int month, int year) {
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

    @Implementation
    public void set(int monthDay, int month, int year) {
        set(0, 0, 0, monthDay, month, year);
    }

    @Implementation
    public void clear(String timezone) {
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

    @Implementation
    public static String getCurrentTimezone() {
        return TimeZone.getDefault().getID();
    }

    @Implementation
    public static int compare(Time a, Time b) {
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

    @Implementation
    public boolean before(Time other) {
        return Time.compare(time, other) < 0;
    }

    @Implementation
    public boolean after(Time other) {
        return Time.compare(time, other) > 0;
    }

    @Implementation
    public boolean parse(String timeString) {
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
                throwTimeFormatException();
            }
        }
        return "UTC".equals(tz.getID());
    }

    @Implementation
    public String format(String format) {
        Strftime strftime = new Strftime(format, Locale.getDefault());
        strftime.setTimeZone(TimeZone.getTimeZone(time.timezone));
        return strftime.format(new Date(toMillis(false)));
    }

    @Implementation
    public String format2445() {
        return format("%Y%m%dT%H%M%S");
    }

    @Implementation
    public String format3339(boolean allDay) {
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

    private void throwTimeFormatException() {
        try {
            Constructor<TimeFormatException> c = TimeFormatException.class.getDeclaredConstructor();
            c.setAccessible(true);
            throw c.newInstance();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Calendar getCalendar() {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(time.timezone));
        c.set(time.year, time.month, time.monthDay, time.hour, time.minute, time.second);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    // taken from org.apache.catalina.util.Strftime.java
    // see http://javasourcecode.org/html/open-source/tomcat/tomcat-6.0.32/org/apache/catalina/util/Strftime.java.html
    /*
    * Licensed to the Apache Software Foundation (ASF) under one or more
    * contributor license agreements.  See the NOTICE file distributed with
    * this work for additional information regarding copyright ownership.
    * The ASF licenses this file to You under the Apache License, Version 2.0
    * (the "License"); you may not use this file except in compliance with
    * the License.  You may obtain a copy of the License at
    *
    *      http://www.apache.org/licenses/LICENSE-2.0
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    */
    public static class Strftime {
        protected static Properties translate;
        protected SimpleDateFormat simpleDateFormat;

        /**
         * Initialize our pattern translation
         */
        static {
            translate = new Properties();
            translate.put("a", "EEE");
            translate.put("A", "EEEE");
            translate.put("b", "MMM");
            translate.put("B", "MMMM");
            translate.put("c", "EEE MMM d HH:mm:ss yyyy");

            //There's no way to specify the century in SimpleDateFormat.  We don't want to hard-code
            //20 since this could be wrong for the pre-2000 files.
            //translate.put("C", "20");
            translate.put("d", "dd");
            translate.put("D", "MM/dd/yy");
            translate.put("e", "dd"); //will show as '03' instead of ' 3'
            translate.put("F", "yyyy-MM-dd");
            translate.put("g", "yy");
            translate.put("G", "yyyy");
            translate.put("H", "HH");
            translate.put("h", "MMM");
            translate.put("I", "hh");
            translate.put("j", "DDD");
            translate.put("k", "HH"); //will show as '07' instead of ' 7'
            translate.put("l", "hh"); //will show as '07' instead of ' 7'
            translate.put("m", "MM");
            translate.put("M", "mm");
            translate.put("n", "\n");
            translate.put("p", "a");
            translate.put("P", "a");  //will show as pm instead of PM
            translate.put("r", "hh:mm:ss a");
            translate.put("R", "HH:mm");
            //There's no way to specify this with SimpleDateFormat
            //translate.put("s","seconds since ecpoch");
            translate.put("S", "ss");
            translate.put("t", "\t");
            translate.put("T", "HH:mm:ss");
            //There's no way to specify this with SimpleDateFormat
            //translate.put("u","day of week ( 1-7 )");

            //There's no way to specify this with SimpleDateFormat
            //translate.put("U","week in year with first sunday as first day...");

            translate.put("V", "ww"); //I'm not sure this is always exactly the same

            //There's no way to specify this with SimpleDateFormat
            //translate.put("W","week in year with first monday as first day...");

            //There's no way to specify this with SimpleDateFormat
            //translate.put("w","E");
            translate.put("X", "HH:mm:ss");
            translate.put("x", "MM/dd/yy");
            translate.put("y", "yy");
            translate.put("Y", "yyyy");
            translate.put("Z", "z");
            translate.put("z", "Z");
            translate.put("%", "%");
        }


        /**
         * Create an instance of this date formatting class
         *
         * @see #Strftime(String, Locale)
         */
        public Strftime(String origFormat) {
            String convertedFormat = convertDateFormat(origFormat);
            simpleDateFormat = new SimpleDateFormat(convertedFormat);
        }

        /**
         * Create an instance of this date formatting class
         *
         * @param origFormat the strftime-style formatting string
         * @param locale     the locale to use for locale-specific conversions
         */
        public Strftime(String origFormat, Locale locale) {
            String convertedFormat = convertDateFormat(origFormat);
            simpleDateFormat = new SimpleDateFormat(convertedFormat, locale);
        }

        /**
         * Format the date according to the strftime-style string given in the constructor.
         *
         * @param date the date to format
         * @return the formatted date
         */
        public String format(Date date) {
            return simpleDateFormat.format(date);
        }

        /**
         * Get the timezone used for formatting conversions
         *
         * @return the timezone
         */
        public TimeZone getTimeZone() {
            return simpleDateFormat.getTimeZone();
        }

        /**
         * Change the timezone used to format dates
         *
         * @see SimpleDateFormat#setTimeZone
         */
        public void setTimeZone(TimeZone timeZone) {
            simpleDateFormat.setTimeZone(timeZone);
        }

        /**
         * Search the provided pattern and get the C standard
         * Date/Time formatting rules and convert them to the
         * Java equivalent.
         *
         * @param pattern The pattern to search
         * @return The modified pattern
         */
        protected String convertDateFormat(String pattern) {
            boolean inside = false;
            boolean mark = false;
            boolean modifiedCommand = false;

            StringBuffer buf = new StringBuffer();

            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);

                if (c == '%' && !mark) {
                    mark = true;
                } else {
                    if (mark) {
                        if (modifiedCommand) {
                            //don't do anything--we just wanted to skip a char
                            modifiedCommand = false;
                            mark = false;
                        } else {
                            inside = translateCommand(buf, pattern, i, inside);
                            //It's a modifier code
                            if (c == 'O' || c == 'E') {
                                modifiedCommand = true;
                            } else {
                                mark = false;
                            }
                        }
                    } else {
                        if (!inside && c != ' ') {
                            //We start a literal, which we need to quote
                            buf.append("'");
                            inside = true;
                        }

                        buf.append(c);
                    }
                }
            }

            if (buf.length() > 0) {
                char lastChar = buf.charAt(buf.length() - 1);

                if (lastChar != '\'' && inside) {
                    buf.append('\'');
                }
            }
            return buf.toString();
        }

        protected String quote(String str, boolean insideQuotes) {
            String retVal = str;
            if (!insideQuotes) {
                retVal = '\'' + retVal + '\'';
            }
            return retVal;
        }

        /**
         * Try to get the Java Date/Time formatting associated with
         * the C standard provided.
         *
         * @param buf       The buffer
         * @param pattern   The date/time pattern
         * @param index     The char index
         * @param oldInside Flag value
         * @return True if new is inside buffer
         */
        protected boolean translateCommand(StringBuffer buf, String pattern, int index, boolean oldInside) {
            char firstChar = pattern.charAt(index);
            boolean newInside = oldInside;

            //O and E are modifiers, they mean to present an alternative representation of the next char
            //we just handle the next char as if the O or E wasn't there
            if (firstChar == 'O' || firstChar == 'E') {
                if (index + 1 < pattern.length()) {
                    newInside = translateCommand(buf, pattern, index + 1, oldInside);
                } else {
                    buf.append(quote("%" + firstChar, oldInside));
                }
            } else {
                String command = translate.getProperty(String.valueOf(firstChar));

                //If we don't find a format, treat it as a literal--That's what apache does
                if (command == null) {
                    buf.append(quote("%" + firstChar, oldInside));
                } else {
                    //If we were inside quotes, close the quotes
                    if (oldInside) {
                        buf.append('\'');
                    }
                    buf.append(command);
                    newInside = false;
                }
            }
            return newInside;
        }
    }
}
