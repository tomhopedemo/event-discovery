package com.arcta.events;
import java.util.List;
import static com.arcta.events.Util.list;
class Weekdays {
    static final Util.MapList<String,String> WEEKDAYS_STANDARD_ENG; static final Util.MapList<String,String> WEEKDAYS_STANDARD_DEU;
    static final List<String> WEEKDAYS_ORDER = list("mon", "tue", "wed", "thu", "fri", "sat", "sun");
    static final List<String> WEEKDAYS_LONG = list("monday", "tuesday", "wednesday", "thursday","friday", "saturday", "sunday");
    static { WEEKDAYS_STANDARD_ENG = new Util.MapList<>(); WEEKDAYS_STANDARD_ENG.addAll("mon", list("mon", "monday")); WEEKDAYS_STANDARD_ENG.addAll("tue", list("tue", "tuesday", "tues")); WEEKDAYS_STANDARD_ENG.addAll("wed", list("wed", "wednesday")); WEEKDAYS_STANDARD_ENG.addAll("thu", list("thu", "thursday", "thur")); WEEKDAYS_STANDARD_ENG.addAll("fri", list("fri", "friday"));WEEKDAYS_STANDARD_ENG.addAll("sat", list("sat", "saturday")); WEEKDAYS_STANDARD_ENG.addAll("sun", list("sun", "sunday"));
        WEEKDAYS_STANDARD_DEU = new Util.MapList<>(); WEEKDAYS_STANDARD_DEU.addAll("mon", list("montag", "mo"));WEEKDAYS_STANDARD_DEU.addAll("tue", list("dienstag", "di")); WEEKDAYS_STANDARD_DEU.addAll("wed", list("mittwoch", "mi"));WEEKDAYS_STANDARD_DEU.addAll("thu", list("donnerstag", "do")); WEEKDAYS_STANDARD_DEU.addAll("fri", list("freitag", "fr"));WEEKDAYS_STANDARD_DEU.addAll("sat", list("samstag", "sa")); WEEKDAYS_STANDARD_DEU.addAll("sun", list("sonntag", "so"));}}