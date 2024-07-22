package com.events.date;

import com.events.DateMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.events.date.HyphenMatchers.M_HYPHENS_TO;
import static com.events.date.DateUtils.matcher;
import static com.events.date.M_Static.SPACES;
import static com.events.date.M_Static.SPACESO;
import static com.events.date.M_Weekday.M_WEEKDAY_ENG;
import static com.events.date.Weekdays.WEEKDAYS_ORDER;
import static com.events.date.Weekdays.WEEKDAYS_STANDARD_ENG;

public class BetweenDaysSingleTime extends DateMatcher.DayTimeMatcher {
    public Map<String, Time> match(String text) {
        Map<String, Time> weekdayTimes = new HashMap<>();
        String dayDayTime = M_WEEKDAY_ENG + SPACESO + M_HYPHENS_TO + SPACESO + M_WEEKDAY_ENG + "[,]?" + SPACES + "(eves|at|@)?" + SPACESO + "([0-9]{1,2})[:|\\.]([0-5])(0|5)" + SPACESO + "(am|pm)?";
        Matcher matcher = matcher(dayDayTime, text);
        while (matcher.find()) {
            List<Integer> weekdayIndices = weekdayBetweenIndices(WEEKDAYS_STANDARD_ENG.getKey(matcher.group(1)), WEEKDAYS_STANDARD_ENG.getKey(matcher.group(3)));
            if (weekdayIndices == null) continue;
            Time time = new Time(String.valueOf(Integer.valueOf(matcher.group(5).trim())), matcher.group(6).trim() + matcher.group(7), matcher.group(8));
            if (Integer.parseInt(time.getHour()) > 24) continue;
            time.convertTo24H();
            if (Integer.parseInt(time.getHour()) < 12) continue;
            for (Integer weekdayIndex : weekdayIndices) {
                weekdayTimes.putIfAbsent(WEEKDAYS_ORDER.get(weekdayIndex), time);
            }
        }
        return weekdayTimes;
    }
}     //sat - sun 7.30(pm)