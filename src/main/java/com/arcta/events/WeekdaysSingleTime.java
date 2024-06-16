package com.arcta.events;

import java.util.Map;
import java.util.regex.Matcher;

import static com.arcta.events.Util.map;
import static com.arcta.events.Weekdays.WEEKDAYS_STANDARD_ENG;
import static com.arcta.events.M_Static.*;
import static com.arcta.events.M_Weekday.M_WEEKDAY_ENG;

class WeekdaysSingleTime extends DateMatcher.DayTimeMatcher {
    Map<String, Time> match(String text) {
        Map<String, Time> weekdayTimes = map();
        String day_day_time = M_WEEKDAY_ENG + "[s]?" + "[,]?" + SPACES + "(at)?" + SPACESO + "([0-9]{1,2})[:|\\.]([0-5])(0|5)" + SPACESO + "(am|pm)?";
        Matcher matcher = Util.matcher(day_day_time, text);
        while (matcher.find()) {
            String day_of_week = WEEKDAYS_STANDARD_ENG.getKey(matcher.group(1));
            if (Util.empty(day_of_week)) continue;
            Time time = new Time(Util.cleanInt(matcher.group(3)), matcher.group(4).trim() + matcher.group(5), matcher.group(6));
            if (Integer.parseInt(time.getHour()) > 24) continue;
            time.convertTo24H();
            if (Integer.parseInt(time.getHour()) < 12) continue;
            weekdayTimes.putIfAbsent(day_of_week, time);
        }
        return weekdayTimes;
    }
}          //saturday(s) 7.30(pm)
