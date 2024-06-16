package com.arcta.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import static com.arcta.events.HyphenMatchers.M_HYPHENS_TO_UNTIL;
import static com.arcta.events.M_Month.M_MONTHS_SPACE_BEFORE_AND_AFTER;
import static com.arcta.events.M_Static.*;
import static com.arcta.events.M_Weekday.M_WEEKDAYO_COMMA;
import static com.arcta.events.Util.Months.MONTHS_STANDARD_ENG;
import static com.arcta.events.Util.list;
import static com.arcta.events.Util.set;

class BetweenTwoDatesText extends DateMatcher {
    final boolean allIndices;

    BetweenTwoDatesText() {
        this(false);
    }

    BetweenTwoDatesText(boolean allIndices) {
        this.allIndices = allIndices;
    }

    public DateMeta matchInternal(String text) {
        String timeO = "([0-9]{1,2}:[0-9]{2})?";
        Matcher matcherPrecheck = Util.matcher(M_WEEKDAYO_COMMA + MWO + M_DAY_MONTHO_YEARO + "[\\.|,]?" + SPACESO + timeO + SPACESO + M_HYPHENS_TO_UNTIL + MWO + M_WEEKDAYO_COMMA + MWO2 + M_DAY_MONTH_YEARO, text);
        if (!matcherPrecheck.find()) return null;
        String regex = "(?<!" + M_MONTHS_SPACE_BEFORE_AND_AFTER + "| vol )" + "(from |dates: )?" + M_WEEKDAYO_COMMA + MWO + NEGATIVE_LOOKBEHIND_DIGITS + M_DAY_MONTHO_YEARO + "[\\.|,]?" + SPACESO + timeO + SPACESO + M_HYPHENS_TO_UNTIL + MWO + M_WEEKDAYO_COMMA + MWO2 + M_DAY_MONTH_YEARO;
        Collection<List<Calendar.Date>> betweenSet = allIndices ? list() : set();
        DateMeta meta = new DateMeta();
        Matcher matcher = Util.matcher(regex, text);
        while (matcher.find()) {
            Calendar.Date dateFrom = new Calendar.Date();
            Calendar.Date dateTo = new Calendar.Date();
            dateTo.dateDay = matcher.group(11).trim();
            if (dateTo.dateDay.length() == 2 && dateTo.dateDay.startsWith("0")) {
                dateTo.dateDay = dateTo.dateDay.substring(1, 2);
            }
            dateTo.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(13));
            dateTo.dateYear = "20" + (matcher.group(15) == null ? Calendar.defaultYearAbbrev(dateTo.dateMonth) : matcher.group(15).trim());
            dateFrom.dateDay = matcher.group(3).trim();
            if (dateFrom.dateDay.length() == 2 && dateFrom.dateDay.startsWith("0")) {
                dateFrom.dateDay = dateFrom.dateDay.substring(1, 2);
            }
            dateFrom.dateMonth = matcher.group(5) == null ? MONTHS_STANDARD_ENG.get(matcher.group(13)) : MONTHS_STANDARD_ENG.get(matcher.group(5).trim());
            if (matcher.group(7) != null) {
                dateFrom.dateYear = "20" + matcher.group(7).trim();
            } else {
                if (MONTHS_ORDER.indexOf(dateFrom.dateMonth) <= MONTHS_ORDER.indexOf(dateTo.dateMonth)) {
                    dateFrom.dateYear = dateTo.dateYear;
                } else {
                    dateFrom.dateYear = String.valueOf(Integer.parseInt(dateTo.dateYear) - 1);
                }
            }
            String timestring = matcher.group(8);
            Integer midIndexA = null;
            Integer midIndexB = null;
            if (timestring != null) {
                timestring = timestring.trim();
                String[] split = timestring.split(":");
                Integer hour = Integer.valueOf(split[0]);
                Integer minute = Integer.valueOf(split[1]);
                if (hour <= 24 && minute < 60) {
                    midIndexA = matcher.start(8);
                    midIndexB = matcher.end(8);
                }
            }
            if (midIndexA == null) {
                getIndexPairs(text, meta, matcher.start(), matcher.end());
            } else {
                getIndexPairs(text, meta, matcher.start(), midIndexA);
                getIndexPairs(text, meta, midIndexB, matcher.end());
            }
            List<Calendar.Date> dates = constructIntermediateDates(dateFrom, dateTo);
            if (Util.empty(dates)) continue;
            for (Calendar.Date date : dates) {
                Util.MultiList<Integer, Integer> indexPairs = new Util.MultiList<>();
                if (midIndexA == null) {
                    indexPairs.add(new Util.Multi<>(matcher.start(), matcher.end()));
                } else {
                    indexPairs.add(new Util.Multi<>(matcher.start(), midIndexA));
                    indexPairs.add(new Util.Multi<>(midIndexB, matcher.end()));
                }
                date.indexPairs = indexPairs;
            }
            dates.forEach(d -> d.note = getClass().getSimpleName());
            betweenSet.add(dates);
        }
        meta.betweenList = new ArrayList<>(betweenSet);
        meta.between = Util.get(meta.betweenList, 0);
        return meta;
    }
}                // 1 apr 2020 - 2 apr 2020