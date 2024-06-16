package com.arcta.events;

import java.util.*;

import static com.arcta.events.FiltersLists.REVERSE_ORDER;
import static com.arcta.events.FiltersLists.REVERSE_ORDER_SHORT;
import static com.arcta.events.Util.*;
import static java.lang.Integer.parseInt;

class AppDataGenerator {
    static void generateAppData(Util.MultiList<Multi<Calendar.Date, Time>, Multi<Event, Boolean>> dateTimeEvent, Map<String, String> filtered, Map<Util.Multi<String, Calendar.Date>, String> filteredByDate, Dirs dirs, InputData inputData) {
        MapList<Calendar.Date, Multi<Time, String>> dateEvent = new MapList<>();
        MapList<MultiA<String>, ScreenEvent> refDateTimeEvent = new MapList<>();
        for (Util.Multi<Util.Multi<Calendar.Date, Time>, Util.Multi<Event, Boolean>> multi : dateTimeEvent.underlying) {
            if (!(multi.b.b)) continue;
            final String ref = multi.b.a.organizer;
            DisplayEvent displayEvent = new DisplayEvent();
            displayEvent.time = generateTimeDisplay(multi, ref);
            if (displayEvent.time == null) continue;
            displayEvent.displayOrganizer = generateOrganizerName(ref, inputData);             //[1] --------------- ORGANIZER
            displayEvent.ref = ref;
            displayEvent.fullTitle = multi.b.a.name;
            displayEvent.id = multi.b.a.id;
            Util.Multi<String, String> titleUndertitle = TitleSplitter.generateTitleUndertitle(multi);             //[2]/[5] ---------------- TITLE & UNDERTITLE
            if (Util.safeA(titleUndertitle) == null) {
                sout("null title");
                continue;
            }
            TitleSplitter.colonSplit(titleUndertitle);
            TitleSplitter.commaSplitUndertitle(titleUndertitle);
            TitleSplitter.phrasalSplit(titleUndertitle);
            TitleSplitter.bracketSplit(titleUndertitle);
            TitleSplitter.switchTitleUndertitle(titleUndertitle);
            String title = titleUndertitle.a;
            title = Removals.removeExceptions(title);
            title = TitleSplitter.commaSplitTitle(title);
            title = Removals.cleanLonelyParenthesesTitle(title);
            title = Removals.cleanLonelyDoublequotesTitle(title);
            title = Removals.cleanLonelySinglequotes(title);
            title = Removals.cleanBracketPairs(title);
            title = Upper.cleanUppercases(title);
            title = Output.cleanAfterquotes(title);
            title = Upper.cleanUppercasesByWord(title, multi.b.a.ctx.lang);
            title = Upper.cleanForceUppercase(title, multi.b.a.originalText);
            title = Removals.removalFilmRatings(title);
            title = Removals.cleanBracketSpaces(title);
            title = Removals.cleanSpecific(title);
            title = Removals.obscenities(title);
            title = Removals.cleanSundays(title, multi.a.a.dayOfWeek);
            if (Output.checkWeekdayNights(title, multi.a.a.dayOfWeek)) {
                sout(titleUndertitle.a, ref, multi.b.a.name);
                continue;
            }
            if (title.equalsIgnoreCase("yoga")) {
                sout("YOGA", ref, multi.b.a.name);
                continue;
            }
            displayEvent.title = title;
            String url = Output.generateUrl(multi);             //[3] ----------------- URL
            if (url == null) {
                sout("URL NULL", ref, multi.b.a.name);
                continue;
            }
            displayEvent.link = url;
            displayEvent.district = generateDistrict(ref, multi.b.a, inputData);             //[4] -------------- LOCATION ----------------
            displayEvent.dates = new ArrayList<>(Util.safeNull(multi.b.a.dateTimes.keySet()));
            displayEvent.category = inputData.getCategory(ref);
            if (Util.empty(displayEvent.category)) {
                displayEvent.category = multi.b.a.category;
                if (Util.empty(displayEvent.category) && multi.b.a.ctx.isFestival()) {
                    displayEvent.category = "festival";
                }
            }
            displayEvent.source = multi.b.a.source;
            displayEvent.soldout = multi.b.a.soldout ? "Y" : "N";
            displayEvent.languages = multi.b.a.ctx.lang.languages();
            String saveFormat = DisplayEvent.saveFormatShort(displayEvent);             // --------------------- END STRING CONSTRUCTION ------------------
            dateEvent.put(multi.a.a, new Util.Multi<>(multi.a.b, saveFormat));
            refDateTimeEvent.put(new MultiA<>(multi.b.a.organizer, multi.b.a.source), new ScreenEvent(multi.a.a, multi.a.b, displayEvent.id, displayEvent.title));
        }
        if (Output.WRITE_TO_SCREEN) {
            writeToScreen(refDateTimeEvent, filtered, filteredByDate, dirs);
        } else {
            writeOutput(dateEvent, dirs, inputData);
        }
    }

