package com.arcta.events;
import java.util.List;
import java.util.regex.Matcher;
import static com.arcta.events.Util.Months.MONTHS_STANDARD_ENG;
import static com.arcta.events.M_Month.M_MONTH_ENG;
import static com.arcta.events.M_Static.*;
import static com.arcta.events.M_Weekday.M_WEEKDAYO_ENG;
import static com.arcta.events.Util.list;
class SingleDateReverseStrong extends DateMatcher {
    public DateMeta matchInternal(String text) { List<Calendar.Date> dates = list();
        text = cleanText(text);
        String day_month_year = M_START_INDICATORSO_ENG + M_WEEKDAYO_ENG + "[,]?" + SPACESO + M_MONTH_ENG + SPACES + M_DAY_ORDINALO + "\\s*,\\s*" + M_YEAR_STRONG;
        DateMeta date_internal = new DateMeta();
        Matcher matcher = Util.matcher(day_month_year, text);
        while (matcher.find()) { getIndexPairs(text, date_internal, matcher);
            Calendar.Date date = new Calendar.Date();
            date.dateDay = String.valueOf(Integer.valueOf(matcher.group(4)));
            date.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(3));
            date.dateYear = matcher.group(6);
            date.indexPairs.add(new Util.Multi<>(matcher.start(), matcher.end()));
            date.note = getClass().getSimpleName();
            dates.add(date);}
        date_internal.dateList = dates; return date_internal;}}