package com.arcta.events;
import java.util.List;
import java.util.Map;
import static com.arcta.events.Util.list;
class Merger {
    static void merge(List<Event> events, Event event){
        if (!Util.empty(events)) {
            if (mergeProperSubstring(events, event)) {return;}
            if (!event.source.equals(event.link)) {
                if (mergeLinkAndPhrase(events, event)) {return;}
                if (mergeLinkWords(events, event)) {return;}}}
        events.add(event);}
    static void merge(String output, Event existing, Event newEvent){ //DATE URLS
        if (Util.empty(existing.dateUrl)) {
            for (Calendar.Date date : existing.dateTimes.keySet()) {
                existing.dateUrl.putIfAbsent(date, newEvent.link);
            }
            Util.putIfAbsent(existing.dateUrl, newEvent.dateUrl);}
        if (!Util.empty(newEvent.dateUrl)) {
            Util.putIfAbsent(existing.dateUrl, newEvent.dateUrl);
        } else {
            for (Calendar.Date date : newEvent.dateTimes.keySet()) { existing.dateUrl.putIfAbsent(date, newEvent.link);}}
        Util.putIfAbsent(existing.dateTimes, newEvent.dateTimes);
        if (existing.link == null) existing.link = newEvent.link;
        if (existing.link.contains("?") && !newEvent.link.contains("?")){existing.link = newEvent.link;}
        if (existing.link.contains("#") && !newEvent.link.contains("#")){existing.link = newEvent.link;}
        if (urlsSimilar(existing.link, newEvent.source)){existing.link = newEvent.link;}}
    static boolean mergeLinkAndPhrase(List<Event> events, Event event){
        for (Event existing : events) { if (!urlsEquivalent(existing.link, event.link)) continue;
            if (PhraseUtil.phraseIntersection(event.name.toLowerCase(), existing.name.toLowerCase(), 3, null) != null ) { merge("LINK + PHRASE " + event.link + " ", existing, event); return true;}}
        return false;}
    static boolean mergeProperSubstring(List<Event> events, Event event){ String newEventName = preMergeClean(event.name);
        for (Event existing : events) { String existingEventName = preMergeClean(existing.name);
            if (existingEventName.startsWith(newEventName)) { merge("SUBSTRING ", existing, event); return true;
            } else if (newEventName.startsWith(existingEventName)){ merge("SUBSTRING ", existing, event);
                existing.name = event.name; return true;}}
        return false;}
    static boolean mergeLinkWords(List<Event> events, Event event) {
        for (Event existing : events) { if (!event.link.equals(existing.link)) continue;
            if (existing.link.equals(existing.source)) continue;
            List<String> wordsEvent = PhraseUtil.wordList(event.name, 3);
            List<String> wordsExisting = PhraseUtil.wordList(existing.name, 3);
            if (Util.size(wordsEvent) < 2 || Util.size(wordsExisting) < 2) continue;
            boolean similar = similar(event.name, existing.name, 1);
            if (similar) { merge("LINK + WORDS ", existing, event); return true;}}
        return false;}
    static boolean urlsSimilar(String url, String url2){ return url.replaceAll("/$", "").equals(url2.replaceAll("/$", ""));}
    static boolean similar(String a, String b, int difference){ a = a.toLowerCase();
        b = b.toLowerCase();
        if (a.equals(b)) return true;
        List<String> wordsA = Util.split(a, " ").underlying;
        List<String> wordsB = Util.split(b, " ").underlying;
        int countAWordsNotInB = 0;
        for (String s : wordsA) {if (!wordsB.contains(s)) countAWordsNotInB++;}
        int countBWordsNotInA = 0;
        for (String s : wordsB) {if (!wordsA.contains(s)) countBWordsNotInA++;}
        int min = Math.min(countBWordsNotInA, countAWordsNotInB);
        return (min <= difference);}
    static String preMergeClean(String string){return string.toLowerCase().replace(":","").replace("\u2021"," ").trim();}
    static boolean urlsEquivalent(String a, String b){ if (a == null || b == null) return false;
        if (a.endsWith("/")) a = Util.substringRemoveLast(a);
        if (b.endsWith("/")) b = Util.substringRemoveLast(b);
        return a.equals(b);}
    static List<Event> getExactNameMerge(List<Event> localEvents, Map<String, String> filtered) { List<Event> mergedEvents = list();
        for (Event event : localEvents) { boolean merged = false;
            for (Event existing : mergedEvents) {
                if (event.name.toLowerCase().equals(existing.name.toLowerCase())) { merge("exactname", existing, event);
                    merged = true;
                    filtered.put(event.id, "NAME-MERGE"); break;}}
            if (!merged) {mergedEvents.add(event);}}
        return mergedEvents;}
    static void remerge(List<Event> localEvents, Event event) {
        remerge:{for (Event localEvent : localEvents) {                 // ----------------- merge same display + same date/times -----------------------
                if (localEvent.name.equals(event.name) && localEvent.dateTimes.equals(event.dateTimes)) {break remerge;}}
            localEvents.add(event);}}}