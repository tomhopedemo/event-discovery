package com.events.date;

import com.events.DateMatcher;
import com.events.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static com.events.date.M_Static.MONTHS_ORDER;
import static java.lang.Integer.valueOf;
import static java.lang.String.valueOf;

public class BetweenTwoDatesNumeric extends DateMatcher {
    public DateMeta matchInternal(String text) {
        Set<List<Calendar.Date>> betweenSet = new HashSet<>();
        String regex = "(0|1|2|3)([0-9])(/|\\.)(0|1)([0-9])\\320(19|20|21|22)" + "[\\s]" + Util.HYPHENS + "[\\s]" + "(0|1|2|3)([0-9])\\3(0|1)([0-9])\\320(19|20|21|22)";
        Matcher matcher = DateUtils.matcher(regex, text);
        DateMeta meta = new DateMeta();
        while (matcher.find()) {
            getIndexPairs(text, meta, matcher);
            Calendar.Date dateFrom = new Calendar.Date(valueOf(valueOf(matcher.group(1) + matcher.group(2))), MONTHS_ORDER.get(Integer.parseInt(matcher.group(4) + matcher.group(5)) - 1), "201" + matcher.group(6));
            Calendar.Date dateTo = new Calendar.Date(valueOf(valueOf(matcher.group(7) + matcher.group(8))), MONTHS_ORDER.get(Integer.parseInt(matcher.group(9) + matcher.group(10)) - 1), "20" + matcher.group(11));
            List<Calendar.Date> dates = constructIntermediateDates(dateFrom, dateTo);
            if (DateUtils.empty(dates)) continue;
            for (Calendar.Date date : dates) {
                Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
                indexPairsToRemove.add(new Util.Multi<>(matcher.start(), matcher.end()));
                date.indexPairs = indexPairsToRemove;
            }
            dates.forEach(d -> d.note = getClass().getSimpleName());
            betweenSet.add(dates);
        }
        meta.betweenList = new ArrayList<>(betweenSet);
        meta.between = DateUtils.get(meta.betweenList, 0);
        return meta;
    }
}             // 1/2/2020 - 2/2/2020