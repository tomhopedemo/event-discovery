package com.events;

import com.events.date.Calendar;
import com.events.date.DateMatcher;
import com.events.date.Time;
import com.events.date.TimeMatcher;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


class DateTimeParse {
    static Map<Calendar.Date, Time> make(Document doc, Context ctx) {
        String identifier = ctx.linkxClass();
        Elements elements = doc.getElementsByClass(identifier);
        Map<Calendar.Date, Time> dateTimes = new HashMap<>();
        for (Element element : elements) {
            Util.Multi<String, Util.MultiList<Integer, Element>> textHtml = Jutil.text(element);
            Util.StringMutable clean = new Util.StringMutable(Util.safeA(textHtml).toLowerCase());
            List<Calendar.Date> match = DateMatcher.match(clean, ctx.lang, ctx.date);
            if (Util.empty(match)) continue;
            Calendar.Date date = match.get(0);
            if (date == null) continue;
            Time time = TimeMatcher.match(clean, ctx.lang, ctx.time);
            if (time == null) continue;
            dateTimes.put(date, time);
        }
        return dateTimes;
    }
}