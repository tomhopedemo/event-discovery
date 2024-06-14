package com.arcta.events;
import com.arcta.events.Calendar.Date;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.List;
import static com.arcta.events.Primary.REF_EVENTS;
import static com.arcta.events.Util.*;
import static com.arcta.events.Util.sout;
class ClassParser {
    static void make(Util.Url url, Context ctx, List<Element> elements, String redirectedUrl, Util.MultiList<Integer, Element> indexMap, Util.MultiList<Integer, Date> indexMy, Dirs dirs) {
        Multi3<List<String>, List<String>, Multi> exclusions = Parse.exclusions(ctx); sout("CLASS_METHODOLOGY running for " + size(elements) + " elements");
        for (Element element : elements) { Date date = elementParse(url, ctx, elements, redirectedUrl, indexMap, indexMy, exclusions, element, dirs);
            if (islistCut(ctx, date)) break;}}
    static boolean islistCut(Context ctx, Date date) {
        if (ctx.primaryListcut() && date != null && Calendar.CalendarDate.diffFromCurrent(date) > 90) {sout("LIST CUT reached 3 month point");return true;}
        return false;}
    static Date elementParse(Util.Url url, Context ctx, List<Element> elements, String redirectedUrl, Util.MultiList<Integer, Element> indexMap, Util.MultiList<Integer, Date> indexMy, Multi3<List<String>, List<String>, Multi> exc, Element element, Dirs dirs) {
        if (pairRequiredButNotFound(ctx, element)) return null;         //INITIAL FILTERS
        if (pairExcluded(ctx, element)) return null;         //Text
        Multi<String, Util.MultiList<Integer, Element>> textHtml = Jutil.text(element, exc.b, exc.a, exc.c, null, null, null, null, ctx.parseGap(), ctx.parseTagGap());
        String original = safeA(textHtml);
        String text = original;
        if (creq0(ctx, text)) return null;
        Multi<String,String> linkText = Link.make(redirectedUrl, elements, element, ctx);         //Link
        String link = safeA(linkText);
        if (ctx.parseBlank() && ctx.parseTitleUrl()){ original = titleUrlLogic(link);
            text = original;
            if (text == null) { sout(link + "\nFILTERED ON TEXT NULL"); return null;}}
        text = lowercase(text);
        if (empty(text) && !ctx.parseBlank()) return null;
        if (xwc(ctx, element)) return null;
        if (reqclass(ctx, element)) return null;
        if (creq(ctx, text)) return null;
        if (creq2(ctx, text)) return null;
        if (cexc(ctx, text)) return null;
        StringMutable clean = new StringMutable(text);
        List<Date> dates;         //DATES
        if (!empty(indexMap) && !empty(indexMy)) { Integer elementIndex = indexMap.getaOne(element);
            Date dateMy = interpolateBefore(indexMy, elementIndex);
            List<Date> dateDay = DateMatcher.matchDay(clean, ctx.lang, ctx.date);
            Date date = ctx.partialEntire() ? dateMy : Date.merge(dateMy, get(dateDay, 0));
            if (date == null) {sout("FILTERED ON DATE NULL"); return null;}
            dates = list(date);
        } else if (ctx.urlDate()) { dates = list(url.date);
        } else {
            dates = Parse.matchDates(ctx, clean);
            if (empty(dates) && !ctx.parseBlank()) {sout("FILTERED ON DATES EMPTY"); return null;}}
        Date fiveWeeksLater = Calendar.get(Calendar.indexOfCurrent() + 35).toDate();
        if (!Util.empty(dates)){ boolean earlyDateFound = false;
            for (Date date : dates) {
              if (date.compareTo(fiveWeeksLater) == -1){
                earlyDateFound = true;
                break;}}
            if (!earlyDateFound) {sout("FILTERED ON LATE DATE"); return null;}}
        if (ctx.parseTitleUrl()) { original = titleUrlLogic(link);
            clean.string = original;}
        if (blank(ctx, clean, "1")) return null;
        List<Element> secondLevelElements = getSecondLevelElements(ctx, element);         //SECOND LEVEL ELEMENTS
        if (secondLevelElements != null){
            for (Element secondLevelElement : secondLevelElements) { secondLevel(url, ctx, redirectedUrl, exc, dates, secondLevelElements, secondLevelElement, dirs);} return null;
        } else { Event event = constructEvent(ctx, clean, link, dates, safeB(linkText), original, url.url, element, redirectedUrl, dirs); if (event == null) return null;
            REF_EVENTS.put(ctx.ref, event);
            return getFirst(event.dateTimes.keySet());}}
    static List<Element> getSecondLevelElements(Context ctx, Element element) { List<Element> secondLevelElements = null;
        if (ctx.parseTimesplit()){ secondLevelElements = timesplitElements(ctx, element);
        } else if (ctx.parseCsub() != null){ secondLevelElements = element.getElementsByClass(ctx.parseCsub());} return secondLevelElements;}
    static void secondLevel(Util.Url url, Context ctx, String redirectedUrl, Multi3<List<String>, List<String>, Multi> exc, List<Date> dates, List<Element> secondLevelElements, Element secondLevelElement, Dirs dirs) {
        Multi<String, Util.MultiList<Integer, Element>> textHtmlX = Jutil.text(secondLevelElement, exc.b, exc.a, exc.c, null, null, null, null, null, null);
        String textX = safeA(textHtmlX); if (empty(textX)) return;
        StringMutable cleanX = new StringMutable(textX.toLowerCase());
        Multi<String,String> linkTextX = Link.make(redirectedUrl, secondLevelElements, secondLevelElement, ctx);
        List<Date> csubdates;
        if (ctx.parseCsubx()) { csubdates = Parse.matchDates(ctx, cleanX);
            if (csubdates ==null) return;
        } else { csubdates = Date.cloneMe(dates);}
        Event event = constructEvent(ctx, cleanX, safeA(linkTextX), csubdates, safeB(linkTextX), textX, url.url, secondLevelElement, redirectedUrl, dirs);
        if (event == null) return;
        REF_EVENTS.put(ctx.ref, event);}
    static boolean cexc(Context ctx, String text) { List<String> cexc = ctx.classExcText();
        if (!empty(cexc) && contains(text.toLowerCase(), lowercase(cexc))) { sout("FILTERED ON CEXC"); return true;}
        return false;}
    static boolean creq2(Context ctx, String text) { List<String> creq2 = ctx.classReqText2();
        if (!empty(creq2) && !contains(text.toLowerCase(), lowercase(creq2))) { sout("FILTERED ON CREQ2"); return true;}
        return false;}
    static boolean creq(Context ctx, String text) { List<String> creq = ctx.classReqtext();
        if (!empty(creq) && !contains(text.toLowerCase(), lowercase(creq))) { sout("FILTERED ON CREQ"); return true;}
        return false;}
    static boolean reqclass(Context ctx, Element element) { String reqclass = ctx.classReqclass();
        if (!empty(reqclass) && !Jutil.hasClass(element, reqclass)) { sout("FILTERED ON REQCLASS"); return true;}
        return false;}
    static boolean xwc(Context ctx, Element element) { String excWithClass = ctx.classExcWithClass();
        if (!empty(excWithClass) && Jutil.hasClass(element, excWithClass)) { sout("FILTERED ON XWC"); return true;}
        return false;}
    static boolean creq0(Context ctx, String text) { List<String> creq0 = ctx.classReqtext0();
        if (!empty(creq0) && !contains(text.toLowerCase(), lowercase(creq0))) {sout("FILTERED ON CREQ0"); return true;}
        return false;}
    static boolean pairExcluded(Context ctx, Element element) { Multi<String,String> excpair = ctx.classExcpair();
        if (!empty(excpair) && identifyPair(excpair, element)) {sout("FILTERED ON EXCPAIR"); return true;}
        return false;}
    static boolean pairRequiredButNotFound(Context ctx, Element element) { Multi<String,String> reqpair = ctx.classReqpair();
        if (!empty(reqpair) && !identifyPair(reqpair, element)) {sout("FILTERED ON REQPAIR"); return true;}
        return false;}
    static List<Element> timesplitElements(Context ctx, Element parent){ List<Element> timeSplitChildren = list();
        for (Element child : parent.children()) { String text = child.text(); if (empty(text)) continue;
            Time time = TimeMatcher.match(new StringMutable(text.toLowerCase()), ctx.lang, ctx.time);
            if (time != null) timeSplitChildren.add(child);} return timeSplitChildren;}
    static boolean identifyPair(Multi<String,String> multi, Element element) {
        for (Element subclass : element.getElementsByClass(multi.a)) { String text = subclass.text();
            if (text == null) continue;
            if (containsIgnoreCase(text, multi.b)) return true;}
        return false;}
    static Event constructEvent(Context ctx, StringMutable clean, String link, List<Date> dates, String linkText, String original, String source, Element element, String redirectedUrl, Dirs dirs){
        if (almostBlank(ctx, clean, "1")) return null;
        Time time = Parse.getTime(ctx, clean, linkText);         //TIME
        String septext = Parse.septext(ctx, element);
        if (almostBlank(ctx, clean, "2")) return null;
        Event event = new Event(ctx.ref);
        event.name = !empty(septext) ? septext : clean.string;
        parseNoTitle(ctx, link, event);
        boolean pass = Context.performLocreq(event, ctx);if (!pass) {return null;}
        if (ctx.parseAnchorId()){ String id = element.id();
            if (!empty(id)) link = source + "#" + id;}
        if (ctx.parseAnchorName()){ Elements linkElements = element.getElementsByTag("a");
            for (Element aTags : linkElements) { String name = aTags.attr("name");
                if (!empty(name)){ link = source + "#" + name; break;}}}
        event.link = link != null ? link : source;
        event.originalText = original;
        event.source = source;
        if (ctx.link1Force()) {event.dateTimes = EventLink.make(link, ctx, dates, dirs.getBaseDirs());
        } else if (!empty(ctx.link1Req()) && reqlink(ctx, link, ctx.link1Req(), dirs.getBaseDirs())) {sout("FILTERED ON REQLINK"); return null;
        } else if (!empty(ctx.timeSkip()) && time == null) { Time timeSkiptime = TimeMatcher.matchSingle(lowercase(ctx.timeSkip()), false, ctx.lang, ctx.time);
            dates.forEach(d -> event.dateTimes.put(d, timeSkiptime));
        } else if (time == null && link != null && !ctx.parseNolink()) { event.dateTimes = EventLink.make(link, ctx, dates, dirs.getBaseDirs());}
        if (event.dateTimes == null) event.dateTimes = map();
        if (empty(event.dateTimes) && !empty(ctx.timeManual())){
            Time timeManual = TimeMatcher.matchSingle(lowercase(ctx.timeManual()), false, ctx.lang, ctx.time);
            for (Date date : dates) { event.dateTimes.put(date, timeManual);}}
        if (empty(event.dateTimes) && time != null){
            if (event.dateTimes == null) event.dateTimes = map();
            for (Date date : safeNull(dates)) { if (date == null) continue;
                event.dateTimes.put(date, time);}}
        if (empty(event.dateTimes) && time == null && size(dates) == 1){
            if (event.name.toLowerCase().contains("a day of ")){ event.dateTimes = map();
                event.dateTimes.put(dates.get(0), Time.getStartDayTime());}}
        if (empty(dates) && empty(event.dateTimes)) {sout("FILTERED ON DATETIMES"); return null;}
        if (!empty(event.dateTimes)) {
            for (Date date : event.dateTimes.keySet()) { event.dateUrl.put(date, event.link);}} return event;}
    static void parseNoTitle(Context ctx, String link, Event event) {
        if (ctx.parseNotitle()){ Multi3<Document, String,String> multi = WebReader.readParse(link);
            Document document = WebUtils.safeA(multi);
            if (document != null) { Elements titles = document.getElementsByTag("title");
                if (!empty(titles)) {event.name = titles.get(0).text();}}}}
    static boolean almostBlank(Context ctx, StringMutable clean, String id) {
        if (Parse.check(clean.string) && !ctx.parseBlank()) {sout("FILTERED ON STRING NULL " + id); return true;}
        return false;}
    static boolean blank(Context ctx, StringMutable clean, String id) {
        if (empty(clean) && !ctx.parseBlank()) {sout("FILTERED ON CLEAN NULL " + id); return true;}
        return false;}
    static String titleUrlLogic(String link) { if (empty(link)) return null;
        String urlToWorkOn = link;
        if (link.endsWith("/")){urlToWorkOn = substringRemoveLast(link);}
        urlToWorkOn = split(urlToWorkOn,"/").getLast(); return urlToWorkOn.replaceAll("\\-", " ");}
    static boolean reqlink(Context ctx, String link, String reqlink, BaseDirs dirs) {Document document = WebReader.Download.documentOnly(ctx, link, dirs, ctx.downloadDelay());
        if (document == null) return true;
        String text = lowercase(document.text()); if (empty(text)) return true;
        text = text.replaceAll("[\\s]{2,}", " ");
        return !text.contains(reqlink);}}