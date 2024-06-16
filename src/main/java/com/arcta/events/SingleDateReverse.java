package com.arcta.events;

import java.util.List;
import java.util.regex.Matcher;

import static com.arcta.events.M_Month.M_MONTH_ENG;
import static com.arcta.events.M_Static.*;
import static com.arcta.events.M_Weekday.M_WEEKDAYO_ENG;
import static com.arcta.events.Util.Months.MONTHS_STANDARD_ENG;
import static com.arcta.events.Util.list;

class SingleDateReverse extends DateMatcher {
    public DateMeta matchInternal(String text) {
        List<Calendar.Date> dates = list();//may 21st 2018 may 21 18:00 okay fine, but may 18:00 - cannot end with colon
        text = cleanText(text);
        String day_month_year = M_START_INDICATORSO_ENG + M_WEEKDAYO_ENG + "[,]?[\\s+]?" + M_MONTH_ENG + "[\\.]?\\s+" + M_DAY_ORDINALO + "(\\s|\u00A0|\\.|,| ,)" + "\\s*" + M_WEEKDAYO_ENG + "\\s*" + M_YEAR_STRONGO + "(?!:)";
        DateMeta date_internal = new DateMeta();
        Matcher matcher = Util.matcher(day_month_year, text);
        while (matcher.find()) {
            getIndexPairs(text, date_internal, matcher);
            Calendar.Date date = new Calendar.Date();
            date.dateDay = String.valueOf(Integer.valueOf(matcher.group(4)));
            date.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(3));
            date.dateYear = (matcher.group(8) == null ? "20" + Calendar.defaultYearAbbrev(date.dateMonth) : matcher.group(8));
            Util.MultiList<Integer, Integer> index_pairs_to_remove = new Util.MultiList<>();
            index_pairs_to_remove.add(new Util.Multi<>(matcher.start(), matcher.end()));
            date.indexPairs = index_pairs_to_remove;
            date.note = getClass().getSimpleName();
            dates.add(date);
        }
        date_internal.dateList = dates;
        return date_internal;
    }
}