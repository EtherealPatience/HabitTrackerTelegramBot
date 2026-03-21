package org.example.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static boolean isValidTime(String time) {
        return time != null && time.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]");
    }

    public static String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static int compareTimes(String time1, String time2) {
        if (time1 == null || time2 == null) return 0;
        LocalTime t1 = LocalTime.parse(time1);
        LocalTime t2 = LocalTime.parse(time2);
        return t1.compareTo(t2);
    }

    public static String formatTime(String time) {
        if (time == null || time.isEmpty()) return "не указано";
        return time;
    }
}