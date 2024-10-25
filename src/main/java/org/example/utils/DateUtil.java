package org.example.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {
    public static String convertDateToUTC(Date date) {
        Instant instant = date.toInstant();
        ZonedDateTime utcDateTime = instant.atZone(ZoneId.of("UTC"));
        return utcDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"));
    }
}
