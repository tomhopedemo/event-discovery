package com.events;

import com.events.date.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

import static com.events.Primary.REF_EVENTS;
import static com.events.Util.list;
import static com.events.Util.sout;

class PrimaryWorker {
    static void make(Context ctx, Dirs dirs) {
        boolean executed = nonStructuralMethods(ctx, dirs);
        if (executed) return;
        Util.Multi3<String, String, String> multi3 = Shared.getStructuralParameters(ctx);
        String identifier = multi3.a;
        String method = multi3.b;
        String identifierType = multi3.c;
        for (Util.Url url : ctx.urls()) {
            Util.Multi3<Document, String, String> multi = WebReader.Download.document(ctx, url.url, dirs.getBaseDirs(), true, false);
            Document doc = multi.a;
            String redirectedUrl = multi.b;
            if (doc == null) {
                sout("PROGRAM D - URL FAILED : " + url.url);
                continue;
            }
            if (ctx.primaryIframe()) {
                Util.Multi3<Document, String, String> multiIframe;
                String urlIframe = Iframe.make(redirectedUrl, doc, ctx);
                if (ctx.primaryIframex()) {
                    doc = WebReader.Download.phantomjs(urlIframe, ctx, dirs.getBaseDirs(), true).a;
                    redirectedUrl = urlIframe;
                } else if (ctx.primaryIframeGoogle()) {
                    doc = WebReader.Download.googlechrome(urlIframe, dirs.getBaseDirs(), ctx).a;
                    redirectedUrl = urlIframe;
                } else {
                    multiIframe = WebReader.readParse(urlIframe);
                    if (multiIframe != null) {
                        doc = multiIframe.a;
                        redirectedUrl = multiIframe.b;
                    }
                }
            }
            if (doc == null) {
                sout("PROGRAM D - URL FAILED 2: " + url.url);
                continue;
            }
            if (ctx.primaryIframe2()) {
                String urlIframe = Iframe.make(redirectedUrl, doc, ctx);
                doc = WebReader.Download.phantomjs(urlIframe, ctx, dirs.getBaseDirs(), true).a;
                redirectedUrl = urlIframe;
            }
            if (doc == null) {
                sout("PROGRAM D - URL FAILED 3: " + url.url);
                continue;
            }
            if (identifier == null) {
                sout("PROGRAM D - No Class/Tag/Id Identifier For : " + ctx.ref);
                return;
            }
            List<Element> elements = Shared.generateElements(ctx, identifier, identifierType, doc);
            if (Util.empty(elements) && !Util.empty(ctx.primaryClick())) {
                sout(ctx.ref + " : NO elements with " + identifierType + " identifier " + identifier + " for " + url);
                continue;
            }
            executeProper(ctx, url, method, identifier, identifierType, doc, redirectedUrl, elements, dirs);
            if (!Util.empty(ctx.primaryClick())) {
                Util.Multi3<Document, String, String> multiClick = WebReader.Download.document(ctx, url.url, ctx.primaryClick(), dirs.getBaseDirs(), true, false);
                Element documentClick = multiClick.a;
                String redirectedUrlClick = multiClick.b;
                if (documentClick == null) {
                    sout("PROGRAM D - CLICK URL FAILED : " + url.url);
                    continue;
                }
                List<Element> elementsClick = Shared.generateElements(ctx, identifier, identifierType, documentClick);
                executeProper(ctx, url, method, identifier, identifierType, documentClick, redirectedUrlClick, elementsClick, dirs);
            }
        }
    }

    static boolean nonStructuralMethods(Context ctx, Dirs dirs) {
        if (ctx.primaryDatesplit()) {
            DateSplitParser.make(ctx, dirs);
            return true;
        }
        if (ctx.primaryBreaksplit()) {
            parseBreak(ctx, dirs.getBaseDirs());
            return true;
        }
        if (!Util.empty(ctx.primaryStatic())) {
            parseStatic(ctx);
            return true;
        }
        if (ctx.isFestival()) {
            parseFestival(ctx);
            return true;
        }
        if (!Util.empty(ctx.primaryFestivalDate())) {
            parseStaticBasic(ctx);
            return true;
        }
        return false;
    }

    static void parseStatic(Context ctx) {
        if (Util.empty(ctx.timeManual())) return;
        List<Calendar.CalendarDate> dates = list();
        if (Util.empty(ctx.staticDay())) {
            List<Calendar.CalendarDate> datesx = Calendar.nextWeekdays(1);
            dates.addAll(datesx);
        } else {
            for (String day : ctx.staticDay()) {
                List<Calendar.CalendarDate> datesx = Calendar.nextWeekdays(day.toLowerCase(), 2);
                dates.addAll(datesx);
            }
        }
        List<Time> times = TimeMatcher.matchSingle(new Util.StringMutable(ctx.timeManual()), ctx.lang, ctx.time);
        if (Util.empty(times)) return;
        Event event = new Event(ctx.ref);
        event.name = ctx.primaryStatic();
        event.originalText = ctx.primaryStatic();
        Util.Url url = Util.get(ctx.urls(), 0);
        event.link = url.url;
        event.source = url.url;
        for (Calendar.CalendarDate date : dates) {
            Calendar.Date key = date.toDate();
            event.dateTimes.put(key, times.get(0));
            event.dateUrl.put(key, url.url);
        }
        REF_EVENTS.put(ctx.ref, event);
    }

