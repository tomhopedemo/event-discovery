package com.events;

import com.events.date.Calendar;
import com.events.date.M_Lang;
import com.events.date.Time;
import com.events.date.TimeMatcher;

import java.util.List;
import java.util.UUID;

import static com.events.DateMatcher.cleanDates;
import static com.events.date.TimeMatcher.cleanTimes;
import static com.events.Util.list;
import static com.events.Util.sout;

class Secondary {
    static void execute(List<String> refs, Dirs dirs, InputData inputData) {
        sout("MERGING...");
        for (String ref : refs) {
            List<Event> merged = list();
            List<String> raw = IntermediateData.getUnmergedEvents(ref, dirs);
            List<Event> unmerged = Event.create(raw);
            if (Util.empty(unmerged)) continue;
            Context ctx = Context.make(ref, inputData);
            for (Event event : unmerged) {
                event.name = Util.lowercase(event.name);
                prepareForMerge(event, ctx);
                if (ctx.isFestival()) {
                    merged.add(event);
                    continue;
                }
                if (Parse.check(event.name)) continue;
                Merger.merge(merged, event);
            }
            String output = dirs.getMergedDir() + ref + ".txt";
            merged.forEach(m -> m.id = UUID.randomUUID().toString());
            List<String> eventsSaveFormat = Event.saveFormat(merged);
            Util.write(output, Util.string(eventsSaveFormat, Shared.DELIMITER_RECORD));
        }
        sout("MERGING COMPLETE");
    }

    static void prepareForMerge(Event event, Context ctx) {
        if (Util.empty(event.name)) return;
        Util.StringMutable clean = new Util.StringMutable(event.name);
        backupTime(event, ctx);
        cleanDates(clean, event.dateTimes.keySet(), ctx.lang);
        cleanTimes(clean);
        if (!ctx.parseKeepRef() && !ctx.isFestival()) {
            cleanOrganizerName(event.organizer, clean, ctx);
        }
        for (String phrase : M_Lang.PHRASES_TO_CLEAN_ENG) {
            clean.string = PhraseUtil.removeWordSafely(clean.string, phrase);
        }
        for (Util.Lang lang : Util.Lang.values()) {
            if (ctx.lang.lang(lang)) {
                for (String phrase : Util.safeNull(M_Lang.LANG_PHRASES_TO_CLEAN.get(lang))) {
                    clean.string = PhraseUtil.removeWordSafely(clean.string, phrase);
                }
            }
        }
        if (!event.organizer.toLowerCase().contains("cinema")) {
            clean.string = PhraseUtil.removeWordSafely(clean.string, "free, &");
            clean.string = PhraseUtil.removeWordSafely(clean.string, "free to");
            clean.string = PhraseUtil.removeWordSafely(clean.string, "for free");
            clean.string = PhraseUtil.removeWordSafely(clean.string, "free");
        }
        event.name = clean.string;
    }

    static void backupTime(Event event, Context ctx) {
        if (Util.empty(event.dateTimes) && !Util.empty(ctx.timeManual())) {
            Time time = TimeMatcher.matchSingle(Util.lowercase(ctx.timeManual()), false, ctx.lang, ctx.time);
            for (Calendar.Date date : event.dateUrl.keySet()) {
                event.dateTimes.put(date, time);
            }
        }
    }

    static void cleanOrganizerName(String organizer, Util.StringMutable clean, Context ctx) {
        String string = clean.string;
        if (Util.empty(string)) return;
        string = removeOrganizerPhrase(string, organizer);
        if (!Util.empty(ctx.secondaryAlternativeName())) {
            string = removeOrganizerPhrase(string, ctx.secondaryAlternativeName());
        }         // ------------- ALTERNATIVE ORGANIZER NAME CLEAN
        List<String> wordPairs = Util.split(organizer, "\\s+").getWordPairs();         // ----------------CLEAN COMPONENT PARTS OF ORGANIZER NAME ---
        if (!Util.empty(wordPairs)) {
            for (String wordPair : wordPairs) {
                if (ctx.lang.prepositionWords().contains(wordPair)) continue;
                string = removeOrganizerPhrase(string, wordPair);
            }
        }
        clean.string = string;
    }

    static String removeOrganizerPhrase(String string, String organizerName) {
        List<String> prepositonalOrganizerPrefixes = list("with ", "to ", "to the ", "at the ", "at ", "@ ", "@\u00A0", "in the ", "of the ", "and ");
        List<String> prepositonalOrganizerSuffixes = list("presents", "and");
        for (String prefix : prepositonalOrganizerPrefixes) {
            string = PhraseUtil.removeWordSafely(string, prefix + organizerName.toLowerCase().trim());
        }
        for (String suffix : prepositonalOrganizerSuffixes) {
            string = PhraseUtil.removeWordSafely(string, organizerName.toLowerCase().trim() + " " + suffix);
        }
        string = PhraseUtil.removeWordSafely(string, organizerName.toLowerCase().trim());
        return string;
    }
}