    static void writeOutput(MapList<Calendar.Date, Multi<Time, String>> dateEvent, Dirs dirs, InputData inputData) {
        for (Calendar.Date date : dateEvent.orderedKeys()) {
            if (Calendar.CalendarDate.diffFromCurrent(date) < 90 && Calendar.indexOf(date) > -1) {
                List<String> output = list();
                List<Util.Multi<Time, String>> multis = dateEvent.get(date);
                multis.sort((o1, o2) -> o1.a.compareTo(o2.a));
                for (Util.Multi<Time, String> multi : multis) {
                    output.add(multi.b);
                }
                String outputString = Util.string(output, Shared.DELIMITER_RECORD_READABLE);
                Util.write(dirs.getOutputDir() + inputData.getCity() + "-" + date.fileDate() + ".txt", outputString);
            }
        }
    }

    static void writeToScreen(MapList<MultiA<String>, ScreenEvent> refDateTimeEvent, Map<String, String> filtered, Map<Util.Multi<String, Calendar.Date>, String> filteredByDate, Dirs dirs) {
        for (MultiA<String> key : refDateTimeEvent.keys()) {
            List<ScreenEvent> originalEvents = list();
            for (Event loadedEvent : IntermediateData.getMergedEventList(key.a, dirs)) {
                for (Map.Entry<Calendar.Date, Time> datetime : loadedEvent.dateTimes.entrySet()) {
                    originalEvents.add(new ScreenEvent(datetime.getKey(), datetime.getValue(), loadedEvent.id, loadedEvent.name));
                }
            }
            Comparator<ScreenEvent> screenEventComparator = (a, b) -> {
                int i = a.date.compareTo(b.date);
                if (i != 0) return i;
                return a.time.compareTo(b.time);
            };
            List<ScreenEvent> events = new ArrayList<>(refDateTimeEvent.get(key));
            for (ScreenEvent event : events) {
                if (!originalEvents.contains(event)) {
                    originalEvents.add(event);
                }
            }
            originalEvents.sort(screenEventComparator);
            Collections.reverse(originalEvents);
            List<String> safeFilters = list();
            for (ScreenEvent originalEvent : originalEvents) {
                if (Calendar.Date.strictlyBeforeAfterCheck(originalEvent.date, Calendar.currentDate())) continue;
                if (Calendar.Date.daysBetween(Calendar.currentDate(), originalEvent.date) > 90) continue;
                if (events.contains(originalEvent)) {
                    ScreenEvent better = events.get(events.indexOf(originalEvent));
                    sout(better.date.prettyDay() + "\n  \t" + better.time.pretty() + "\t" + better.title);
                } else {
                    if (filtered.containsKey(originalEvent.id)) {
                        String whyfilter = filtered.get(originalEvent.id);
                        if (!safeFilters.contains(whyfilter)) {
                            sout(originalEvent.date.prettyDay() + "\n  \t" + originalEvent.time.pretty() + "\t" + Util.substring(0, 50, originalEvent.title));
                            sout("\t" + whyfilter);
                        }
                    } else if (filteredByDate.containsKey(new Util.Multi<>(originalEvent.id, originalEvent.date))) {
                        String whyfilter = filteredByDate.get(new Util.Multi<>(originalEvent.id, originalEvent.date));
                        if (!safeFilters.contains(whyfilter)) {
                            sout(originalEvent.date.prettyDay() + "\n  \t" + originalEvent.time.pretty() + "\t" + Util.substring(0, 50, originalEvent.title));
                            sout("\t" + whyfilter);
                        }
                    } else {
                        sout(originalEvent.date.prettyDay() + "\n  \t" + originalEvent.time.pretty() + "\t" + Util.substring(0, 50, originalEvent.title) + "\t***");
                    }
                }
            }
            sout("\n\n\t" + key.a + "\n\t" + key.b);
        }
    }

