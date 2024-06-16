package com.arcta.events;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.arcta.events.Primary.REF_EVENTS;
import static com.arcta.events.Util.empty;
import static com.arcta.events.Util.map;

class DateSplitParser {
    static void make(Context ctx, Dirs dirs) {
        for (Util.Url url : ctx.urls()) {
            Document doc = WebReader.Download.documentOnly(ctx, url.url, dirs.getBaseDirs(), true);
            if (doc == null) return;
            make(ctx, url, doc, dirs);
        }
    }

    static void make(Context ctx, Util.Url url, Document doc, Dirs dirs) {
        Util.Multi3<List<String>, List<String>, Util.Multi> exclusions = Parse.exclusions(ctx);
        Util.Multi<String, Util.MultiList<Integer, Element>> textHtml = Jutil.text(doc, exclusions.b, exclusions.a, null, ctx.datesplitRestrictToTags(), ctx.datesplitRestrictToClasses(), null, null, ctx.parseGap(), null);
        String text = Util.safeA(textHtml);
        if (Util.empty(text) || text.length() > 100000) return;
        Util.MultiList<Integer, Element> indexElement = Util.safeB(textHtml);
        Util.sortA(indexElement);
        String aftertext = ctx.datesplitAfter();
        if (!Util.empty(aftertext)) {
            Util.NList split = Util.split(text, aftertext);
            if (split.size() > 1) {
                text = split.get(1);
            }
        }
        String beforetext = ctx.datesplitBefore();
        if (!Util.empty(beforetext)) {
            Util.NList split = Util.split(text, beforetext);
            text = split.get(0);
        }
        String originalText = text;
        text = text.toLowerCase();
        List<List<Calendar.Date>> dateLists;
        if (!Util.empty(ctx.partialTag())) {
            List<Calendar.Date> singledates = DateMatcher.matchNoOverlapsPartial(text, ctx.lang, ctx.date, url.date);
            if (Util.empty(singledates)) return;
            dateLists = Util.list();
            for (Calendar.Date singledate : singledates) {
                dateLists.add(Util.list(singledate));
            }
        } else {
            dateLists = DateMatcher.matchNoOverlaps(text, ctx.lang, ctx.date);
        }
        Util.MultiList<String, List<Calendar.Date>> splitTextDate = new Util.MultiList<>();
        Map<String, String> splitTextOriginal = map();
        Map<String, String> textHref = constructATextHrefMap(doc);
        for (int i = 0; i < dateLists.size(); i++) {
            List<Calendar.Date> dates = dateLists.get(i);
            Integer index1;
            Integer index2;
            Integer indexX;
            if (ctx.datesplitReverse()) {
                index1 = i == 0 ? 0 : dateLists.get(i - 1).get(0).indexPairs.get(0).b;
                indexX = index1; //unimplemented
                index2 = dates.get(0).indexPairs.get(0).b;
            } else {
                index1 = dates.get(0).indexPairs.get(0).a;
                indexX = dates.get(0).indexPairs.get(0).b;
                index2 = i == dateLists.size() - 1 ? text.length() : dateLists.get(i + 1).get(0).indexPairs.get(0).a;
            }
            String originalSubstring = Util.substring(index1, index2, originalText);
            String clean = Util.substring(indexX, index2, originalText);
            String substring = Util.substring(index1, index2, text);
            splitTextDate.add(new Util.Multi<>(substring, dates));
            splitTextOriginal.put(substring, originalSubstring);
            if (ctx.datesplitNoTime()) {
                Event event = Parse.eventManufacture(ctx, url.url, clean, dates);
                if (event == null) continue;
                String link = generateLink(index1, index2, indexElement, url.url);
                if (Util.empty(event.dateTimes) && link != null) {
                    if (!Util.empty(ctx.timeSkip())) {
                        Time timeSkiptime = TimeMatcher.matchSingle(Util.lowercase(ctx.timeSkip()), false, ctx.lang, ctx.time);
                        dates.forEach(d -> event.dateTimes.put(d, timeSkiptime));
                    } else if (!ctx.parseNolink()) {
                        event.dateTimes = EventLink.make(link, ctx, dates, dirs.getBaseDirs());
                    }
                }
                if (link != null) {
                    event.link = link;
                    Map<Calendar.Date, String> newDateUrlMap = map();
                    for (Calendar.Date date : Util.safeNull(event.dateUrl.keySet())) {
                        newDateUrlMap.put(date, event.link);
                    }
                    event.dateUrl = newDateUrlMap;
                }
                REF_EVENTS.put(ctx.ref, event);
            }
        }
        if (ctx.datesplitNoTime()) return;
        for (int j = 0; j < splitTextDate.size(); j++) {
            Util.Multi<String, List<Calendar.Date>> dateMulti = splitTextDate.underlying.get(j);
            String datePart = dateMulti.a;
            List<Time> times = TimeMatcher.matchNoOverlaps(datePart, ctx.lang, ctx.time);
            for (int i = -1; i < times.size(); i++) {
                Integer index1 = i == -1 ? 0 : times.get(i).startIndex;
                Integer index2 = i == times.size() - 1 ? datePart.length() : times.get(i + 1).startIndex;
                String originalSubstring = splitTextOriginal.get(datePart).substring(index1, index2);
                Event event = Parse.eventManufacture(ctx, url.url, originalSubstring, dateMulti.b);
                if (event == null) continue;
                String urlIndicative = generateLink(textHref, originalSubstring, url.url);
                if (urlIndicative != null) {
                    event.link = urlIndicative;
                    Set<Calendar.Date> keys = Util.safeNull(event.dateUrl.keySet());
                    for (Calendar.Date date : keys) {
                        event.dateUrl.put(date, urlIndicative);
                    }
                }
                REF_EVENTS.put(ctx.ref, event);
            }
        }
    }

