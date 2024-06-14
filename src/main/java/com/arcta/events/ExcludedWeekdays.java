package com.arcta.events;
import java.util.regex.Matcher;
import static com.arcta.events.Weekdays.WEEKDAYS_STANDARD_ENG;
import static com.arcta.events.M_Weekday.M_WEEKDAY_ENG;
class ExcludedWeekdays extends DateMatcher {
    public DateMeta matchInternal(String text) { String regex = "no shows on " + M_WEEKDAY_ENG + "[s]?" + " or " + M_WEEKDAY_ENG + "[s]?";
        Matcher matcher = Util.matcher(regex, text);
        DateMeta date_internal = new DateMeta();
        while (matcher.find()) { Calendar.Date date = new Calendar.Date();
            Calendar.Date date2 = new Calendar.Date();
            date.dayOfWeek = WEEKDAYS_STANDARD_ENG.getKey(matcher.group(1));
            date2.dayOfWeek = WEEKDAYS_STANDARD_ENG.getKey(matcher.group(2));
            date_internal.dateList.add(date);
            date_internal.dateList.add(date2);
            date_internal.indexPairsInclExcl.add(new Util.Multi<>(matcher.start(), matcher.end()));
            return date_internal;}
        return date_internal;}}