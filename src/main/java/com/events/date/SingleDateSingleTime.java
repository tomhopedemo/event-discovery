package com.events.date;

import com.events.Util;

import java.util.regex.Matcher;

import static com.events.date.HyphenMatchers.M_HYPHENSO;
import static com.events.date.M_Month.M_MONTH_ENG;
import static com.events.date.M_Static.*;
import static com.events.date.M_Weekday.M_WEEKDAYO_ENG;
import static com.events.date.M_Weekday.M_WEEKDAYO_SPACE_AFTER_ENG;
import static com.events.date.Months.MONTHS_STANDARD_ENG;

class SingleDateSingleTime extends DateTimeMatcher {
    boolean pm;
    boolean doors;

    SingleDateSingleTime(boolean pm, boolean doors) {
        this.pm = pm;
        this.doors = doors;
    }

    Util.MultiList<Calendar.Date, Time> matchInternal(String text) {
        Util.MultiList<Calendar.Date, Time> dateTimes = new Util.MultiList<>();
        String doorsOptional = "(\\s*doors:)?";
        String dayMonthYear = M_WEEKDAYO_SPACE_AFTER_ENG + NEGATIVE_LOOKBEHIND_DIGITS + M_DAY_ORDINALO + "[/]?" + MWO + M_MONTH_ENG + "(\\. )?" + MWO + M_WEEKDAYO_ENG + "(, )?" + MWO + M_YEARO + MWO + "\\s*" + "[,]?" + M_HYPHENSO + doorsOptional + "\\s*" + "([0-9]{1,2})[:|\\.]([0-5])(0|5)[ ]?(am|pm)?";
        Matcher matcher = Util.matcher(dayMonthYear, text);
        while (matcher.find()) {
            Calendar.Date date = new Calendar.Date();
            date.dateDay = String.valueOf(Integer.valueOf(matcher.group(2)));
            date.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(4));
            date.dateYear = "20" + (matcher.group(9) == null ? Calendar.defaultYearAbbrev(date.dateMonth) : matcher.group(9));
            Time time = new Time();
            Integer hourInt = Integer.valueOf(matcher.group(12).trim());
            if (doors && !Util.empty(matcher.group(11))) {
                hourInt = hourInt + 1;
            }
            time.setHour(String.valueOf(hourInt));
            if (Integer.parseInt(time.getHour()) > 24) continue;
            time.timeMinute = matcher.group(13).trim() + matcher.group(14);
            time.amPm = matcher.group(15);
            time.provenance = this.getClass().getSimpleName();
            if (pm) time.amPm = "pm";
            time.convertTo24H();
            dateTimes.add(new Util.Multi<>(date, time));
        }
        return dateTimes;
    }
}       //(sat) 3(rd) jan (sat) (2020) 7:30(pm)