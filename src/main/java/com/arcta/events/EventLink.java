package com.arcta.events;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static com.arcta.events.Util.*;
import static com.arcta.events.Util.sout;
class EventLink {
    static final ArrayList<String> SECOND_CLICK_TEXT = list("book tickets", "book now", "buy tickets", "book", "view tickets");
    static final String SECOND_CLICK_PARTIAL_TEXT = "check dates";
    static final List<String> EXCLUDED_HOSTS = list("twitter.com");
    static final List<String> LINK_TIME_AVOID_PHRASES_DIRECTIONAL = list("box office", "opening times", "opening hours");
    static final List<String> LINK_TIME_AVOID_PHRASES_NON_DIRECTIONAL = list("tel:","+44");
    static Map<Calendar.Date, Time> make(String link, Context ctx, List<Calendar.Date> dates, BaseDirs dirs) {
        if (EXCLUDED_HOSTS.contains(WebUtils.hostProperNoWww(link))) return null;
        Document doc = null;
        if (ctx.link1Download()) {
            Util.Multi<Document, String> multi = WebReader.Download.documentRedirect(ctx, link, dirs, ctx.downloadDelay());
            doc = safeA(multi);
        } else if (ctx.link1Phantom()) {
            Util.Multi<Document, String> documentLocation = WebReader.Download.phantomjs(link, ctx, dirs, ctx.downloadDelay());
            doc = safeA(documentLocation);
        } else if (ctx.link1Google()) {
            Util.Multi<Document, String> documentLocation = WebReader.Download.googlechrome(link, dirs);
            doc = safeA(documentLocation);
        } else {
            Util.Multi3 multi = WebReader.readParse(link);
            if (multi != null) {
                doc = (Document) multi.a;}}
        if (doc == null) return null;
        if (ctx.link1Iframe()){
            link = Iframe.make(link, doc, ctx);
            Util.Multi3 multi = WebReader.readParse(link);
            if (multi != null) {
                doc = (Document) multi.a;}}
        Map<Calendar.Date, Time> datetimes = linkTimesInternal(link, ctx, dates, doc, dirs);
        return datetimes;}
    static Util.Multi<Document, String> thirdLink(Context ctx, String link, Document doc, BaseDirs dirs) {
        String thirdText = ctx.link3Text();
        if (empty(thirdText)) return null;
        Map<String, String> hrefText = WebUtils.hrefText(doc);
        if (empty(hrefText)) return null;
        String thirdLinkUrl = null;
        for (String href : hrefText.keySet()) {
            String text = hrefText.get(href);
            if (safeNull(text).toLowerCase().equals(thirdText)){
                thirdLinkUrl = WebReader.UriExtension.toFullUrl(link, href);}}
        if (empty(thirdLinkUrl)) return null;
        if (ctx.link3Download()){ Util.Multi3<Document,String,String> multi = WebReader.Download.document(ctx, thirdLinkUrl, null, ctx.downloadDelay()); return new Util.Multi<>(safeA(multi), thirdLinkUrl);
        } else if (ctx.link3Phantom()) { Util.Multi<Document,String> docLocation = WebReader.Download.phantomjs(thirdLinkUrl, ctx, dirs,ctx.downloadDelay()); return new Util.Multi<>(safeA(docLocation), thirdLinkUrl);
        } else { Util.Multi3<Document, String,String> multi = WebReader.readParse(thirdLinkUrl);
            if (WebUtils.safeA(multi) != null) { return new Util.Multi<>(multi.a, thirdLinkUrl);}}
        return null;}
    static Util.Multi<Document, String> secondLink(Context ctx, String link, Document doc, BaseDirs dirs) { Map<String, String> hrefText = WebUtils.hrefText(doc);
        if (empty(hrefText)) return null;
        String partialPermitted = ctx.link2Partial() ? SECOND_CLICK_PARTIAL_TEXT : null;
        String permittedSpecific = ctx.link2Specific();
        String href2 = ctx.link2Href();
        List<String> permittedGeneral = ctx.link2Permitted() ? SECOND_CLICK_TEXT : null;
        if (!empty(href2) && containsRev(href2, hrefText.keySet())){ hrefText.entrySet().removeIf(e -> !e.getKey().contains(href2));}
        String secondUrl = null;
        if (!empty(partialPermitted)) {
            for (String href : hrefText.keySet()) {
                String text = hrefText.get(href);
                if (safeNull(text).toLowerCase().startsWith(partialPermitted)){ secondUrl = WebReader.UriExtension.toFullUrl(link, href); break;}}}
        if (secondUrl == null && !empty(permittedSpecific)){         //second href good -
            for (String href : hrefText.keySet()) { String text = hrefText.get(href);
                if (safeNull(text).toLowerCase().equals(permittedSpecific)){ secondUrl = WebReader.UriExtension.toFullUrl(link, href); break;}}}
        if (secondUrl == null && !empty(permittedGeneral)){
            for (String href : hrefText.keySet()) { String text = hrefText.get(href);
                if (permittedGeneral.contains(safeNull(text).toLowerCase())){ secondUrl = WebReader.UriExtension.toFullUrl(link, href); break;}}}
        if (secondUrl == null) return null;
        if (ctx.link2Download()){ Util.Multi3<Document, String,String> multi = WebReader.Download.document(ctx, secondUrl, null, ctx.downloadDelay()); return new Util.Multi<>(safeA(multi), secondUrl);
        } else if (ctx.link2Phantom()) { Util.Multi<Document,String> docLocation = WebReader.Download.phantomjs(secondUrl, ctx, dirs,ctx.downloadDelay()); return new Util.Multi<>(safeA(docLocation), secondUrl);
        }else if (secondUrl.matches(".*/#.+")){ Util.Multi3<Document, String,String> multi = WebReader.Download.document(ctx, secondUrl, null, ctx.downloadDelay()); return new Util.Multi<>(safeA(multi), secondUrl);
        } else { Util.Multi3<Document, String,String> multi = WebReader.readParse(secondUrl);
            if (WebUtils.safeA(multi) != null) { return new Util.Multi<>(multi.a, secondUrl);}}
        return null;}
    static Map<Calendar.Date,Time> linkTimesInternal(String link, Context ctx, List<Calendar.Date> dates, Document doc, BaseDirs dirs) { String pageText = cleanPageText(link, ctx, doc);
        if (!empty(ctx.linkxClass())){return DateTimeParse.make(doc, ctx);}
        if (pageText == null) return null;
        if (ctx.parseBlank() && empty(dates)){ dates = DateMatcher.match(new Util.StringMutable(pageText), ctx.lang, ctx.date);}
        Map<Calendar.Date, Time> dateTimes = levelOneDateTimes(ctx, dates, doc);
        if (!empty(dateTimes)) return dateTimes;
        dateTimes = classicalPageTimes(ctx, pageText, dates);
        Util.Multi<Document, String> docUrl = secondLink(ctx, link, doc, dirs);
        if (complete(docUrl)) { doc = docUrl.a;
            link = docUrl.b;}
        docUrl = thirdLink(ctx, link, doc, dirs);
        if (complete(docUrl)) { doc = docUrl.a;
            link = docUrl.b;}
        pageText = cleanPageText(link, ctx, doc);
        if (pageText == null) return null;
        Map<Calendar.Date,Time> secondaryDateTimes = classicalPageTimes(ctx, pageText, dates);
        if (!empty(secondaryDateTimes)){ return secondaryDateTimes;
        } else { return dateTimes;}}
    static Map<Calendar.Date, Time> levelOneDateTimes(Context ctx, List<Calendar.Date> dates, Document doc) { String linkclass = ctx.link1Class();         // ---------------------  COMBO MATCHING ON HTML ------------------------
        if (!empty(linkclass)) { Elements elements = doc.getElementsByClass(linkclass);
            Map<Calendar.Date, Time> dateTimes = map();
            for (Element element : elements) { String textE = safeA(Jutil.text(element));
                Time timeIndicative = TimeMatcher.match(new Util.StringMutable(lowercase(textE)), ctx.lang, ctx.time);
                if (timeIndicative != null) {
                    for (Calendar.Date date : safeNull(dates)) { dateTimes.put(date, timeIndicative.cloneMe());}}} return dateTimes;}
        return null;}
    static Map<Calendar.Date,Time> classicalPageTimes(Context ctx, String pageText, List<Calendar.Date> dates){ Map<Calendar.Date,Time> dateTimes = comboMatch(dates, pageText, ctx);
        if (!empty(dateTimes)) return dateTimes;
        if (ctx.link1ComboOnly()) return dateTimes;
        return pageTime(ctx, dates, pageText);}
    static Map<Calendar.Date,Time> pageTime(Context ctx, List<Calendar.Date> dates, String pageText) { Map<Calendar.Date, Time> dateTimes = map();
        if (empty(dates)) return dateTimes;
        List<Integer> avoidIndicesDirectional = indices(pageText, LINK_TIME_AVOID_PHRASES_DIRECTIONAL);
        List<Integer> avoidIndicesNonDirectional = indices(pageText, LINK_TIME_AVOID_PHRASES_NON_DIRECTIONAL);
        Util.StringMutable clean = new Util.StringMutable(pageText);
        Time timeIndicative;
        if (empty(avoidIndicesDirectional) && empty(avoidIndicesNonDirectional)) { timeIndicative = TimeMatcher.matchOverall(clean, ctx.lang, ctx.time);
        } else { timeIndicative = TimeMatcher.matchAvoidIndices(clean, ctx.lang, ctx.time);}
        if (timeIndicative != null) { timeIndicative.provenance = "LINK_TIME_" + safeNull(timeIndicative.provenance);
            for (Calendar.Date date : dates) { dateTimes.put(date, timeIndicative.cloneMe());}} return dateTimes;}
    static String cleanPageText(String link, Context ctx, Document doc) { Util.Multi<String, Util.MultiList<Integer, Element>> page = Jutil.text(doc);
        String pageText = lowercase(safeA(page));
        if (empty(pageText)) return null;
        pageText = pageText.replaceAll("(\\s|\\u00A0)+", " ");
        if (pageText.length() > 100000 && !ctx.parseLong()) { sout("100000+ page length for " + ctx.ref + " " + link); return null;}
        return pageText;}
    static Map<Calendar.Date, Time> comboMatch(List<Calendar.Date> dates, String pageText, Context ctx) { if (empty(dates) && !ctx.parseBlank()) return null; // ----------- original combo match
        Util.MultiList<Calendar.Date, Time> rawDateTimes = DateTimeMatcher.match(pageText, ctx.time, ctx.datetime);
        Map<Calendar.Date, Time> dateTimes = map();
        if (!empty(rawDateTimes)){
            if (ctx.parseBlank()){
                for (Util.Multi<Calendar.Date, Time> multi : rawDateTimes.underlying) { if (multi.a == null || multi.b == null) continue;
                    dateTimes.put(multi.a, multi.b);}
            } else {
                for (Calendar.Date date : dates) { Time time = rawDateTimes.getBOne(date);
                    if (time != null) { dateTimes.put(date, time);}}}}
        Map<String, Time> rawWeekdayTimes = DateMatcher.DayTimeMatcher.matchWeekdayTime(pageText);            // ---------------- weekday to weekday combo match ------------------
        for (Calendar.Date date : safeNull(dates)) { if (dateTimes.containsKey(date)) continue;
            Calendar.setDayOfWeek(date);
            if (empty(date.dayOfWeek)) continue;
            Time time = rawWeekdayTimes.get(date.dayOfWeek);
            if (time == null) continue;
            dateTimes.put(date, time);} return dateTimes;}}