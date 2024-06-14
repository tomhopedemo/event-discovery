package com.arcta.events;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import static com.arcta.events.Calendar.defaultYearFull;
import static com.arcta.events.M_Static.SPACESO;
class MonthExact extends DateMatcher { String monthMatcher; Map<String,String> monthsStandard;
    MonthExact(String monthMatcher, Map<String,String> monthsStandard) { this.monthMatcher = monthMatcher;this.monthsStandard = monthsStandard;}
    public DateMeta matchInternal(String text){ Matcher matcher = Util.matcher("\u2021" + SPACESO + "\\b" + monthMatcher + "\\b" + SPACESO + "\u2021", text);
        List<Calendar.Date> dates = Util.list();
        while (matcher.find()){ Calendar.Date date = new Calendar.Date();
            date.dateMonth = monthsStandard.get(matcher.group(1));
            date.dateYear = defaultYearFull(date.dateMonth);
            Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
            indexPairsToRemove.add(new Util.Multi<>(matcher.start(1), matcher.end(1)));
            date.indexPairs = indexPairsToRemove;
            dates.add(date);}
        DateMeta meta = new DateMeta();
        meta.dateList = dates; return meta;}}