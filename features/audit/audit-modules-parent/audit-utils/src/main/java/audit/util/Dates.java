/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package audit.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static java.time.ZoneOffset.UTC;
import static java.time.ZoneOffset.ofTotalSeconds;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.util.Calendar.MILLISECOND;
import static java.util.Locale.ROOT;
import static java.util.TimeZone.getTimeZone;
import static javax.xml.datatype.DatatypeFactory.newInstance;

public final class Dates {
    private static final String CALENDAR_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS XX";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss:SSSX";
    private static final String CALENDAR_FORMAT_JAVA8 = "yyyy'/'MM'/'dd HH:mm:ss:SSS XX";

    private Dates() {
        throw new IllegalStateException("no instantiable constructor " + Dates.class.getName());
    }

    public static String format(Calendar c) {
        DateFormat format = new SimpleDateFormat(CALENDAR_FORMAT, ROOT);
        format.setTimeZone(c.getTimeZone());
        return format.format(c.getTime());
    }

    public static String format(Date d) {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, ROOT);
        format.setTimeZone(getTimeZone(UTC));
        return format.format(d);
    }

    public static Calendar format(String formatted) {
        TimeZone utc = getTimeZone(UTC);
        DateTimeFormatter formatter = ofPattern(CALENDAR_FORMAT_JAVA8);

        TemporalAccessor temporalAccessor = formatter.parse(formatted);
        int year = temporalAccessor.get(YEAR);
        int month = temporalAccessor.get(MONTH_OF_YEAR);
        int day = temporalAccessor.get(DAY_OF_MONTH);
        int hour = temporalAccessor.get(HOUR_OF_DAY);
        int minute = temporalAccessor.get(MINUTE_OF_HOUR);
        int second = temporalAccessor.get(SECOND_OF_MINUTE);
        int millis = temporalAccessor.get(MILLI_OF_SECOND);
        int timeZoneInSeconds = temporalAccessor.get(OFFSET_SECONDS);

        GregorianCalendar c = new GregorianCalendar(year, month - 1, day, hour, minute, second);
        c.set(MILLISECOND, millis);

        if (timeZoneInSeconds == utc.getRawOffset()) {
            c.setTimeZone(utc);
        } else {
            ZoneOffset zoneOffset = ofTotalSeconds(timeZoneInSeconds);
            SimpleTimeZone zone = new SimpleTimeZone(timeZoneInSeconds * 1000, zoneOffset.getId());
            c.setTimeZone(zone);
        }
        return c;
    }

    public static String format(XMLGregorianCalendar calendar) {
        return format(toCalendar(calendar));
    }

    public static Calendar toCalendar(XMLGregorianCalendar xmlCalendar) {
        TimeZone utc = getTimeZone(UTC);
        TimeZone timeZone = xmlCalendar.getTimeZone(utc.getRawOffset());
        if (!timeZone.hasSameRules(utc)) {
            throw new IllegalStateException(String.format("Declaring TimeZone %s is not according to XML standard"
                    + " in xsd:dateTime and UTC must be used.", timeZone.getDisplayName()));
        }
        try {
            int utcOffset = utc.getRawOffset();
            XMLGregorianCalendar def = newInstance().newXMLGregorianCalendar(1, 1, 1, 0, 0, 0, 0, utcOffset);
            return xmlCalendar.toGregorianCalendar(utc, ROOT, def);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(Calendar calendar) {
        calendar = (Calendar) calendar.clone();
        TimeZone currentTimeZone = calendar.getTimeZone();
        int offset = currentTimeZone.getRawOffset();
        if (currentTimeZone.useDaylightTime() && currentTimeZone.inDaylightTime(calendar.getTime())) {
            offset += currentTimeZone.getDSTSavings();
        }

        int year         = calendar.get(Calendar.YEAR);
        int month        = calendar.get(Calendar.MONTH);
        int dayOfMonth   = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay    = calendar.get(Calendar.HOUR_OF_DAY);
        int minute       = calendar.get(Calendar.MINUTE);
        int second       = calendar.get(Calendar.SECOND);
        int milliseconds = calendar.get(MILLISECOND);
        calendar.add(MILLISECOND, -1 * offset);

        TimeZone utc = getTimeZone(UTC);

        try {
            return newInstance()
                    .newXMLGregorianCalendar(year, 1 + month, dayOfMonth,
                            hourOfDay, minute, second, milliseconds, utc.getRawOffset());

        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }
}
