package com.arcta.events;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import static com.arcta.events.FiltersLists.*;
import static com.arcta.events.Util.*;
import static com.arcta.events.Weekdays.WEEKDAYS_LONG;
import static com.arcta.events.Weekdays.WEEKDAYS_ORDER;
import static com.arcta.events.M_Weekday.M_WEEKDAY_LONG;
class Output { static boolean WRITE_TO_SCREEN = false; static Map<String, String> filtered; static Map<Util.Multi<String, Calendar.Date>, String> filteredByDate;
    static void execute(List<String> refs, Dirs dirs, InputData inputData) { List<Event> events = list();sout("Output - executing " + Util.size(refs) + " refs");filtered = map();filteredByDate = map();
        for (String ref : refs) { if (Util.empty(ref)) continue;
            Context ctx = Context.make(ref, inputData);
            List<Event> loadedEvents = IntermediateData.getMergedEventList(ref, dirs);
            if (Util.empty(loadedEvents)) { sout("No events available for " + ref); continue; }
            if (refs.size() == 1) sout(loadedEvents.size() + " Raw Events");
            List<Event> localEvents = list();
            for (Event event : loadedEvents) { event.ctx = ctx;
                cleanA(event); // REPLICA WORDS / CURTAILMENT / UPPERCASING
                if (exclusions1(event)) continue; //DATE / TIME / PHRASES CONTAINED AND NOT CONTAINED
                event.name = cleanB(event.name, ref, inputData); //SYMBOLS CLEAN
                event.soldout = checkSoldOut(event);
                event.name = cleanSectionC(event, new StringMutable(event.name)); //REMOVALS + ELLIPSIS + BREAKS
                if (Exclude.startExclusions(event, filtered)) continue;
                event.name = cleanSectionFinal(event.name);
                if (exclusions4(ref, event)) continue; //TECHNICAL EXCLUSIONS + DESCRIPTION LENGTH + QUALITY
                if (event.dateUrl.values().contains(null)) { sout("date url values null"); continue; }
                Merger.remerge(localEvents, event);}
            List<Event> mergedEvents = Merger.getExactNameMerge(localEvents, filtered);
            events.addAll(mergedEvents);}
        List<Event> admittedEvents = list();
        for (Event event : events) { int totalDates = event.dateTimes.size();
            int shortRunningLimit = 10; int sameWeekdayLimit = 6; int highlyDistributedLimit = 5; int exhibitionLimit = 5;
            Calendar.setDayOfWeek(event.dateTimes.keySet());
            Calendar.setDayOfWeek(event.dateUrl.keySet());
            if (event.ctx.isFestival() || event.ctx.tertiaryExhibition()) { ArrayList<String> weekend = list("sat", "sun"); ArrayList<String> satOnly = list("sat");
                if (Util.size(event.dateTimes.entrySet()) < 2) { event.dateTimes.entrySet().removeIf(e -> !weekend.contains(e.getKey().dayOfWeek));
                    event.dateUrl.entrySet().removeIf(e -> !weekend.contains(e.getKey().dayOfWeek));
                } else { event.dateTimes.entrySet().removeIf(e -> !satOnly.contains(e.getKey().dayOfWeek));
                    event.dateUrl.entrySet().removeIf(e -> !satOnly.contains(e.getKey().dayOfWeek));}
                if (Util.empty(event.dateTimes)) continue;}
            String rday = event.ctx.tertiaryRestrictToDay();
            if (!Util.empty(rday)) { event.dateTimes.entrySet().forEach(e -> Calendar.setDayOfWeek(e.getKey()));
                event.dateTimes.entrySet().removeIf(e -> !rday.equalsIgnoreCase(e.getKey().dayOfWeek));}
            Set<String> weekdays = event.dateTimes.keySet().stream().map(d -> d.dayOfWeek).collect(Collectors.toSet());
            if (Theatre.isTheatre(event) && event.dateTimes.size() > 1) {cleanDoubleDateSpecialDates(event);}
            if (event.ctx.isFestival() || event.ctx.tertiaryExhibition()){ admittedEvents.add(event);
            } else if (totalDates > 60) { filtered.put(event.id, "LIMIT_60");
            } else if (totalDates > shortRunningLimit) {
                if (Theatre.isTheatre(event)) { admittedEvents.add(event);
                } else { filtered.put(event.id, "LIMIT_10");}
            } else if (totalDates > highlyDistributedLimit && (Util.safeNull(Calendar.Date.daysBetween(event.dateTimes.keySet())) > 25) && !Theatre.isTheatre(event)) {filtered.put(event.id, "DISTR_LIMIT");
            } else if (totalDates > sameWeekdayLimit && weekdays.size() == 1) {filtered.put(event.id, "WKDAY_LIMIT");
            } else if (totalDates > exhibitionLimit && PhraseUtil.subSentence(event.name.toLowerCase(), 5).contains("exhibition")) {filtered.put(event.id, "EXHIB_LIMIT");
            } else { admittedEvents.add(event);}}
        Util.MultiList<Multi<Calendar.Date, Time>, Event> dateTimeEvents = DateTimeFilters.filter(admittedEvents, filtered);
        dateTimeEvents.underlying = Util.distinct(dateTimeEvents.underlying);
        dateTimeEvents.underlying.removeIf(m -> m.a == null || m.a.a == null);
        dateTimeEvents.underlying.sort(new DateTimeComparator());
        removeRepetitions(dateTimeEvents);
        removeCrossReferences(dateTimeEvents);
        Util.MultiList<Multi<Calendar.Date, Time>, Multi<Event, Boolean>> dateTimeEventVisible = Theatre.markVisibleTheatrePerformances(dateTimeEvents, filtered);
        AppDataGenerator.generateAppData(dateTimeEventVisible, filtered, filteredByDate, dirs, inputData);}
    static boolean checkSoldOut(Event event) {return Util.contains(Util.lowercase(event.name), SOLDOUT_INDICATORS)|| Util.contains(Util.lowercase(event.originalText), SOLDOUT_INDICATORS);}
    static void removeRepetitions(Util.MultiList<Multi<Calendar.Date, Time>, Event> dateTimeEvents) {
        Map<Util.Multi<Calendar.Date, Time>, ArrayList<Event>> map = dateTimeEvents.map();
        for (Util.Multi<Calendar.Date, Time> dateTime : map.keySet()) { ArrayList<Event> events = map.get(dateTime); List<Event> removed = list();
            for (Event event : new ArrayList<>(events)) { if (removed.contains(event)) continue;
                String title = Util.split(event.name, " - ").get(0);
                for (Event candidate : Util.except(events, list(event))) {
                    String candidateTitle = Util.split(candidate.name, " - ").get(0);
                    if (event.organizer.equals(candidate.organizer) && title.equalsIgnoreCase(candidateTitle)) { dateTimeEvents.underlying.remove(new Util.Multi<>(dateTime, candidate));
                        filtered.put(candidate.id, "REPETITION");
                        events.remove(candidate);
                        removed.add(candidate);}}}}}
    static void removeCrossReferences(Util.MultiList<Multi<Calendar.Date, Time>, Event> dateTimeEvents) { Map<Util.Multi<Calendar.Date, Time>, ArrayList<Event>> map = dateTimeEvents.map();
        for (Util.Multi<Calendar.Date, Time> dateTime : map.keySet()) { ArrayList<Event> events = map.get(dateTime);
            external: for (Event event : new ArrayList<>(events)) {
                if (event.name.contains(" ")) { String cleansedName = event.name.replaceAll("(,|\\.|:|')", "").toLowerCase();
                    for (Event candidate : Util.except(new ArrayList<>(events), list(event))) { if (!candidate.name.contains(" ")) continue;
                        String candidateCleansedName = candidate.name.replaceAll("(,|\\.|:)", "").toLowerCase();
                        if (candidate.organizer.equals(event.organizer)) {
                            if (PhraseUtil.phraseIntersection(cleansedName, candidateCleansedName, 6, null) != null) {
                                sout("PHRASAL K MERGE: " + event.organizer + "\t" + cleansedName + "\n" + candidate.organizer + "\t" + candidateCleansedName);
                                dateTimeEvents.underlying.remove(new Util.Multi<>(dateTime, event));
                                events.remove(event); continue external;}
                        } else {
                            if (event.name.toLowerCase().contains(candidate.organizer.toLowerCase())) {
                                if (PhraseUtil.phraseIntersection(event.name.toLowerCase(), candidate.name.toLowerCase(), 2, null) != null) {
                                    dateTimeEvents.underlying.remove(new Util.Multi<>(dateTime, event));
                                    events.remove(event); continue external;
                                } else if (PhraseUtil.firstWord(event.name).equalsIgnoreCase(PhraseUtil.firstWord(candidate.name))) {
                                    if (PhraseUtil.firstWord(event.name).length() > 7) {
                                        dateTimeEvents.underlying.remove(new Util.Multi<>(dateTime, event));
                                        events.remove(event); continue external;}}
                            } else {
                                if (PhraseUtil.phraseIntersection(cleansedName, candidateCleansedName, 6, null) != null) {
                                    sout("PHRASAL K MERGE: " + event.organizer + "\t" + cleansedName + "\n" + candidate.organizer + "\t" + candidateCleansedName);
                                    dateTimeEvents.underlying.remove(new Util.Multi<>(dateTime, event));
                                    events.remove(event);
                                    continue external;}}}}}}}}
    static void cleanDoubleDateSpecialDates(Event event) { if (Util.empty(event.dateTimes)) return;
        Set<Calendar.Date> toRemove = set();
        List<String> dateMatchers = DateMatcher.matchersDoubleNames(event.ctx.lang, event.ctx.date);
        List<String> comboMatchers = DateTimeMatcher.matchersNames(event.ctx.time, event.ctx.datetime);
        for (Map.Entry<Calendar.Date, Time> e : event.dateTimes.entrySet()) { Calendar.Date date = e.getKey();
            if (dateMatchers.contains(date.note) && !comboMatchers.contains(e.getValue().provenance)) {
                if ("sun".equals(e.getKey().dayOfWeek)) {
                    filteredByDate.put(new Util.Multi<>(event.id, date), "BETWEEN_SUN");
                    toRemove.add(date);}
                if ("dec".equals(e.getKey().dateMonth) && "25".equals(e.getKey().dateDay)) {
                    filteredByDate.put(new Util.Multi<>(event.id, date), "XMAS");
                    toRemove.add(date);}
                if ("dec".equals(e.getKey().dateMonth) && "26".equals(e.getKey().dateDay)) {
                    filteredByDate.put(new Util.Multi<>(event.id, date), "BOXINGDAY");
                    toRemove.add(date);}
                if ("jan".equals(e.getKey().dateMonth) && "1".equals(e.getKey().dateDay)) {
                    filteredByDate.put(new Util.Multi<>(event.id, date), "NEWYEARS");
                    toRemove.add(date);}}}
        toRemove.forEach(d -> event.dateTimes.remove(d));}
    static boolean checkWeekdayNights(String title, String dayOfWeek) { int index = WEEKDAYS_ORDER.indexOf(dayOfWeek);
        if (index == -1) return false;
        String longweekday = WEEKDAYS_LONG.get(index);
        if (title.equalsIgnoreCase(longweekday + " " + "night")) {return true;}
        return false;}
    static String cleanAfterquotes(String title) { if (title == null) return title;
        List<String> newlist = list();
        for (String word : Util.splitList(title, "(?<!'‘\")\\s")) { word = Upper.uppercaseQuoted(word);
            newlist.add(word);} return Util.string(newlist, " ");}
    static String cleanSectionFinal(String name) { name = Removals.characterEndRemovals(name);
        name = Upper.uppercaseParts(name);
        name = name.replaceAll("(\\s|\\u00A0)+", " ");
        name = applyNonbreaks(name);
        name = name.replaceAll("\\(\\)", "").replaceAll(" - - ", " - ").replaceAll(", , ", " - "); return cleanEmptyParts(name);}
    static String cleanEmptyParts(String name) {List<Integer> toRemove = list();
        NList split = Util.split(name, " - ");
        for (int i = 0; i < split.size(); i++) { if (split.get(i).trim().length() < 2) {toRemove.add(i);}}
        return split.reconstructExcept(toRemove);}
    static String applyNonbreaks(String name) { NList nameSplit = Util.split(name, " - ");
        for (int i = 0; i < nameSplit.size(); i++) { String part = nameSplit.get(i);
            if (part.length() < 8) { NList phraseSplit = Util.split(part, " ");
                if (phraseSplit.size() > 1) { phraseSplit.splitDelimiter = "\u00A0"; return nameSplit.reconstructReplacing(i, phraseSplit.reconstruct());}}}
        return name;}
    static String generateUrl(Util.Multi<Util.Multi<Calendar.Date, Time>, Util.Multi<Event, Boolean>> multi) { String url;
        String homeParameter = multi.b.a.ctx.tertiaryHomeParam();
        if (multi.b.a.ctx.tertiaryHome()) { url = multi.b.a.source;
        } else if (!Util.empty(homeParameter)) { url = homeParameter;
        } else { url = multi.b.a.link;
            Map<Calendar.Date, String> dateUrls = multi.b.a.dateUrl;
            if (!Util.empty(dateUrls)) { String dateUrl = dateUrls.get(multi.a.a);
                if (dateUrl != null) {url = dateUrl;}}}
        if (url.contains("facebook.com")) {url = multi.b.a.source;}
        return url;}
    static boolean curtailFullstop(StringMutable clean) { String intermediate = clean.string;
        int beginIndex = Math.max(0, intermediate.length() - 40);
        if (intermediate.substring(beginIndex).contains(". ")) { clean.string = intermediate.substring(0, beginIndex + intermediate.substring(beginIndex).lastIndexOf(". ")); return true;}
        return false;}
    static void curtailPreposition(StringMutable clean, Context.LanguageContext langCtx) { int charactersFromEnd = 0;
        NList split = Util.split(clean.string, " ");
        for (int i = split.size() - 1; i > 0; i--) { if (charactersFromEnd > 40) break;
            String word = split.get(i);
            charactersFromEnd += word.length();
            if (langCtx.prepositionWords().contains(word.toLowerCase())) {
                if (i - 1 > 0 && langCtx.prepositionWords().contains(split.get(i - 1).toLowerCase())) { clean.string = split.reconstruct(i - 1);
                } else { clean.string = split.reconstruct(i);}
                break;}}}
    static boolean exclusions4(String ref, Event event) { boolean excluded = Exclude.lowAverageWordLength(ref, event.name);
        if (excluded) return true;
        excluded = Exclude.partExclusion(event);
        if (excluded) return true;
        excluded = Exclude.technicalExclusions(ref, event);
        if (excluded) return true;
        excluded = event.ctx.lang.prepositionWords().contains(event.name.toLowerCase().trim());
        if (excluded) return true;
        excluded = Exclude.exactExclusions(event);
        return excluded;}
    static String cleanSectionC(Event event, StringMutable clean) { Removals.removePresents(clean, event.ctx);
        Removals.removeFromTextSafely(clean);
        Removals.removeHtml(clean);
        Removals.removePrices(clean);
        Removals.removeWeekdays(clean, event.ctx);
        Removals.removeWeekday(clean, event.ctx, event.dateTimes.keySet());
        clean.string = clean.string.trim();
        for (int i = 0; i < 2; i++) {Removals.removeStart(clean, event.ctx);}
        Removals.removeAfter(clean, event.ctx);
        Removals.removePart(clean, event);
        Removals.removeStartPart(clean); //PHRASES TO REMOVE FROM THE START IF A PART
        Removals.removeEndPart(clean, event.ctx); // Removals from the end of the title
        Removals.removeFromEndsOfParts(clean, REMOVE_FROM_ENDS_OF_PARTS);
        Removals.removeFromEndsOfParts(clean, REMOVE_FROM_ENDS_OF_PARTS_LONG, 20);
        Removals.removeRepeatPart(clean);
        clean.string = clean.string.replaceAll(" [0-9]{1,3} (?i)attending", "");
        if (false) ellipsis(event, clean); //Insert Ellipsis if required
        splitByColon(clean); //Replace colon/full stop/exclamation mark with split
        return clean.string;}
    static void splitByColon(StringMutable clean) { clean.string = clean.string.replaceAll(" (?i)feat\\. ", " featuring ");
        NList split = Util.split(clean.string, " - ");
        String firstPart = split.get(0);
        if (PhraseUtil.wordCount(firstPart) > 8 && clean.string.length() > 15) {clean.string = clean.string.substring(0, 10) + clean.string.substring(10).replaceFirst("(:|(?<! (?i)st)\\.|\\!|\\|) ", " - ");}}
    static void ellipsis(Event event, StringMutable clean) { clean.string = clean.string.trim();
        if (event.curtailed && !Util.endsWith(event.name, list(".", ";", ","))) {
            if (clean.string.length() > 60) { clean.string = clean.string + "...";}}}
    static boolean exclusions1(Event event) { List<Calendar.Date> dates = Util.sort(event.dateTimes.keySet());
        if (Util.empty(dates)) { filtered.put(event.id, "NO DATETIMES"); return true; }
        if (event.ctx.tertiarySingleOnly() && dates.size() > 1) { filtered.put(event.id, "_SIN_ FILTER"); return true; }
        if (event.ctx.tertiaryDuoOnly() && dates.size() > 2) { filtered.put(event.id, "_DUO_ FILTER"); return true; }
        if (Calendar.Date.strictlyBeforeAfterCheck(Util.last(dates), Calendar.currentDate())) return true; //date exclusions
        if (Util.empty(event.dateTimes)) {filtered.put(event.id, "NULL TIME"); return true;} //time exclusions
        if (Exclude.urlExclusions(event, filtered)) return true; //url exclusions
        String found;
        for (Util.Lang language : event.ctx.lang.languages()) { List<String> exclusions = LANGUAGE_EXCLUSIONS.get(language);
            if (Util.empty(exclusions)) continue;
            found = Util.containsGet(event.name.toLowerCase(), exclusions);
            if (!Util.empty(found)) {
                filtered.put(event.id, language.name() + " EXCLUSION: " + found + "\n\t" + event.name.toLowerCase()); return true;}}
        if (isCancelled(event)) return true;
        if (isOnline(event)) return true;
        boolean excluded = Exclude.pairExclusions(event.name);
        if (excluded) {
            filtered.put(event.id, "PAIR EXCLUSION"); return true;}
        if (!Util.empty(event.ctx.tertiaryExclusion())) {
            for (String exclusion : event.ctx.tertiaryExclusion()) {
                if (event.name.toLowerCase().matches(".*\\b" + exclusion + "\\b.*")) { filtered.put(event.id, "SPECIFIC EXCLUSION"); return true;
                } else if (event.name.toLowerCase().matches(".*\\b" + exclusion + " .*")) { filtered.put(event.id, "SPECIFIC EXCLUSION"); return true;}}}
        found = Util.startsWithGet(event.name.toLowerCase(), EXC_START);
        if (found != null) { filtered.put(event.id, "START EXCLUSION: " + found); return true;}
        boolean matched = throwbackWeekdayMatcher(event.name);
        if (matched) { filtered.put(event.id, "THROWBACK"); return true;}
        if (Exclude.startExclusions(event, filtered)) return true; //Near Start Phrase
        excluded = !passesRequired(event); //Requirements
        if (excluded) { filtered.put(event.id, "REQUIRED NOT FOUND"); return true;}
        return false;}
    static boolean isOnline(Event event) { String found = Util.containsGet(event.name.toLowerCase(), EXC_ONLINE);
        if (!Util.empty(found)) {filtered.put(event.id, " EXCLUSION_ONLINE: " + found + "\n\t" + event.name.toLowerCase());return true;}
        return false;}
    static boolean isCancelled(Event event) {
        String found = Util.containsGet(event.name.toLowerCase(), EXC_CANCELLED);
        if (!Util.empty(found)) { filtered.put(event.id, " EXCLUSION_CANCELLED: " + found + "\n\t" + event.name.toLowerCase()); return true;}
        return false;}
    static boolean throwbackWeekdayMatcher(String name) { Matcher matcher = matcher("throwback " + M_WEEKDAY_LONG, PhraseUtil.subSentence(name.toLowerCase(), 10)); return matcher.find();}
    static boolean passesRequired(Event event) { if (Util.empty(event.ctx.tertiaryRequired())) return true;
        for (String requirement : event.ctx.tertiaryRequired()) { boolean foundInternal = true;
            for (String internalPattern : Util.split(requirement, "&&").underlying) {
                if (!event.name.toLowerCase().matches(".*\\b" + internalPattern + "\\b.*")) { foundInternal = false; break;}}
            if (foundInternal) {return true;}} sout("Requirement not met: " + Util.string(event.ctx.tertiaryRequired()) + " " + event.name); return false;}
    static void cleanA(Event event) { StringMutable eventName = new StringMutable(event.name.toLowerCase());
        Removals.stripSpecialFromStart(eventName);
        curtail(eventName, 200, event.ctx.lang);
        forceColonSplit(eventName, event.ctx);
        Removals.removeUrls(eventName);
        Removals.removePresents(eventName, event.ctx);
        Replica.cleanReplicaWords(eventName, event.link, event.ctx.lang);
        Removals.cleanUrls(eventName);
        Replace.cleanPunctuation(eventName);
        Upper.uppercase(eventName, event.ctx.lang);
        Upper.camelcase(eventName, event.originalText);
        cleanAttachedPunctuation(eventName);
        Removals.removeFromText(eventName, event.ctx);
        event.curtailed = curtail(eventName, 130, event.ctx.lang);
        event.name = eventName.string;}
    static void forceColonSplit(StringMutable eventName, Context ctx) {
        if (ctx.tertiaryAfterColon()) { NList split = Util.split(eventName.string, ":");
            if (split.size() > 1) {eventName.string = split.reconstructExcept(0);}}}
    static void cleanAttachedPunctuation(StringMutable eventName) {
        eventName.string = eventName.string.replaceAll("\\|", " - ");
    }
    static boolean curtail(StringMutable clean, int numCharacters, Context.LanguageContext langCtx) { if (clean.string.length() < numCharacters) return false;
        if (Util.empty(clean)) return false;
        boolean curtailed = curtailSize(clean, numCharacters);
        if (curtailed) {
            boolean curtailedFurther = curtailFullstop(clean);
            if (!curtailedFurther) {
                curtailPreposition(clean, langCtx);
                endOnGoodWord(clean);
                endOnGoodWord(clean);}} return curtailed;}
    static void endOnGoodWord(StringMutable clean) { NList split = Util.split(clean.string.trim(), "\\s+");
        String last = Util.last(split.underlying);
        if (BAD_WORDS_TO_END_ON.contains(last.toLowerCase())) {clean.string = split.reconstructBeforeLast();}}
    static boolean curtailSize(StringMutable eventName, int numCharacters) { Util.MultiList<Integer, String> indexWords = PhraseUtil.words(eventName.string);
        List<String> restrictedWords = list();
        for (Util.Multi<Integer, String> indexWord : indexWords.underlying) {if (indexWord.a < numCharacters) restrictedWords.add(indexWord.b);}
        String string = Util.string(restrictedWords);
        boolean curtailed = eventName.string.length() > string.length();
        eventName.string = string;
        return curtailed;}
    static class DateTimeComparator implements Comparator<Object> {
        public int compare(java.lang.Object o1, java.lang.Object o2) {
            if (o1 == o2) return 0; if (o1 instanceof String) return -1; if (o2 instanceof String) return 1;
            Util.Multi<Util.Multi<Calendar.Date,Time>, Event> multi1 = (Util.Multi) o1; Util.Multi<Util.Multi<Calendar.Date,Time>, Event> multi2 = (Util.Multi) o2;
            int dateEquality = multi1.a.a.compareTo(multi2.a.a);
            if (dateEquality != 0) return dateEquality;
            if (multi1.a.b == null && multi2.a.b == null) return 0; if (multi1.a.b == null) return 1; if (multi2.a.b == null) return -1;
            if (Integer.valueOf(multi1.a.b.getHour()) >= 22 || Integer.valueOf(multi1.a.b.getHour()) <= 5) {
                if (Integer.valueOf(multi2.a.b.getHour()) >= 22 || Integer.valueOf(multi2.a.b.getHour()) <= 5) {return 0;}
                return 1;}
            if (Integer.valueOf(multi2.a.b.getHour()) >= 22 || Integer.valueOf(multi2.a.b.getHour()) <= 5) return -1;
            return multi1.a.b.compareTo(multi2.a.b);}}
    static String cleanB(String title, String ref, InputData inputData) {
        if (Util.empty(title)) return title;
        title = Replace.replace(title);
        title = Removals.characterEndRemovals(title);
        title = Removals.removeParts(title, ref, inputData);
        title = title.replaceAll("(\\s|\u00A0)+", " ");
        title = Upper.uppercaseParts(title); return title;}
    static class Exclude {
        static boolean pairExclusions(String title) { List<String> words = split(title.toLowerCase(), "\\s+").underlying;
            words = sublist(words, 5);
            for (Util.Multi<String, String> indicator : PAIR_EXC.underlying) {
                if (words.contains(indicator.a) && words.contains(indicator.b)) {return true;}}
            return false;}
        static boolean isChurch(String organizer) { organizer = organizer.toLowerCase();
            if (organizer.startsWith("st ")) return true;
            List<String> churches = list("church", "cathedral");
            if (intersection(PhraseUtil.wordList(organizer), churches)) return true;
            return false;}
        static boolean filteredOnChurch(Event event, List<String> firstSix, Map<String, String> filtered) {
            if (isChurch(event.organizer)) {
                if (intersection(firstSix, EXC_FIRST_6_CHURCH)) { filtered.put(event.id, "FIRST 6 EXCLUSION CHURCH"); return true;}}
            return false;}
        static boolean technicalExclusions(String ref, Event event) { String found = containsGet(event.name.toLowerCase(), list(".jpg", ".png"));
            if (found != null) {sout("Technical Exclusion found: \"" + found + "\" in " + ref + " " + event.name); return true;}
            return false;}
        static boolean lowAverageWordLength(String ref, String string) {if (empty(string)) return true; if (PhraseUtil.averageLengthWords(string) < 2) {sout("AVERAGE WORD LENGTH < 2 for " + ref + " " + string); return true;} return false;}
        static boolean exactExclusions(Event event) {List<String> exactExclusions = list();
            for (String weekday : WEEKDAYS_LONG) { exactExclusions.add(weekday + " night");
                exactExclusions.add(weekday + "s");}
            for (String exactExclusion : exactExclusions) {
                if (exactExclusion.equalsIgnoreCase(event.name.trim())) return true;}
            return false;}
        static boolean partExclusion(Event event) { NList split = split(event.name.toLowerCase(), " - ");
            for (String part : split.underlying) {
                if (EXC_PARTS.contains(part.trim())) {sout("Ignoring event with title part: " + part + " " + event.name); return true;}}
            return false;}
        static boolean urlExclusions(Event event, Map<String, String> filtered) {        // ------------------ Url Indicative Exclusions ------------------------------
            if (!contains(event.link, list("http", "www"))) { filtered.put(event.id, "UNCLICKABLE URL: " + event.link);return true;}
            if (event.link.contains("twitter.com")) { filtered.put(event.id, "TWITTER LINK");return true;}
            if (event.link.toLowerCase().endsWith(".pdf")) { filtered.put(event.id, "PDF");return true;}
            return false;}
        static boolean startExclusions(Event event, Map<String, String> filtered) {
            Util.MultiList<Integer, String> words = PhraseUtil.words(event.name.toLowerCase());
            if (intersection(sublist(words, 3).getBList(), EXC_FIRST_3)) { filtered.put(event.id, "FIRST 3 EXCLUSION");return true;}
            List<String> firstSix = sublist(words, 6).getBList();
            if (intersection(firstSix, EXC_FIRST_6)) { filtered.put(event.id, "FIRST 6 EXCLUSION");return true;}
            if (intersection(firstSix, EXC_WEBINAR)) { filtered.put(event.id, "FIRST 6 WEBINAR");return true;}
            if (filteredOnChurch(event, firstSix, filtered)) return true;
            if (contains(string(firstSix, " "), EXC_FIRST_6_PHRASE)) { filtered.put(event.id, "FIRST 6 PHRASE EXCLUSION");return true;}
            return false;}}}