package com.events;

import com.events.date.Calendar;
import com.events.date.Time;

import java.util.List;
import java.util.Map;

import static com.events.Util.list;

class DateTimeFilters {
    static Util.MultiList<Util.Multi<Calendar.Date, Time>, Event> filter(List<Event> admittedEvents, Map<String, String> filtered) {
        Util.MultiList<Util.Multi<Calendar.Date, Time>, Event> dateTimeEvents = new Util.MultiList<>();
        for (Event event : admittedEvents) {
            for (Calendar.Date date : event.dateTimes.keySet()) {
                if (date == null) continue;
                if (Calendar.Date.strictlyBeforeAfterCheck(date, Calendar.currentDate())) {
                    continue;
                }
                Time time = event.dateTimes.get(date);
                if (Util.empty(time.getHour())) continue;
                if (!time.getHour().matches("\\d+")) continue;
                int timeHourInt = Integer.parseInt(time.getHour());
                if (timeHourInt <= 3) {
                    time.setHour("23");
                    time.timeMinute = "55";
                    date = Calendar.Date.previous(date);
                }
                if (date == null) continue;
                int earlyWeekdayTime = event.ctx.tertiaryEarlier() ? 16 : 17;
                int earlyWeekendTime = event.ctx.tertiaryEarlier() ? 14 : 15;
                int earlySundayTime = 14;
                if (timeHourInt <= earlyWeekdayTime && !list("sun", "sat").contains(date.dayOfWeek)) {
                    filtered.put(event.id, "EARLY");
                    continue;
                }
                if (timeHourInt <= earlyWeekendTime && list("sat").contains(date.dayOfWeek) && !event.ctx.tertiaryExhibition()) {
                    filtered.put(event.id, "EARLY");
                    continue;
                }
                if (timeHourInt <= earlySundayTime && list("sun").contains(date.dayOfWeek) && !event.ctx.tertiaryExhibition()) {
                    filtered.put(event.id, "EARLY");
                    continue;
                }
                if (timeHourInt > 22 && !event.ctx.parseLate()) {
                    filtered.put(event.id, "LATE");
                    continue;
                }
                if (timeHourInt == 22 && Integer.parseInt(time.timeMinute) > 0 && !event.ctx.parseLate()) {
                    filtered.put(event.id, "LATE");
                    continue;
                }
                String parseBeforeHourIncl = event.ctx.parseBeforeHourIncl();
                if (!Util.empty(parseBeforeHourIncl)) {
                    int maxHourIncl = Integer.parseInt(parseBeforeHourIncl);
                    if (timeHourInt > maxHourIncl || (timeHourInt == maxHourIncl && Integer.parseInt(time.timeMinute) > 0)) {
                        filtered.put(event.id, "LATE");
                        continue;
                    }
                }
                dateTimeEvents.add(new Util.Multi<>(new Util.Multi<>(date, time), event));
            }
        }
        return dateTimeEvents;
    }
}