    static Map<String, String> constructATextHrefMap(Element doc) {
        if (doc == null) return null;
        Map<String, String> map = map();
        for (Element element : doc.getElementsByTag("a")) {
            String hrefText = element.text();
            String href = element.attr("href");
            if (empty(href) || empty(hrefText)) continue;
            map.put(hrefText, href);
        }
        return map;
    }

    static String generateLink(Map<String, String> textHref, String originalSubstring, String url) {
        if (textHref != null) {
            String longestMatch = null;
            for (String textlet : textHref.keySet()) {
                if (Util.contains(textHref.get(textlet), Link.BAD_HREF)) continue;
                if (Util.endsWith(textHref.get(textlet), Link.BAD_HREF_ENDINGS)) continue;
                if (!originalSubstring.toLowerCase().contains(textlet.toLowerCase())) continue;
                if (longestMatch == null) {
                    longestMatch = textlet;
                } else if (longestMatch.length() < textlet.length()) {
                    longestMatch = textlet;
                }
            }
            if (longestMatch != null) {
                String href = textHref.get(longestMatch);
                return WebReader.UriExtension.toFullUrl(url, href);
            }
        }
        return null;
    }

    static String generateLink(Integer index1, Integer index2, Util.MultiList<Integer, Element> indexHrefs, String url) {
        Util.CountMap<Element> hrefCounts = new Util.CountMap<>();
        for (Util.Multi<Integer, Element> indexHref : indexHrefs.underlying) {
            if (Util.between(indexHref.a, index1, index2)) {
                Element b = indexHref.b;
                if (b != null && b.tag() != null) {
                    if ("a".equals(b.tag().toString())) {
                        hrefCounts.add(indexHref.b);
                    }
                }
            }
        }
        hrefCounts.greaterThan(1);
        if (hrefCounts.map.size() > 0) {
            String href = hrefCounts.getLargest().attr("href");
            return WebReader.UriExtension.toFullUrl(url, href);
        }
        return null;
    }
}