package com.events.date.time;

import com.events.Util;
import com.events.DateMatcher;
import com.events.date.Calendar;
import com.events.date.DateMeta;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.events.date.M_Static.MONTHS_ORDER;
import static com.events.Util.set;

public class SingleMillis extends DateMatcher {
    public DateMeta matchInternal(String text) {
        String string_date;
        Pattern pattern = Pattern.compile("(15[0-9]{8})([0-9]{3})?");
        Matcher matcher = pattern.matcher(text);
        Set<Calendar.Date> dates = set();
        DateMeta date_internal = new DateMeta();
        while (matcher.find()) {
            getIndexPairs(text, date_internal, matcher);
            java.util.Date javaDate = new java.util.Date(Long.valueOf(matcher.group(1)));
            Calendar.Date date = new Calendar.Date(String.valueOf(javaDate.getDate()), MONTHS_ORDER.get(javaDate.getMonth()), String.valueOf(javaDate.getYear() + 1900));
            Util.MultiList<Integer, Integer> index_pairs_to_remove = new Util.MultiList<>();
            index_pairs_to_remove.add(new Util.Multi<>(matcher.start(), matcher.end()));
            date.indexPairs = index_pairs_to_remove;
            date.note = getClass().getSimpleName();
            dates.add(date);
        }
        date_internal.dateList = new ArrayList<>(dates);
        return date_internal;
    }
}