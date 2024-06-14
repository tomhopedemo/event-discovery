package com.arcta.events;
import org.jsoup.nodes.Element;
import java.util.List;
import static com.arcta.events.Util.*;
class Parse {
    static String septext(Context ctx, Element element) { String _class = ctx.parseSeptextClass();
        if (!Util.empty(_class)){return Util.safeA(Jutil.text(element, null, null, null, null, list(_class), null, null, ctx.parseGap(), ctx.parseTagGap()));}
        String tag = ctx.parseSeptextTag();
        if (!Util.empty(tag)){return Util.safeA(Jutil.text(element, null, null, null, list(tag), null, null, null, ctx.parseGap(), ctx.parseTagGap()));}
        return null;}
    static List<Calendar.Date> matchDates(Context ctx, Util.StringMutable clean) { List<Calendar.Date> dates = DateMatcher.match(clean, ctx.lang, ctx.date);
        return Util.empty(dates) ? null : dates;}
    static Util.Multi3<List<String>, List<String>, Multi> exclusions(Context ctx){ List<String> xclasses = Util.splitList(ctx.parseExcClasses(), "\\|");
        List<String> xtags = Util.splitList(ctx.parseExcTags(), "\\|");
        Util.Multi<String,String> xattrs = null;
        if (Util.contains(ctx.parseExcAttr(), "-")) { List<String> underlying = Util.splitList(ctx.parseExcAttr(), "\\-");
            xattrs = new Util.Multi<>(Util.get(underlying, 0), Util.get(underlying, 1));}
        if (xclasses == null) xclasses = list(); return new Util.Multi3<>(xclasses, xtags, xattrs);}
    static boolean check(String string) { if (Util.empty(string)) return true;
        return (!string.matches(".*[a-zA-Z].*"));}
    static Time getTime(Context ctx, Util.StringMutable clean, String linkText) {Time timeIndicative = null;
        if (!Util.empty(linkText)){timeIndicative = TimeMatcher.exactMatchSingle(linkText.toLowerCase().trim(), ctx.lang, ctx.time);}
        Time match = TimeMatcher.match(clean, ctx.lang, ctx.time);
        if (timeIndicative == null) timeIndicative = match; return timeIndicative;}
    static Event eventManufacture(Context ctx, String url, String text, List<Calendar.Date> datesParameter) { if (empty(text)) return null;
        Util.StringMutable clean = new Util.StringMutable(text.toLowerCase());
        List<Calendar.Date> dates = datesParameter != null ? datesParameter : DateMatcher.match(clean, ctx.lang, ctx.date);
        if (Util.empty(dates)) return null;
        Time timeIndicative = TimeMatcher.match(clean, ctx.lang, ctx.time);
        Event event = new Event(ctx.ref);
        event.originalText = text;
        if (!Util.empty(ctx.timeManual()) && timeIndicative == null){timeIndicative = TimeMatcher.match(new Util.StringMutable(ctx.timeManual()), ctx.lang, ctx.time);}
        if (timeIndicative != null) {
            for (Calendar.Date date : dates) {event.dateTimes.put(date, timeIndicative);}}
        event.name = clean.string;
        event.link = url;
        for (Calendar.Date date : dates) {event.dateUrl.put(date, url);}
        event.source = url; return Util.empty(event.name) ? null : event;}}