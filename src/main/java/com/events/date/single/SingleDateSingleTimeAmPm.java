package com.events.date.single;

import com.events.Util;
import com.events.date.Calendar;
import com.events.date.DateTimeMatcher;
import com.events.date.Time;

import java.util.regex.Matcher;

import static com.events.date.HyphenMatchers.M_HYPHENSO;
import static com.events.date.M_Month.M_MONTH_ENG;
import static com.events.date.M_Static.*;
import static com.events.date.M_Weekday.M_WEEKDAYO_ENG;
import static com.events.date.M_Weekday.M_WEEKDAYO_SPACE_AFTER_ENG;
import static com.events.date.Months.MONTHS_STANDARD_ENG;

public class SingleDateSingleTimeAmPm extends DateTimeMatcher {
    public Util.MultiList<Calendar.Date, Time> matchInternal(String text) {
        Util.MultiList<Calendar.Date, Time> dateTimes = new Util.MultiList<>();
        String dayMonthYear = M_WEEKDAYO_SPACE_AFTER_ENG + NEGATIVE_LOOKBEHIND_DIGITS + M_DAY_ORDINALO + "[/]?" + MWO + M_MONTH_ENG + "(\\. )?" + MWO + M_WEEKDAYO_ENG + "(, )?" + MWO + M_YEARO + MWO + "\\s*" + "[,]?" + M_HYPHENSO + "\\s*" + "([0-9]{1,2})[ ]?(am|pm)";
        Matcher matcher = Util.matcher(dayMonthYear, text);
        while (matcher.find()) {
            Calendar.Date date = new Calendar.Date();
            date.dateDay = String.valueOf(Integer.valueOf(matcher.group(2)));
            date.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(4));
            date.dateYear = "20" + (matcher.group(9) == null ? Calendar.defaultYearAbbrev(date.dateMonth) : matcher.group(9));
            Time time = new Time();
            time.setHour(String.valueOf(Integer.valueOf(matcher.group(11).trim())));
            if (Integer.parseInt(time.getHour()) > 24) continue;
            time.amPm = matcher.group(12);
            time.timeMinute = "00";
            time.provenance = this.getClass().getSimpleName();
            time.convertTo24H();
            dateTimes.add(new Util.Multi<>(date, time));
        }
        return dateTimes;
    }
}   //(sat) 3(rd) jan (sat) (2020) 7pm