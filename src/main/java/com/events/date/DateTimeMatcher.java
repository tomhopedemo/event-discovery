package com.events.date;

import com.events.*;
import com.events.date.datetime.SingleTimeSingleDate;
import com.events.date.datetime.SlashDotDateSingleTime;
import com.events.date.single.SingleDateSingleTime;
import com.events.date.single.SingleDateSingleTimeAmPm;

import java.util.List;

import static com.events.Util.list;

public abstract class DateTimeMatcher {
    public abstract Util.MultiList<Calendar.Date, Time> matchInternal(String text);

    public static Util.MultiList<Calendar.Date, Time> match(String text, Context.TimeContext timeCtx, Context.DateTimeContext datetimeCtx) {
        Util.MultiList<Calendar.Date, Time> date_times = new Util.MultiList<>();
        List<DateTimeMatcher> matchers = matchers(timeCtx, datetimeCtx);
        for (DateTimeMatcher dateTimeMatcher : matchers) {
            Util.addAll(date_times, dateTimeMatcher.matchInternal(text));
        }
        return date_times;
    }

    public static List<String> matchersNames(Context.TimeContext timeCtx, Context.DateTimeContext datetimeCtx) {
        List<String> names = list();
        List<DateTimeMatcher> matchers = matchers(timeCtx, datetimeCtx);
        matchers.forEach(m -> names.add(m.getClass().getSimpleName()));
        return names;
    }

    static List<DateTimeMatcher> matchers(Context.TimeContext timeCtx, Context.DateTimeContext datetimeCtx) {
        List<DateTimeMatcher> dateTimeMatchers = list();
        if (datetimeCtx.datetimeReverse()) {
            dateTimeMatchers.add(new SingleTimeSingleDate());
        } else {
            dateTimeMatchers.add(new SingleDateSingleTime(timeCtx.timePm(), timeCtx.timeDoors()));
            dateTimeMatchers.add(new SingleDateSingleTimeAmPm());
            dateTimeMatchers.add(new SlashDotDateSingleTime());
        }
        return dateTimeMatchers;
    }
}