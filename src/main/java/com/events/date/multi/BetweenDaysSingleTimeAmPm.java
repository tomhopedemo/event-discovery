package com.events.date.multi;

import com.events.DateMatcher;
import com.events.date.Time;

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
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;

public class BetweenDaysSingleTimeAmPm extends DateMatcher.DayTimeMatcher {
    public Map<String, Time> match(String text) {
        Map<String, Time> weekdayTimes = new HashMap<>();
        String day_day_time = M_WEEKDAY_ENG + SPACESO + M_HYPHENS_TO + SPACESO + M_WEEKDAY_ENG + "[,]?" + SPACES + "(eves|at)?" + SPACESO + "([0-9]{1,2})(am|pm)";
        Matcher matcher = matcher(day_day_time, text);
        while (matcher.find()) {
            List<Integer> weekdayIndices = weekdayBetweenIndices(WEEKDAYS_STANDARD_ENG.getKey(matcher.group(1)), WEEKDAYS_STANDARD_ENG.getKey(matcher.group(3)));
            if (weekdayIndices == null) continue;
            Time time = new Time(valueOf(Integer.valueOf(matcher.group(5).trim())), "00", matcher.group(6));
            if (parseInt(time.getHour()) > 24 || parseInt(time.getHour()) == 0) continue;
            time.convertTo24H();
            if (parseInt(time.getHour()) < 12) continue;
            for (Integer weekdayIndex : weekdayIndices) {
                weekdayTimes.putIfAbsent(WEEKDAYS_ORDER.get(weekdayIndex), time);
            }
        }
        return weekdayTimes;
    }
} //sat - sun 7pm