package com.arcta.events;
import java.util.regex.Matcher;
import static com.arcta.events.Util.Months.MONTHS_STANDARD_ENG;
import static com.arcta.events.M_Month.M_MONTH_ENG;
import static com.arcta.events.M_Static.*;
import static com.arcta.events.M_Weekday.M_WEEKDAYO_ENG;
import static com.arcta.events.TimeMatcher.M_TIME_AMO;
class SingleTimeSingleDate extends DateTimeMatcher {
    Util.MultiList<Calendar.Date, Time> matchInternal(String text) { Util.MultiList<Calendar.Date, Time> dateTimes = new Util.MultiList<>();
        String time_date = M_TIME_AMO + SPACES + M_WEEKDAYO_ENG + "[,]?" + SPACESO + M_DAY_ORDINALO + MWO + M_MONTH_ENG + MWO + "[,]?" + MWO + M_YEARO;
        Matcher matcher = Util.matcher(time_date, text);
        while (matcher.find()) { Time time = new Time(String.valueOf(Integer.valueOf(matcher.group(1).trim())), matcher.group(2), matcher.group(3));
            if (Integer.parseInt(time.getHour()) > 24) continue;
            time.provenance = getClass().getSimpleName();
            time.convertTo24H();
            Calendar.Date date = new Calendar.Date();
            date.dateDay = String.valueOf(Integer.valueOf(matcher.group(5)));
            date.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(7));
            date.dateYear = "20" + (matcher.group(9) == null ? Calendar.defaultYearAbbrev(date.dateMonth) : matcher.group(9));
            dateTimes.add(new Util.Multi<>(date, time));} return dateTimes;}}       //7.30(pm) (sat) 3(rd) jan (2020)
