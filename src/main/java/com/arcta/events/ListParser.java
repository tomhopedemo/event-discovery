package com.arcta.events;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

import static com.arcta.events.Primary.REF_EVENTS;
import static com.arcta.events.Util.list;

class ListParser {
    static void make(String url, Context ctx, List<Element> elements, String redirectedUrl, BaseDirs dirs) {
        Util.Multi3<List<String>, List<String>, Util.Multi> exc = Parse.exclusions(ctx);
        for (Element element : elements) {
            if (element == null) continue;
            Element supertagElement = Jutil.childElementByTag(element, "ul");
            if (supertagElement == null) supertagElement = Jutil.childElementByTag(element, "ol");
            if (supertagElement == null && ("ul".equals(element.tagName()) || "ol".equals(element.tagName()))) supertagElement = element;
            String textDate = element.getAllElements().get(0).text();            // -------------- DATES ------ (CURRENTLY ONE DATE FOR ALL LIST ELEMENTS) --------------
            String subsentence = PhraseUtil.subSentence(textDate.toLowerCase(), 30);
            List<Calendar.Date> overallDates = DateMatcher.match(new Util.StringMutable(subsentence), ctx.lang, ctx.date);
            Calendar.Date monthYear = null;
            if (Util.empty(overallDates)) {
                monthYear = Util.get(DateMatcher.matchMonthYear(subsentence), 0);
            }
            List<Element> lis = Jutil.childElementsByTag(supertagElement, "li");
            if (lis == null) continue;
            for (Element listElement : lis) {
                Util.Multi<String, Util.MultiList<Integer, Element>> textHtml = Jutil.text(listElement, exc.b, exc.a, exc.c, null, null, null, null, ctx.parseGap(), ctx.parseTagGap());
                String text = Util.safeA(textHtml);
                if (Util.empty(text)) continue;
                String originalText = text;
                text = text.toLowerCase();
                Util.StringMutable clean = new Util.StringMutable(text);
                List<Calendar.Date> dates = Parse.matchDates(ctx, clean);                 //first attempt match date in list element
                if (Util.empty(dates) && monthYear != null) {                 //second attempt match partial date in list element
                    List<Calendar.Date> dateDay = DateMatcher.matchDay(clean, ctx.lang, ctx.date);
                    Calendar.Date date = Calendar.Date.merge(monthYear, Util.get(dateDay, 0));
                    if (date != null) dates = list(date);
                }
                if (Util.empty(dates)) {                 //use backup
                    if (!ctx.parseBlank()) { //this mechanism is ever so slightly the wrong way around - but cleaner
                        if (Util.empty(overallDates)) continue;
                        dates = new ArrayList<>(overallDates);
                    }
                }
                Util.Multi<String, String> linkText = Link.make(redirectedUrl, elements, listElement, ctx);
                String link = Util.safeA(linkText);
                String textFromLink = Util.safeB(linkText);
                Time time = Parse.getTime(ctx, clean, textFromLink);
                String septext = Parse.septext(ctx, listElement);
                if (Parse.check(clean.string)) continue;
                Event event = new Event(ctx.ref);
                event.link = link != null ? link : url;
                event.name = !Util.empty(septext) ? septext : clean.string;
                event.originalText = originalText;
                event.source = url;
                if (time == null && link != null && !ctx.parseNolink()) {
                    event.dateTimes = EventLink.make(link, ctx, dates, dirs);
                } else {
                    dates.forEach(d -> event.dateTimes.put(d, time));
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