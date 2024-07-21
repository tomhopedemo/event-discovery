package com.events;

import com.events.date.Calendar;
import com.events.date.Time;

import java.util.Map;

class Theatre {
    static boolean isTheatre(Event event) {
        return !event.ctx.tertiaryNotTheatre() && (Util.contains(event.organizer.toLowerCase(), Util.list("theatre", "playhouse")) || event.ctx.tertiaryTheatre());
    }

    static Util.MultiList<Util.Multi<Calendar.Date, Time>, Util.Multi<Event, Boolean>> markVisibleTheatrePerformances(Util.MultiList<Util.Multi<Calendar.Date, Time>, Event> multis, Map<String, String> filtered) {
        Util.MultiList<Util.Multi<Calendar.Date, Time>, Util.Multi<Event, Boolean>> toReturn = new Util.MultiList<>();
        for (Util.Multi<Util.Multi<Calendar.Date, Time>, Event> multi : multis.underlying) {
            toReturn.add(new Util.Multi<>(multi.a, new Util.Multi<>(multi.b, true)));
        }
        return toReturn;
    }
}