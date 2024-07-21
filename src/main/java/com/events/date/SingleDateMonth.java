package com.events.date;

import com.events.Util;
import com.events.date.Calendar;
import com.events.date.DateMatcher;
import com.events.date.DateMeta;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.events.date.Calendar.defaultYearFull;
import static com.events.date.M_Lang.*;
import static com.events.date.M_Static.*;
import static com.events.Util.list;
import static com.events.Util.opt;

public class SingleDateMonth extends DateMatcher {
    Util.Lang language;

    SingleDateMonth(Util.Lang language) {
        this.language = language;
    }

    public DateMeta matchInternal(String text) {
        List<Calendar.Date> dates = list();
        Map<String, String> monthsStandard = LANG_MONTHS_STANDARD.get(language);
        String day_month_year = opt(LANG_M_START_INDICATORS.get(language)) + opt(LANG_M_WEEKDAY.get(language)) + "[,]?" + MWO + NEGATIVE_LOOKBEHIND_DIGITS + M_DAY + SPACESO + opt(LANG_M_ORDINAL.get(language)) + "(/| of)?" + SPACESO + LANG_M_MONTH.get(language) + "\\b";
        DateMeta dateMeta = new DateMeta();
        Matcher matcher = Util.matcher(day_month_year, text);
        while (matcher.find()) {
            getIndexPairs(text, dateMeta, matcher);
            Calendar.Date date = new Calendar.Date();
            date.dateDay = String.valueOf(Integer.valueOf(matcher.group(3)));
            date.dateMonth = monthsStandard.get(matcher.group(6));
            date.dateYear = defaultYearFull(date.dateMonth);
            Util.MultiList<Integer, Integer> index_pairs_to_remove = new Util.MultiList<>();
            index_pairs_to_remove.add(new Util.Multi<>(matcher.start(), matcher.end()));
            date.indexPairs = index_pairs_to_remove;
            date.note = getClass().getSimpleName();
            dates.add(date);
        }
        dateMeta.dateList = dates;
        return dateMeta;
    }
}                              // 1 apr