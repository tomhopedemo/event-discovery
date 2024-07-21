package com.events.date;

import com.events.date.Calendar;
import com.events.date.DateMatcher;
import com.events.date.DateMeta;
import com.events.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static com.events.HyphenMatchers.M_HYPHENS;
import static com.events.Util.Months.MONTHS_STANDARD_ENG;
import static com.events.Util.*;
import static com.events.date.M_Static.*;

class BetweenDateSeason extends DateMatcher {
    public DateMeta matchInternal(String text) {
        String regex = M_DAY_MONTH_YEAR + " " + M_HYPHENS + " " + M_SEASON + " " + M_YEAR;
        Matcher matcher = matcher(regex, text);
        Set<List<Calendar.Date>> betweenSet = set();
        DateMeta meta = new DateMeta();
        while (matcher.find()) {
            getIndexPairs(text, meta, matcher);
            Calendar.Date date_from = new Calendar.Date(matcher.group(1), MONTHS_STANDARD_ENG.get(matcher.group(2)), matcher.group(3) + matcher.group(4));
            Calendar.Date date_to = SEASONS.get(matcher.group(6)).cloneMe();
            date_to.dateYear = "20" + matcher.group(8);
            List<Calendar.Date> dates = constructIntermediateDates(date_from, date_to);
            if (Util.empty(dates)) continue;
            for (Calendar.Date date : dates) {
                Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
                indexPairsToRemove.add(new Util.Multi<>(matcher.start(), matcher.end()));
                date.indexPairs = indexPairsToRemove;
            }
            dates.forEach(d -> d.note = getClass().getSimpleName());
            betweenSet.add(dates);
        }
        meta.betweenList = new ArrayList<>(betweenSet);
        meta.between = get(meta.betweenList, 0);
        return meta;
    }
}