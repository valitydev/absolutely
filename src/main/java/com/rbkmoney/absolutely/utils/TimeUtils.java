package com.rbkmoney.absolutely.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static OffsetDateTime toOffsetDateTime(String time) {
        return OffsetDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
    }
}