    static void parseStaticBasic(Context ctx) {
        String dateStatic = ctx.primaryFestivalDate();
        if (Util.empty(dateStatic)) return;
        List<Calendar.Date> dates = DateMatcher.match(new Util.StringMutable(dateStatic), ctx.lang, ctx.date);
        if (Util.empty(dates)) return;
        List<Time> times = TimeMatcher.matchSingle(new Util.StringMutable(""), ctx.lang, ctx.time);
        if (Util.empty(times)) return;
        Event event = new Event(ctx.ref);
        event.name = ctx.ref;
        event.originalText = ctx.ref;
        Util.Url url = Util.get(ctx.urls(), 0);
        event.link = url.url;
        event.source = url.url;
        for (Calendar.Date key : dates) {
            event.dateTimes.put(key, times.get(0));
            event.dateUrl.put(key, url.url);
        }
        REF_EVENTS.put(ctx.ref, event);
    }

    static void parseBreak(Context ctx, BaseDirs dirs) {
        for (Util.Url url : ctx.urls()) {
            Document doc = WebReader.Download.documentOnly(ctx, url.url, dirs, true, false);
            for (String splitBreak : Util.splitList(Jutil.textOnly(doc), "\u2021")) {
                Util.StringMutable clean = new Util.StringMutable(splitBreak.toLowerCase());
                List<Calendar.Date> dates = DateMatcher.match(clean, ctx.lang, ctx.date);
                if (Util.empty(dates)) continue;
                Event event = Parse.eventManufacture(ctx, url.url, clean.string, dates);
                REF_EVENTS.put(ctx.ref, event);
            }
        }
    }

    static void parseFestival(Context ctx) {
        String dateStatic = ctx.primaryFestivalDate();
        if (Util.empty(dateStatic)) return;
        List<Calendar.Date> dates = DateMatcher.match(new Util.StringMutable(dateStatic), ctx.lang, ctx.date);
        if (Util.empty(dates)) return;
        List<Time> times = TimeMatcher.matchSingle(new Util.StringMutable("19:30"), ctx.lang, ctx.time);
        if (Util.empty(times)) return;
        Event event = new Event(ctx.ref);
        event.name = ctx.ref;
        event.originalText = ctx.ref;
        Util.Url url = Util.get(ctx.urls(), 0);
        event.link = url.url;
        event.source = url.url;
        for (Calendar.Date key : dates) {
            event.dateTimes.put(key, times.get(0));
            event.dateUrl.put(key, url.url);
        }
        REF_EVENTS.put(ctx.ref, event);
    }

    static void executeProper(Context ctx, Util.Url url, String method, String identifier, String identifierType, Element doc, String redirectedUrl, List<Element> elements, Dirs dirs) {
        Util.MultiList<Integer, Element> indexElements = null;
        Util.MultiList<Integer, Element> indexElementMy = null;
        Util.MultiList<Integer, Calendar.Date> indexMy = null;
        if (list("Q", "P").contains(method)) {
            if (!Util.empty(ctx.partialTag())) {
                indexElementMy = Jutil.indexElementByTag(doc, ctx.partialTag());
            } else if (!Util.empty(ctx.partialClass())) {
                indexElementMy = Jutil.indexElement(doc, ctx.partialClass());
            }
            if (indexElementMy != null) {
                indexMy = new Util.MultiList<>();
                for (Util.Multi<Integer, Element> indexElement : indexElementMy.underlying) {
                    List<Calendar.Date> monthYear;
                    String text = Util.safeA(Jutil.text(indexElement.b));
                    if (ctx.partialEntire()) {
                        monthYear = DateMatcher.matchSingle(Util.lowercase(text));
                    } else {
                        monthYear = DateMatcher.matchMonth(text, ctx.lang);
                    }
                    indexMy.add(new Util.Multi<>(indexElement.a, Util.get(monthYear, 0)));
                }
                if ("TAG".equals(identifierType)) {
                    indexElements = Jutil.indexElementByTag(doc, identifier);
                } else {
                    indexElements = Jutil.indexElement(doc, identifier);
                }
            } else {
                sout(" UNABLE TO IDENTIFY any MY Elements");
            }
        }
        switch (method) {
            case "L":
                ListParser.make(url.url, ctx, elements, redirectedUrl, dirs.getBaseDirs());
                break;
            case "Q":
                if (indexElementMy != null) {
                    TableParser.make(url.url, ctx, elements, indexElements, indexMy, dirs.getBaseDirs());
                }
                break;
            case "T":
                TableParser.make(url.url, ctx, elements, null, null, dirs.getBaseDirs());
                break;
            case "P":
                if (indexElementMy != null) {
                    ClassParser.make(url, ctx, elements, redirectedUrl, indexElements, indexMy, dirs);
                }
                break;
            case "C":
                ClassParser.make(url, ctx, elements, redirectedUrl, null, null, dirs);
                break;
            default:
                break;
        }
    }
}