package com.events.date;

import com.events.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static com.events.HyphenMatchers.M_HYPHENS_TO;
import static com.events.Util.Months.MONTHS_STANDARD_ENG;
import static com.events.Util.set;
import static com.events.date.M_Month.M_MONTH_ENG;
import static com.events.date.M_Static.M_DAY_ORDINALO;
import static com.events.date.M_Static.M_YEAR_STRONG;
import static com.events.date.M_Weekday.M_WEEKDAYO_ENG;

class BetweenTwoDatesTextReverse extends DateMatcher { // ------------ (1.REVERSE) month date
    public DateMeta matchInternal(String text) {         //may 21st 2018 may 21 18:00 okay fine, but may 18:00 - cannot end with colon
        String dayMonthYear = M_WEEKDAYO_ENG + "[,]?[ ]?" + M_MONTH_ENG + " " + M_DAY_ORDINALO + "[ |\u00A0|\\.|,]" + M_HYPHENS_TO + "[ ]?" + M_DAY_ORDINALO + "[ ]?" + M_YEAR_STRONG + "(?!:)";
        Set<List<Calendar.Date>> betweenSet = set();
        DateMeta meta = new DateMeta();
        Matcher matcher = Util.matcher(dayMonthYear, text);
        while (matcher.find()) {
            getIndexPairs(text, meta, matcher.start(), matcher.end());
            Calendar.Date date_from = new Calendar.Date();
            Calendar.Date date_to = new Calendar.Date();
            date_from.dateDay = matcher.group(3);
            date_from.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(2));
            date_from.dateYear = matcher.group(8);
            date_to.dateDay = matcher.group(6);
            date_to.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(2));
            date_to.dateYear = matcher.group(8);
            List<Calendar.Date> dates_list = constructIntermediateDates(date_from, date_to);
            if (Util.empty(dates_list)) continue;
            for (Calendar.Date date : dates_list) {
                Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
                indexPairsToRemove.add(new Util.Multi(matcher.start(), matcher.end()));
                date.indexPairs = indexPairsToRemove;
            }
            dates_list.forEach(d -> d.note = getClass().getSimpleName());
            betweenSet.add(dates_list);
        }
        if (Util.empty(betweenSet)) return null;
        meta.betweenList = new ArrayList<>(betweenSet);
        meta.between = Util.get(meta.betweenList, 0);
        return meta;
    }
}