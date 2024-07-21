package com.events;

import com.events.date.Calendar;
import com.events.date.DateMatcher;
import com.events.date.Time;
import org.jsoup.nodes.Element;

import java.util.*;

import static com.events.Primary.REF_EVENTS;
import static com.events.Util.*;

class TableParser {
    static void make(String url, Context ctx, List<Element> elements, Util.MultiList<Integer, Element> indexElements, Util.MultiList<Integer, Calendar.Date> indexMy, BaseDirs dirs) {
        boolean foundTable = false;
        Map<Element, String> elementText = new HashMap<>();
        for (Element element : elements) {
            if ("table".equals(element.tagName())) {
                foundTable = true;
                elementText.put(element, element.text());
            }
        }
        if (foundTable) {
            tableMethodology(url, ctx, elementText, indexElements, indexMy, dirs);
        } else {
            for (Element element : elements) {
                Element table = Jutil.childElementByTag(element, "table");
                if (table != null) {
                    foundTable = true;
                    elementText.put(table, element.text());
                }
            }
            if (foundTable) {
                tableMethodology(url, ctx, elementText, indexElements, indexMy, dirs);
            }
        }
    }

    static void tableMethodology(String url, Context ctx, Map<Element, String> elementText, Util.MultiList<Integer, Element> indexElements, Util.MultiList<Integer, Calendar.Date> indexMy, BaseDirs dirs) {
        for (Element element : elementText.keySet()) {
            Element tbody = Jutil.childElementByTag(element, "tbody");
            if (tbody == null) continue;
            String textDate = elementText.get(element);             // -------------- TBODY DATES --------------------
            Collection<Calendar.Date> tbodyDates = DateMatcher.match(new Util.StringMutable(PhraseUtil.subSentence(textDate.toLowerCase(), 30)), ctx.lang, ctx.date);
            for (Element rowElement : Jutil.childElementsByTag(tbody, "tr")) {
                Util.Multi<String, Util.MultiList<Integer, Element>> textHtml = Jutil.text(rowElement, null, null, null, null, null, null, null, ctx.parseGap(), ctx.parseTagGap());
                String text = Util.safeA(textHtml);
                if (empty(text)) continue;
                String originalText = text;
                text = text.toLowerCase();
                Util.StringMutable clean = new Util.StringMutable(text);
                List<Calendar.Date> dates = Parse.matchDates(ctx, clean);
                if (Util.empty(dates)) {
                    if (!Util.empty(indexElements) && !Util.empty(indexMy)) {
                        Calendar.Date dateMy = Util.interpolateBefore(indexMy, indexElements.getaOne(rowElement));
                        List<Calendar.Date> dateDay = DateMatcher.matchDay(clean, ctx.lang, ctx.date);
                        Calendar.Date date = Calendar.Date.merge(dateMy, Util.get(dateDay, 0));
                        if (date == null) continue;
                        dates = list(date);
                    } else {
                        if (Util.empty(tbodyDates)) continue;
                        dates = new ArrayList<>(tbodyDates);
                    }
                }
                Util.Multi<String, String> linkText = Link.make(url, null, rowElement, ctx);
                String link = Util.safeA(linkText);
                Time time = Parse.getTime(ctx, clean, Util.safeB(linkText));
                if (Parse.check(clean.string)) continue;
                Event event = new Event(ctx.ref);
                event.link = link != null ? link : url;
                event.name = clean.string;
                event.originalText = originalText;
                event.source = url;
                if (time == null && link != null && !ctx.parseNolink()) {
                    event.dateTimes = EventLink.make(link, ctx, dates, dirs);
                } else if (time != null) {
                    dates.forEach(d -> event.dateTimes.put(d, time));
                } else {
                    sout("FILTERED ON TABLE DATETIMES");
                    continue;
                }
                if (!Util.empty(event.dateTimes)) {
                    for (Calendar.Date date : event.dateTimes.keySet()) {
                        event.dateUrl.put(date, event.link);
                    }
                }
                REF_EVENTS.put(ctx.ref, event);
            }
        }
    }
}