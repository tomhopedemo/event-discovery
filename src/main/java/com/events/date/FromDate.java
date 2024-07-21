package com.events.date;

import com.events.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static com.events.date.Calendar.PREVIOUS_YEAR_ABBREV;
import static com.events.date.Calendar.defaultYearAbbrev;
import static com.events.Util.Months.MONTHS_STANDARD_ENG;
import static com.events.Util.set;
import static com.events.date.M_Static.M_DAY_MONTH_YEARO;
import static com.events.date.M_Weekday.M_WEEKDAYO_SPACE_AFTER_ENG;

class FromDate extends DateMatcher {
    public DateMeta matchInternal(String text) {
        String regex = "from " + M_WEEKDAYO_SPACE_AFTER_ENG + M_DAY_MONTH_YEARO;
        Matcher matcher = Util.matcher(regex, text);
        Set<List<Calendar.Date>> between_set = set();
        DateMeta meta = new DateMeta();
        while (matcher.find()) {
            getIndexPairs(text, meta, matcher);
            Calendar.Date date = new Calendar.Date();
            date.dateDay = matcher.group(2);
            date.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(4));
            Calendar.CalendarDate currentCalendarDate = Calendar.currentCalendarDate();
            String yearAbbrev = defaultYearAbbrev();
            if (currentCalendarDate.dateMonth.equals("jan")) {
                if (Util.list("dec", "nov").contains(date.dateMonth)) {
                    yearAbbrev = PREVIOUS_YEAR_ABBREV;
                }
            }
            if (currentCalendarDate.dateMonth.equals("feb")) {
                if (Util.list("dec").contains(date.dateMonth)) {
                    yearAbbrev = PREVIOUS_YEAR_ABBREV;
                }
            }
            date.dateYear = "20" + (matcher.group(6) == null ? yearAbbrev : matcher.group(6));
            Calendar.CalendarDate temporaryToDate = Calendar.get(Calendar.indexOf(date) + 30);
            List<Calendar.Date> dates = constructIntermediateDates(date, temporaryToDate.toDate());
            if (Util.empty(dates)) continue;
            for (Calendar.Date date_iterate : dates) {
                Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
                indexPairsToRemove.add(new Util.Multi<>(matcher.start(), matcher.end()));
                date_iterate.indexPairs = indexPairsToRemove;
            }
            dates.forEach(d -> d.note = getClass().getSimpleName());
            between_set.add(dates);
        }
        meta.betweenList = new ArrayList<>(between_set);
        meta.between = Util.get(meta.betweenList, 0);
        return meta;
    }
}