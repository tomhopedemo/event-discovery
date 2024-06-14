package com.arcta.events;
import java.util.*;
import java.util.regex.Pattern;
import static com.arcta.events.FiltersLists.*;
import static com.arcta.events.Util.map;
import static com.arcta.events.Weekdays.WEEKDAYS_LONG;
import static com.arcta.events.Weekdays.WEEKDAYS_ORDER;
import static com.arcta.events.M_Month.M_MONTH_ENG;
class Removals {
    static final List<String> CINEMA_INDICATIVE = Util.list("films");
    static final List<String> AND_WHITELIST = Util.list("and is phi"); //?
    static String cleanLonelyParenthesesTitle(String title) { if (Util.empty(title)) return title;
        int lcount = 0; int rcount = 0;
        for (char c : title.toCharArray()) { if (c == '(') lcount++;
            if (c == ')') rcount++;}
        if (lcount > rcount) { title = Util.removeLast("(", title);
        } else if (rcount > lcount) { title = title.replaceFirst("\\)", "");}
        return title;}
    static String cleanLonelySinglequotes(String title) { if (Util.empty(title)) return title;
        if (title.startsWith("'") && PhraseUtil.num("'", title) == 1) { title = title.replace("'", "");}
        return title;}
    static String removeExceptions(String title) { if (Util.empty(title)) return title;
        return title.replaceAll("\\((?i)except.*\\)", "").trim();}
    static String cleanLonelyDoublequotesTitle(String title) { if (Util.empty(title)) return title;
        if (PhraseUtil.num("\"", title) == 1) {title = title.replace("\"", "");}
        if (PhraseUtil.num("\u201d", title) == 1) {title = title.replace("\u201d", "");}
        return title;}
    static String cleanBracketPairs(String title) { if (Util.empty(title)) return title; return title.replaceAll("\\[\\s*\\]", "");}
    static String cleanBracketSpaces(String title) { if (title == null) return title;return title.replace(" )", ")").replace("( ", "(");}
    static String cleanSpecific(String title) { if (title == null) return title; return title.replaceAll("(?i)s0[0-9]e[0-1][0-9] ", "");}
    static String removalFilmRatings(String title) { if (title == null) return title;
        for (String filmIdentifier : Util.Film.filmRatings) { title = title.replace(filmIdentifier, "").trim();}
        return title;}
    static String cleanSundays(String title, String dayOfWeek) {
        if ("sun".equalsIgnoreCase(dayOfWeek)) {
            if (title.toLowerCase().startsWith("sunday ")) {title = title.substring(6).trim();}}
        return title;}
    static String obscenities(String title) { if (title == null) return title;
        for (String obscenity : obscenities) { String removed = PhraseUtil.removeWordSafely(title, obscenity);
            if (!removed.equals(title)) return title;}
        return title;}
    static void removeUrls(Util.StringMutable eventName) {eventName.string = eventName.string.replaceAll("https:\\S*", " - ");}
    static String removeAngleBracketText(String text){
        if (!text.startsWith("<")) return text;
        int i = text.indexOf(">");
        if (i == -1) return text;
        return text.substring(i + 1);}
    static String removeParts(String title, String ref, InputData inputData) { Util.NList split;
        String district = inputData.getDistrict(ref);
        if (!Util.empty(title)){ split = Util.split(title, " - ");
            List<Integer> indexesToRemove = Util.list();
            for (int i = 0; i < split.size(); i++) { String candidate = split.get(i);
                candidate = candidate.toLowerCase().trim();
                if (candidate.length() < 2){indexesToRemove.add(i);}
                if (candidate.matches(M_MONTH_ENG)){indexesToRemove.add(i);}
                if (SINGLE_PARTS_TO_REMOVE.contains(candidate)){indexesToRemove.add(i);}
                if (district != null) {
                    if (candidate.equals(district.toLowerCase().trim())) {indexesToRemove.add(i);}}
                if (candidate.equals(inputData.getCity())){indexesToRemove.add(i);}}
            title = split.reconstructExcept(indexesToRemove);}
        return title;}
    static String characterEndRemovals(String fullTitle) {Util.NList split = Util.split(fullTitle, " - ");
        List<String> newConstruct = Util.list();
        for (int j = 0; j < split.size(); j++) { String title = split.get(j);
            title = removeAngleBracketText(title);
            title = Util.trim(title);
            List<String> characters = Util.union(Util.HYPHENS, Util.list(".", ",", ":", "\u2026", ")", "\u00A0", "|", "*"));
            for (int i = 0; i < 4; i++) {
                if (Util.startsWith(title, characters)) {title = Util.trim(Util.substringRemoveFirst(title));
                } else { break;}}
            for (int i = 0; i < 2; i++) {
                if (Util.endsWith(title, Util.union(Util.HYPHENS, Util.list("-", ":", ",", "@", "<", "(", "|", "&", ".", "\\","/", "!", "\u2026")))) { title = Util.trim(Util.substringRemoveLast(title));
                } else { break;}}
            if (title.endsWith("'") && title.startsWith("'")){ title = Util.trim(Util.substringRemoveFirst(Util.substringRemoveLast(title)));}
            if (title.startsWith("(") && title.endsWith(")")) {title = Util.substringRemoveLast(Util.substringRemoveFirst(title));}
            if (title.endsWith(")") && !title.contains("(")) {title = Util.substringRemoveLast(title);}
            if (title.startsWith("(") && !title.contains(")")) {title = Util.substringRemoveFirst(title);}
            newConstruct.add(title);}
        return Util.string(newConstruct," - ");}
    static void removePresents(Util.StringMutable clean, Context ctx){ if (ctx.tertiaryPresents()) return;
        clean.string = clean.string.replaceAll(" (?i)presents\\u2026", " presents\u2026 ");
        List<String> presents = Util.list("presents", "present", "pres.", "pres:", "present\u2026", "presents\u2026", "conducts");
        List<String> presentColon = Util.list("presents:", "present:");
        Util.NList splitDash = Util.split(clean.string, " - ");
        for (int partIndex = 0; partIndex < splitDash.size(); partIndex++) { Util.NList split = Util.split(splitDash.get(partIndex),"\\s+");
            for (int index = 0; index < Math.min(11, split.size()); index++) {
                if (presentColon.contains(split.get(index).toLowerCase())){ String newPart = split.reconstruct(index + 1, split.size());
                    clean.string = splitDash.reconstructReplacing(partIndex, newPart);
                    return;}
                if (split.get(index).endsWith(":")){break;}
                if (presents.contains(split.get(index).toLowerCase())){ String newPart = split.reconstruct(index + 1, split.size());
                    clean.string = splitDash.reconstructReplacing(partIndex, newPart);
                    return;}}}}
    static void removePart(Util.StringMutable clean, Event event) { Util.NList split = Util.split(clean.string, " - ");
        List<Integer> indicesToRemove = Util.list();
        for (int i = 0; i < split.size(); i++) { String s = split.get(i);
            for (String part : SINGLE_PARTS_TO_REMOVE) {
                if (s.toLowerCase().trim().equals(part)) { indicesToRemove.add(i);
                    if (CINEMA_INDICATIVE.contains(part) && Util.empty(event.category)) {event.category = "cinema";}}}}
        clean.string = split.reconstructExcept(indicesToRemove);
        List<String> toAllow = Util.list("u2", "404");
        split = Util.split(clean.string, " - ");
        List<Integer> indexesToRemove = Util.list();
        for (int i = 0; i < split.size(); i++) { String s = split.get(i);
            if (toAllow.contains(Util.lowercase(s))) continue;
            if (s.matches("[^a-zA-Z]+")) indexesToRemove.add(i);
            if (s.matches("[a-zA-Z][^a-zA-Z]+")) indexesToRemove.add(i);}
        clean.string = split.reconstructExcept(indexesToRemove);}
    static void removeAfter(Util.StringMutable clean, Context ctx) { if (Util.empty(ctx.tertiaryRemAfter())) return;
        Util.NList split = Util.split(clean.string, ctx.tertiaryRemAfter());
        clean.string = split.get(0);}
    static void removeStartPart(Util.StringMutable clean) { Util.NList split = Util.split(clean.string, "-");
        for (String word : START_PARTS_TO_REMOVE) {
            if (word.equals(split.get(0).toLowerCase().trim())) { clean.string = split.reconstructExcept(0);
                break;}}}
    static void removeRepeatPart(Util.StringMutable clean) { Util.NList split = Util.split(clean.string, " - ");
        List<Integer> indexesToRemove = Util.list();
        for (int i = 1; i < split.size(); i++) {
            if (split.get(i).trim().toLowerCase().equals(split.get(i - 1).trim().toLowerCase())) { indexesToRemove.add(i);}}
        clean.string = split.reconstructExcept(indexesToRemove);}
    static void removeEndPart(Util.StringMutable clean, Context ctx) { Util.NList split;
        String endRem = ctx.tertiaryRemoveEnd();
        if (!Util.empty(endRem)) { split = Util.split(clean.string, "-");
            if (endRem.equals(split.getLast().toLowerCase().trim())) { clean.string = split.reconstructBeforeLast();}}}
    static void removeFromTextSafely(Util.StringMutable clean) { for (String phraseToRemove : PHRASES_TO_REMOVE_SAFELY) {clean.string = PhraseUtil.removeWordSafely(clean.string, phraseToRemove, true);}}
    static void removeFromText(Util.StringMutable clean, Context ctx) { List<String> combinedPhrasesToRemove = new ArrayList<>(PHRASES_TO_REMOVE);
        Util.addAll(combinedPhrasesToRemove, ctx.tertiaryRemove());
        for (String phraseToRemove : combinedPhrasesToRemove) {clean.string = clean.string.replaceAll("(?i)" + Pattern.quote(phraseToRemove), "");}
        clean.string = clean.string.replaceAll("(?i)" + "[0-9]{1,2}" + "(th|rd|nd) anniversary", "");
        clean.string = clean.string.trim();}
    static void removeStart(Util.StringMutable clean, Context ctx) { List<String> phrasesToClean = new ArrayList<>(PHRASES_TO_CLEAN_FROM_START_OF_TEXT);
        List<String> remstart = ctx.tertiaryRemoveStart();
        if (!Util.empty(remstart)) Util.addAll(phrasesToClean, remstart);
        for (String phrase : phrasesToClean) {
            if (clean.string.toLowerCase().startsWith(phrase + " ") || clean.string.toLowerCase().startsWith(phrase + ": ")) {
                if (Util.startsWith(clean.string.toLowerCase(), AND_WHITELIST)) continue;
                clean.string = clean.substring(phrase.length()).trim();
                break;}}}
    static void removeWeekdays(Util.StringMutable clean, Context ctx) {if (clean.string == null) return;
        if (!ctx.remRemDays()) return;
        for (String weekday : WEEKDAYS_LONG) { String s = clean.string.toLowerCase();
            if (s.startsWith(weekday + "'s ") || s.startsWith(weekday + "\u2019s ")) {clean.string = clean.string.substring(weekday.length() + 3);}}}
    static void removeWeekday(Util.StringMutable clean, Context ctx, Set<Calendar.Date> weekdays) {if (clean.string == null) return;
        if (!ctx.remRemDay()) return;
        for (Calendar.Date day : weekdays) { Calendar.setDayOfWeek(day);
            String weekday = WEEKDAYS_LONG.get(WEEKDAYS_ORDER.indexOf(day.dayOfWeek));
            clean.string = PhraseUtil.removeWordSafely(clean.string, weekday, true);}}
    static void removeHtml(Util.StringMutable clean) {
        if (!Util.empty(clean.string)) {clean.string = Util.split(clean.string, "\\<img").get(0);}
        if (!Util.empty(clean.string)) { clean.string = Util.split(clean.string, "\\<p\\>").get(0);}
        if (clean.string == null) clean.string = "";}
    static void removePrices(Util.StringMutable clean) {
         clean.string = clean.string.replaceAll("(from )?" + "£[0-9]{1,2}", "");
         clean.string = clean.string.trim();}
    static void removeFromEndsOfParts(Util.StringMutable clean, List<String> removals) {removeFromEndsOfParts(clean, removals, -1);}
    static void removeFromEndsOfParts(Util.StringMutable clean, List<String> removals, int partminchars) {         Map<Integer, String> map = map(); Util.NList split = Util.split(clean.string, " - ");
        for (int i = 0; i < split.size(); i++) { String s = split.get(i);
            if (s.length() > partminchars) {
                for (String word : removals) {
                    if (s.toLowerCase().trim().endsWith(" " + word)) {s = s.substring(0, s.length() - word.length()).trim();}}}
            map.put(i, s);}
        clean.string = split.reconstructReplacing(map);}
    static void cleanUrls(Util.StringMutable clean){ if (Util.empty(clean)) return;
        clean.string = clean.string.toLowerCase().replaceAll("http\\S* ", "");}
    static void stripSpecialFromStart(Util.StringMutable eventName) {
        for (int i = 0; i < 20; i++) {
            if (eventName.string.startsWith("\u2021 ")) { eventName.string = eventName.string.substring(2);
            } else { break;}}}}