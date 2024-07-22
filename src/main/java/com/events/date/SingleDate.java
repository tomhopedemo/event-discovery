package com.events.date;

import com.events.Util;
import com.events.DateMatcher;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.events.date.M_Lang.*;
import static com.events.date.M_Static.*;
import static com.events.Util.list;
import static com.events.Util.opt;

public class SingleDate extends DateMatcher {
    Util.Lang language;

    public SingleDate(Util.Lang language) {
        this.language = language;
    }

    public DateMeta matchInternal(String text) {
        List<Calendar.Date> dates = list();
        Map<String, String> monthsStandard = LANG_MONTHS_STANDARD.get(language);
        String dayMonthYear = opt(LANG_M_START_INDICATORS.get(language)) + LANG_M_WEEKDAYO_SPACE_AFTER.get(language) + NEGATIVE_LOOKBEHIND_DIGITS + M_DAY + SPACESO + opt(LANG_M_ORDINAL.get(language)) + "(/| of)?" + SPACESO + LANG_M_MONTH.get(language) + SPACESO + M_YEAR + "(?!:|am|pm| am | pm|h|\\.[0-9]|[0-9])";
        DateMeta meta = new DateMeta();
        Matcher matcher = Util.matcher(dayMonthYear, text);
        while (matcher.find()) {
            getIndexPairs(text, meta, matcher);
            Calendar.Date date = new Calendar.Date();
            date.dateDay = String.valueOf(Integer.valueOf(matcher.group(3)));
            date.dateMonth = monthsStandard.get(matcher.group(6));
            String year_suffix = matcher.group(8);
            if (Integer.parseInt(year_suffix) > 25) continue;
            date.dateYear = "20" + year_suffix; //date.date_year = "20" + (year_suffix == null ? Calendar.default_year__abbrev(date.date_month) : year_suffix);
            Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
            indexPairsToRemove.add(new Util.Multi<>(matcher.start(), matcher.end()));
            date.indexPairs = indexPairsToRemove;
            date.note = getClass().getSimpleName();
            dates.add(date);
        }
        meta.dateList = dates;
        return meta;
    }
}                                   // 1 apr 2020