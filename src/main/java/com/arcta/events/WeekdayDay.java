package com.arcta.events;
import java.util.regex.Matcher;
import static com.arcta.events.M_Lang.*;
import static com.arcta.events.M_Static.*;
import static com.arcta.events.Util.multilist;
import static com.arcta.events.Util.opt;
class WeekdayDay extends DateMatcher {
    Util.Lang language; WeekdayDay(Util.Lang language) {
        this.language = language;
    }
    public DateMeta matchInternal(String text) { Util.MapList<String, String> standardWeekdays = LANG_WEEKDAYS_STANDARD.get(language);
        String regex = "(^| )" + LANG_M_WEEKDAY.get(language) + "(,)?" + SPACES + M_DAY + opt(LANG_M_ORDINAL.get(language)) + "(?!\\.\\d|\\:\\d)";
        Matcher matcher = Util.matcher(regex, text);
        DateMeta meta = new DateMeta();
        while (matcher.find()) { Calendar.Date date = new Calendar.Date();
            date.dayOfWeek = standardWeekdays.getKey(matcher.group(2));
            date.dateDay = Integer.valueOf(matcher.group(4)).toString(); if (Integer.parseInt(date.dateDay) < 1) continue;
            Util.Multi startEnd = new Util.Multi<>(matcher.start(), matcher.end());
            date.indexPairs = multilist(startEnd);
            date.note = this.getClass().getSimpleName();
            meta.dateList.add(date);
            meta.indexPairsInclExcl.add(startEnd);} return meta;}}