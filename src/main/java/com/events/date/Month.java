package com.events.date;

import com.events.DateMatcher;
import com.events.Util;

import java.util.Map;
import java.util.regex.Matcher;

import static com.events.date.Calendar.defaultYearFull;

public class Month extends DateMatcher {
    String monthMatcher;
    Map<String, String> monthsStandard;

    public Month(String monthMatcher, Map<String, String> monthsStandard) {
        this.monthMatcher = monthMatcher;
        this.monthsStandard = monthsStandard;
    }

    public DateMeta matchInternal(String text) {
        Matcher matcher = Util.matcher("\\b" + monthMatcher + "\\b", text);
        DateMeta meta = new DateMeta();
        if (matcher.find()) {
            Calendar.Date date = new Calendar.Date();
            date.dateMonth = monthsStandard.get(matcher.group(1));
            date.dateYear = defaultYearFull(date.dateMonth);
            meta.dateList.add(date);
        }
        return meta;
    }
}