package com.arcta.events;

import java.util.List;
import java.util.regex.Matcher;

import static com.arcta.events.M_Month.M_MONTH_ENG;
import static com.arcta.events.M_Static.M_YEAR_STRONG;
import static com.arcta.events.M_Static.SPACES;
import static com.arcta.events.Util.Months.MONTHS_STANDARD_ENG;

class MonthYear extends DateMatcher {
    public DateMeta matchInternal(String text) {
        String regex = M_MONTH_ENG + SPACES + M_YEAR_STRONG;
        Matcher matcher = Util.matcher(regex, text);
        List<Calendar.Date> dates = Util.list();
        while (matcher.find()) {
            Calendar.Date date = new Calendar.Date();
            date.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(1));
            date.dateYear = matcher.group(2);
            Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
            indexPairsToRemove.add(new Util.Multi<>(matcher.start(), matcher.end()));
            date.indexPairs = indexPairsToRemove;
            dates.add(date);
        }
        DateMeta meta = new DateMeta();
        meta.dateList = dates;
        return meta;
    }
}