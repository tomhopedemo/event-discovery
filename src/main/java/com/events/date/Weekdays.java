package com.events.date;

import com.events.Util.MapList;

import java.util.List;

import static com.events.Util.list;

public class Weekdays {
    public static final MapList<String, String> WEEKDAYS_STANDARD_ENG;
    public static final MapList<String, String> WEEKDAYS_STANDARD_DEU;
    public static final List<String> WEEKDAYS_ORDER = list("mon", "tue", "wed", "thu", "fri", "sat", "sun");
    public static final List<String> WEEKDAYS_LONG = list("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday");

    static {
        WEEKDAYS_STANDARD_ENG = new MapList<>();
        WEEKDAYS_STANDARD_ENG.addAll("mon", list("mon", "monday"));
        WEEKDAYS_STANDARD_ENG.addAll("tue", list("tue", "tuesday", "tues"));
        WEEKDAYS_STANDARD_ENG.addAll("wed", list("wed", "wednesday"));
        WEEKDAYS_STANDARD_ENG.addAll("thu", list("thu", "thursday", "thur"));
        WEEKDAYS_STANDARD_ENG.addAll("fri", list("fri", "friday"));
        WEEKDAYS_STANDARD_ENG.addAll("sat", list("sat", "saturday"));
        WEEKDAYS_STANDARD_ENG.addAll("sun", list("sun", "sunday"));
        WEEKDAYS_STANDARD_DEU = new MapList<>();
        WEEKDAYS_STANDARD_DEU.addAll("mon", list("montag", "mo"));
        WEEKDAYS_STANDARD_DEU.addAll("tue", list("dienstag", "di"));
        WEEKDAYS_STANDARD_DEU.addAll("wed", list("mittwoch", "mi"));
        WEEKDAYS_STANDARD_DEU.addAll("thu", list("donnerstag", "do"));
        WEEKDAYS_STANDARD_DEU.addAll("fri", list("freitag", "fr"));
        WEEKDAYS_STANDARD_DEU.addAll("sat", list("samstag", "sa"));
        WEEKDAYS_STANDARD_DEU.addAll("sun", list("sonntag", "so"));
    }
}