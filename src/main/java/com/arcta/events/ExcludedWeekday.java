package com.arcta.events;
import java.util.regex.Matcher;
import static com.arcta.events.Weekdays.WEEKDAYS_STANDARD_ENG;
import static com.arcta.events.M_Weekday.M_WEEKDAY_ENG;
class ExcludedWeekday extends DateMatcher {
    public DateMeta matchInternal(String text) { Matcher matcher = Util.matcher("no shows on " + M_WEEKDAY_ENG + "[s]?", text);
        DateMeta dateMeta = new DateMeta();
        while (matcher.find()) { Calendar.Date date = new Calendar.Date();
            date.dayOfWeek = WEEKDAYS_STANDARD_ENG.getKey(matcher.group(1));
            dateMeta.dateList.add(date);
            dateMeta.indexPairsInclExcl.add(new Util.Multi<>(matcher.start(), matcher.end()));
            return dateMeta;}
        return dateMeta;}}