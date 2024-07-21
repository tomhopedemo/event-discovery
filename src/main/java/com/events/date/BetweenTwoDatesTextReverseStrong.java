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
import static com.events.date.M_Static.NEGATIVE_LOOKAHEAD_DIGITS;
import static com.events.date.M_Weekday.M_WEEKDAYO_ENG;
import static com.events.date.M_Static.M_DAY_ORDINALO;
import static com.events.date.M_Static.M_DAY;

class BetweenTwoDatesTextReverseStrong extends DateMatcher {
    public DateMeta matchInternal(String text) {         //may 21 - jun 12 18:00 okay, but may 18 - jun 13:00 - cannot end with colon or other digits
        String day_month_year = M_WEEKDAYO_ENG + "[,]?[ ]?" + M_MONTH_ENG + " " + M_DAY_ORDINALO + "[ |\u00A0|\\.|,]" + M_HYPHENS_TO + "\\s*" + M_MONTH_ENG + " " + M_DAY + NEGATIVE_LOOKAHEAD_DIGITS;
        Set<List<Calendar.Date>> betweenSet = set();
        DateMeta meta = new DateMeta();
        Matcher matcher = Util.matcher(day_month_year, text);
        while (matcher.find()) {
            getIndexPairs(text, meta, matcher.start(), matcher.end());
            Calendar.Date date_from = new Calendar.Date();
            Calendar.Date date_to = new Calendar.Date();
            date_from.dateDay = matcher.group(3);
            date_from.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(2));
            date_from.dateYear = Calendar.defaultYearFull(date_from.dateMonth);
            date_to.dateDay = matcher.group(7);
            date_to.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(6));
            date_to.dateYear = Calendar.defaultYearFull(date_to.dateMonth);
            List<Calendar.Date> dates_list = constructIntermediateDates(date_from, date_to);
            if (Util.empty(dates_list)) continue;
            for (Calendar.Date date : dates_list) {
                Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
                indexPairsToRemove.add(new Util.Multi<>(matcher.start(), matcher.end()));
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