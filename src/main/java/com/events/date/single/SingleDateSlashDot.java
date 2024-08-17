package com.events.date.single;

import com.events.Util;
import com.events.DateMatcher;
import com.events.date.Calendar;
import com.events.date.DateMeta;

import java.util.List;
import java.util.regex.Matcher;

import static com.events.date.M_Static.*;
import static com.events.Util.list;

public class SingleDateSlashDot extends DateMatcher {
    public DateMeta matchInternal(String text) {
        List<Calendar.Date> dates = list();
        String regex = NEGATIVE_LOOKBEHIND_DIGITS + "(0|1|2|3)?([0-9])" + MWO + "(/|\\.)" + MWO + "(0|1)?([0-9])" + MWO + "\\3" + MWO + "(20)?(2[4-9])";
        Matcher matcher = Util.matcher(regex, text);
        DateMeta meta = new DateMeta();
        while (matcher.find()) {
            getIndexPairs(text, meta, matcher);
            Calendar.Date date = new Calendar.Date();
            String firstDigit = Util.empty(matcher.group(1)) ? "" : matcher.group(1);
            Integer date_int = Integer.valueOf(firstDigit + matcher.group(2));
            if (date_int > 31) continue;
            date.dateDay = String.valueOf(date_int);
            Integer month = Integer.valueOf(Util.safeNull(matcher.group(4)) + matcher.group(5));
            if (month > 12 || month < 1) continue;
            date.dateMonth = MONTHS_ORDER.get(month - 1);
            date.dateYear = "20" + matcher.group(7);
            Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
            indexPairsToRemove.add(new Util.Multi<>(matcher.start(), matcher.end()));
            date.indexPairs = indexPairsToRemove;
            date.note = getClass().getSimpleName();
            dates.add(date);
        }
        meta.dateList = dates;
        return meta;
    }
}