    static String generateTimeDisplay(Util.Multi<Util.Multi<Calendar.Date, Time>, Util.Multi<Event, Boolean>> multi, String ref) {
        String timeDisplay;
        if (parseInt(multi.a.b.getHour()) >= 23 && !multi.b.a.ctx.parseLate()) {
            sout("LATE TIME", ref, multi.b.a.name, multi.a.b.pretty());
            return null;
        } else {
            timeDisplay = multi.a.b.pretty();
        }
        if (timeDisplay == null) {
            sout("TIME DISPLAY NULL", ref, multi.b.a.name);
            return null;
        }
        return timeDisplay;
    }

    static String generateDistrict(String ref, Event event, InputData inputData) {
        if (event.district != null) return Util.uppercaseFirstLetter(event.district);
        return Util.uppercaseFirstLetter(Util.empty(inputData.getDistrict(ref)) ? inputData.getCity() : inputData.getDistrict(ref));
    }

    static String generateOrganizerName(String ref, InputData inputData) {
        return (Util.empty(inputData.getPreferredName(ref)) ? ref : inputData.getPreferredName(ref)).trim();
    }

    static class ScreenEvent {
        Calendar.Date date;
        Time time;
        String id;
        String title;

        ScreenEvent(Calendar.Date date, Time time, String id, String title) {
            this.date = date;
            this.time = time;
            this.id = id;
            this.title = title;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScreenEvent that = (ScreenEvent) o;
            return Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(id, that.id);
        }

        public int hashCode() {
            return Objects.hash(date, time, id);
        }
    }

    static class TitleSplitter {
        static String commaSplitTitle(String title) {
            if (!empty(title) && title.length() > 50) {
                NList split = split(title, ", ");
                if (split.get(0).length() > 20) title = split.get(0);
            }
            return title;
        }

        static void switchTitleUndertitle(Util.Multi<String, String> titleUndertitle) {
            if (REVERSE_ORDER.contains(lowercase(titleUndertitle.a)) && (titleUndertitle.b != null && titleUndertitle.b.length() >= 10)) {
                sout("SWITCHING: " + titleUndertitle.a + " WITH " + titleUndertitle.b);
                String tmp = titleUndertitle.a;
                titleUndertitle.a = titleUndertitle.b;
                titleUndertitle.b = tmp;
            }
        }

        static void bracketSplit(Util.Multi<String, String> titleUndertitle) {
            if (empty(titleUndertitle.a)) return;
            if (titleUndertitle.a.length() > 40 && titleUndertitle.a.substring(40).contains(")")) {
                titleUndertitle.a = titleUndertitle.a.substring(0, 40) + safeNull(split(titleUndertitle.a.substring(40), "\\)").get(0)) + ")";
            }
        }

        static void phrasalSplit(Util.Multi<String, String> titleUndertitle) {
            if (!empty(titleUndertitle.a) && titleUndertitle.a.length() > 30 && titleUndertitle.a.toLowerCase().contains(" featuring ")) {
                List<String> list = split(titleUndertitle.a, "(?i)featuring").underlying;
                if (list.get(0).length() > 15 && list.get(1).length() > 10) {
                    titleUndertitle.a = list.get(0);
                    titleUndertitle.b = "Featuring" + list.get(1);
                }
            }
        }

        static void commaSplitUndertitle(Util.Multi<String, String> titleUndertitle) {
            if (!empty(titleUndertitle.b)) {
                if (titleUndertitle.b.length() > 40 && titleUndertitle.b.substring(40).contains(", ")) {
                    titleUndertitle.b = titleUndertitle.b.substring(0, 40) + split(titleUndertitle.b.substring(40), ", ").get(0);
                }
                if (titleUndertitle.b.length() > 20 && titleUndertitle.b.substring(20).contains(", ")) {
                    NList nlist = split(titleUndertitle.b.substring(20), ", ");
                    List<String> underlying = nlist.underlying;
                    if (underlying.size() > 1) {
                        String last = last(underlying).trim();
                        String[] split = last.split("\\s");
                        if (split.length == 1) {
                            titleUndertitle.b = titleUndertitle.b.substring(0, 20) + nlist.reconstructBeforeLast();
                        }
                    }
                }
            }
        }

