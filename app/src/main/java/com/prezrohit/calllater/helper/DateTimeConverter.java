package com.prezrohit.calllater.helper;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateTimeConverter {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    public static String convertTo12Hour(int hour, int minute) {
        String timeSet;

        if (hour > 12) {
            hour -= 12;
            timeSet = "PM";

        } else if (hour == 0) {
            hour += 12;
            timeSet = "AM";

        } else if (hour == 12) {
            timeSet = "PM";

        } else {
            timeSet = "AM";
        }

        String min;
        if (minute < 10)
            min = "0" + minute;
        else
            min = String.valueOf(minute);

        return String.valueOf(hour) + ':' + min + " " + timeSet;
    }
}
