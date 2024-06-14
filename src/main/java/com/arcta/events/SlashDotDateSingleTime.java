package com.arcta.events;
import java.util.regex.Matcher;
import static com.arcta.events.Calendar.M_YEAR_CUR_OR_NEX;
import static com.arcta.events.M_Static.*;
class SlashDotDateSingleTime extends DateTimeMatcher {
    Util.MultiList<Calendar.Date, Time> matchInternal(String text) {
        String dayMonthYear = "(0|1|2|3)([0-9])(/|\\.)(0|1)([0-9])\\3(20)?" + M_YEAR_CUR_OR_NEX + SPACESO + "([0-9]{1,2})[:|\\.]([0-5])(0|5)" + MWO + "(am|pm)?";
        Util.MultiList<Calendar.Date, Time> dateTimes = new Util.MultiList<>();
        Matcher matcher = Util.matcher(dayMonthYear, text);
        while (matcher.find()) { Calendar.Date date = new Calendar.Date();
            date.dateDay = String.valueOf(Integer.valueOf(matcher.group(1) + matcher.group(2)));
            Integer month = Integer.valueOf(matcher.group(4) + matcher.group(5));
            if (month > 12) continue;
            date.dateMonth = MONTHS_ORDER.get(month - 1);
            date.dateYear = "20" + (matcher.group(7) == null ? Calendar.defaultYearAbbrev(date.dateMonth) : matcher.group(7));
            Time time = new Time();
            time.setHour(String.valueOf(Integer.valueOf(matcher.group(8))));
            if (Integer.parseInt(time.getHour()) > 24) continue;
            time.timeMinute = matcher.group(9) + matcher.group(10);
            time.amPm = matcher.group(11);
            time.provenance = getClass().getSimpleName();
            time.convertTo24H();
            dateTimes.add(new Util.Multi<>(date, time));} return dateTimes;}}     // 17/11/(20)20 7.30(pm)