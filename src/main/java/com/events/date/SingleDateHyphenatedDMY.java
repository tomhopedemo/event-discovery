package com.events.date;

import com.events.Util;

import java.util.List;
import java.util.regex.Matcher;

import static com.events.date.HyphenMatchers.M_HYPHENS_UNDERSCORES_DOTS;
import static com.events.date.M_Static.MONTHS_ORDER;
import static com.events.date.M_Static.M_YEAR;
import static com.events.Util.list;

class SingleDateHyphenatedDMY extends DateMatcher {
    public DateMeta matchInternal(String text) {
        List<Calendar.Date> dates = list();
        String regex = "([0-9]{2})" + M_HYPHENS_UNDERSCORES_DOTS + "([0-9]{2})" + "\\2" + M_YEAR;
        Matcher matcher = Util.matcher(regex, text);
        DateMeta date_internal = new DateMeta();
        while (matcher.find()) {
            getIndexPairs(text, date_internal, matcher);
            Calendar.Date date = new Calendar.Date();
            Integer date_int = Integer.valueOf(matcher.group(1));
            if (date_int > 31) continue;
            date.dateDay = String.valueOf(date_int);
            Integer month_integer = Integer.valueOf(matcher.group(3));
            if (month_integer > 12 || month_integer <= 0) continue;
            date.dateMonth = MONTHS_ORDER.get(month_integer - 1);
            date.dateYear = "20" + matcher.group(5);
            Util.MultiList<Integer, Integer> index_pairs_to_remove = new Util.MultiList<>();
            index_pairs_to_remove.add(new Util.Multi<>(matcher.start(), matcher.end()));
            date.indexPairs = index_pairs_to_remove;
            date.note = getClass().getSimpleName();
            dates.add(date);
        }
        date_internal.dateList = dates;
        return date_internal;
    }
}                      // 01-04-2020