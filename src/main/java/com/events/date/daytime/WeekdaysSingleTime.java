package com.events.date.daytime;

import com.events.DateMatcher;
import com.events.Util;
import com.events.date.Time;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static com.events.date.Weekdays.WEEKDAYS_STANDARD_ENG;
import static com.events.date.M_Static.*;
import static com.events.date.M_Weekday.M_WEEKDAY_ENG;

public class WeekdaysSingleTime extends DateMatcher.DayTimeMatcher {
    public Map<String, Time> match(String text) {
        Map<String, Time> weekdayTimes = new HashMap<>();
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
