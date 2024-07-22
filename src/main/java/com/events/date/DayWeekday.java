package com.events.date;

import com.events.DateMatcher;
import com.events.Util;

import java.util.regex.Matcher;

import static com.events.date.M_Lang.LANG_M_WEEKDAY;
import static com.events.date.M_Lang.LANG_WEEKDAYS_STANDARD;
import static com.events.date.M_Static.M_DAY;

public class DayWeekday extends DateMatcher {
    Util.Lang language;

    public DayWeekday(Util.Lang language) {
        this.language = language;
    }

    public DateMeta matchInternal(String text) {
        Util.MapList<String, String> weekdaysStandard = LANG_WEEKDAYS_STANDARD.get(language);
        String m__weekday = LANG_M_WEEKDAY.get(language);
        String regex = "(^| )" + M_DAY + "\\s+" + m__weekday;
        Matcher matcher = Util.matcher(regex, text);
        DateMeta meta = new DateMeta();
        while (matcher.find()) {
            Calendar.Date date = new Calendar.Date();
            date.dayOfWeek = weekdaysStandard.getKey(matcher.group(3));
            date.dateDay = Integer.valueOf(matcher.group(2)).toString();
            if (Integer.parseInt(date.dateDay) < 1) continue;
            Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
            Util.Multi<Integer, Integer> multi = new Util.Multi<>(matcher.start(), matcher.end());
            indexPairsToRemove.add(multi);
            date.indexPairs = indexPairsToRemove;
            date.note = this.getClass().getSimpleName();
            meta.dateList.add(date);
            meta.indexPairsInclExcl.add(multi);
        }
        return meta;
    }
}