        static void colonSplit(Util.Multi<String, String> titleUndertitle) {
            if (titleUndertitle.a.contains(": ")) {
                List<String> underlying = split(titleUndertitle.a, ": ").underlying;
                if (underlying.size() > 1 && underlying.get(0).length() > 10 && underlying.get(1).length() > 10) {
                    titleUndertitle.a = underlying.get(0);
                    titleUndertitle.b = underlying.get(1);
                } else if (underlying.size() > 2 && (underlying.get(0).length() + underlying.get(1).length()) > 10 && underlying.get(2).length() > 10) {
                    titleUndertitle.a = underlying.get(0) + ": " + underlying.get(1);
                    titleUndertitle.b = underlying.get(2);
                }
            }
        }

        static Util.Multi<String, String> generateTitleUndertitle(Util.Multi<Util.Multi<Calendar.Date, Time>, Util.Multi<Event, Boolean>> multi) {
            Util.Multi<String, String> titleUndertitle = new Util.Multi<>(null, "");
            String wholeText = multi.b.a.name;
            if (wholeText == null) return null;
            String[] overviewSplit = wholeText.split(" - ");
            String overviewTemp = overviewSplit[0];
            wholeText = finalSplits(wholeText, overviewTemp);
            overviewSplit = wholeText.split(" - ");
            String tentativeTitle = overviewSplit[0].trim();
            String tentativeUndertitle = overviewSplit.length > 1 ? overviewSplit[1] : null;
            if (REVERSE_ORDER.contains(tentativeTitle.toLowerCase())) {
                multi.b.a.category = Category.map.get(tentativeTitle.toLowerCase());
                if (!empty(tentativeUndertitle) && tentativeUndertitle.length() > 15) {
                    String tmp = tentativeUndertitle;
                    tentativeUndertitle = tentativeTitle;
                    tentativeTitle = tmp;
                }
            } else if (REVERSE_ORDER_SHORT.contains(tentativeTitle.toLowerCase())) {
                multi.b.a.category = Category.map.get(tentativeTitle.toLowerCase());
                if (!empty(tentativeUndertitle)) {
                    String tmp = tentativeUndertitle;
                    tentativeUndertitle = tentativeTitle;
                    tentativeTitle = tmp;
                }
            }
            titleUndertitle.a = cleanEnd(tentativeTitle);
            titleUndertitle.b = cleanEnd(tentativeUndertitle);
            return titleUndertitle;
        }

        static String finalSplits(String wholeText, String overviewTemp) {
            List<String> splitWordsRemove = list(" performs ");
            List<String> splitWords = list(" with ", " accompanied ");
            if (overviewTemp.length() > 50) {
                String substring = overviewTemp.substring(20);
                if (contains(substring.toLowerCase(), splitWords)) {
                    for (String splitWord : splitWords) {
                        if (substring.toLowerCase().contains(splitWord)) {
                            wholeText = overviewTemp.substring(0, 20) + substring.replaceFirst("(?i)" + splitWord, " - " + uppercaseFirstLetter(splitWord) + " ");
                            break;
                        }
                    }
                } else if (contains(substring.toLowerCase(), splitWordsRemove)) {
                    for (String splitWord : splitWordsRemove) {
                        if (substring.toLowerCase().contains(splitWord)) {
                            wholeText = overviewTemp.substring(0, 20) + substring.replaceFirst("(?i)" + splitWord, " - ");
                            break;
                        }
                    }
                } else if (substring.contains("? ")) {
                    wholeText = overviewTemp.substring(0, 20) + substring.replaceFirst("\\? ", " - ");
                } else if (substring.contains(" + ")) {
                    wholeText = overviewTemp.substring(0, 20) + substring.replaceFirst(" \\+ ", " - + ");
                }
            }
            return wholeText;
        }

        static String cleanEnd(String string) {
            if (string == null) return null;
            if (endsWith(string, union(HYPHENS, list("-", ":", ",", "@", "*", "<", "(", "|", "&", ".", "\\", "/", "!", "\u2026")))) {
                string = substringRemoveLast(string);
                string = trim(string);
            }
            return string;
        }
    }

    static class Category {
        static Map<String, String> map = map();

        static {
            map.put("screening", "cinema");
            map.put("concert", "music");
            map.put("film screening", "cinema");
            map.put("film", "cinema");
            map.put("gig", "music");
            map.put("films", "cinema");
            map.put("poetry", "literature");
            map.put("literature", "literature");
        }
    }
}