package com.events;

import com.events.Context.DateContext;
import com.events.Context.LanguageContext;
import com.events.Util.NList;
import com.events.date.DateMatcher;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.events.Context.LanguageContext.andWords;
import static com.events.Util.map;

class DataHtml {
    static final List<String> BAD_CINEMA = Util.list("met opera", "met opera live", "nt live");
    static final List<String> DEPRIORITIZED_CATEGORIES = Util.list("comedy", "cinema", "film");
    static final List<String> DEPRIORITIZED_WORDS = Util.list("bingo");
    final static List<String> LATER_FILTER = Util.list("film", "comedy", "theatre");
    final static List<String> RELEGATION_STRINGS = Util.list(" rape", "twilight tour", " club ", "bingo", "special guest", "dj set", "xmas party", "x-mas party", "christmas disco", "christmas do", "christmas party", "christmas ball");
    List<String> majorOrgs, trustedOrgs, A1Orgs, untrustedOrgs;
    InputData inputData;

    DataHtml(InputData inputData) {
        this.inputData = inputData;
        this.majorOrgs = inputData.getStatus().major();
        this.trustedOrgs = inputData.getStatus().promoted();
        this.A1Orgs = inputData.getStatus().promoted1();
        this.untrustedOrgs = inputData.getStatus().untrusted();
    }

    Util.PageData generateFestivalPageData(com.events.date.Calendar.Date date) {
        List<DisplayEvent> events = generateFestivalEvents(date);
        Util.PageData pageData = new Util.PageData();
        pageData.events = events;
        return pageData;
    }

    Util.PageData generatePageData(String data, Html.HtmlContext ctx, com.events.date.Calendar.CalendarDate baseDate) {
        List<DisplayEvent> displayEvents = filterAndSort(data, ctx, baseDate, inputData.getCity());
        Util.PageData pageData = new Util.PageData();
        pageData.events = displayEvents;
        if (!"anywhere".equals(ctx.area)) {
            pageData.events = Util.sublist(pageData.events, 50);
        }
        return pageData;
    }

    List<DisplayEvent> filterAndSort(String data, Html.HtmlContext ctx, com.events.date.Calendar.CalendarDate baseDate, String city) {
        List<DisplayEvent> events = generateEvents(data);
        for (DisplayEvent event : events) {
            event.title = event.title.replace((char) 160, ' ');
        }
        applyPreClassificationFilters(ctx, events);
        Collections.shuffle(events);
        classify(events);
        applyClassificationFilters(ctx, events);
        Collections.shuffle(events);
        return orderEvents(ctx, baseDate, events, city);
    }

    void applyPreClassificationFilters(Html.HtmlContext ctx, List<DisplayEvent> events) {
        filterMajorVenues(events, ctx);
        filterRepeatFilms(events);
        filterRepeatEvents(events);
        filterRepeatNames(events);
        filterRepeatEventsSameOrganizer(events);
        filterCommonEvents(events);
        filterUncertainTitles(events);
        if (ctx.type != null && !LATER_FILTER.contains(ctx.type)) {
            filterType(events, ctx.type);
        }
    }

    void applyClassificationFilters(Html.HtmlContext ctx, List<DisplayEvent> events) {
        filterBadCinema(events);
        if (LATER_FILTER.contains(ctx.type)) {
            filterTypeByCompleteClassification(events, ctx.type);
        }
    }

    LinkedList<DisplayEvent> orderEvents(Html.HtmlContext ctx, com.events.date.Calendar.CalendarDate baseDate, List<DisplayEvent> events, String city) {
        LinkedList<DisplayEvent> orderedEvents;
        if (Util.empty(ctx.area) || "anywhere".equals(ctx.area)) {
            orderedEvents = orderEventsInCity(baseDate, events);
        } else {
            orderedEvents = orderByAreaLondon(events, ctx);
            shortFirst(orderedEvents);
        }
        if ("anywhere".equals(ctx.area) || Util.empty(ctx.area)) {
        } else {
            orderedEvents = new LinkedList<>(Util.sublist(orderedEvents, 20));
        }
        applyColors(orderedEvents);
        return orderedEvents;
    }

    void filterMajorVenues(List<DisplayEvent> eventList, Html.HtmlContext ctx) {
        if (ctx.majorsOnly) {
            eventList.removeIf(e -> !majorOrgs.contains(e.ref));
        }
    }

    LinkedList<DisplayEvent> orderByAreaLondon(List<DisplayEvent> eventList, Html.HtmlContext ctx) {
        LinkedList<DisplayEvent> orderedByArea = new LinkedList<>();
        Util.MultiList<Double, String> areasOrderedByProximityLondon = London.areasOrderedByProximity(ctx.area);
        if (Util.empty(areasOrderedByProximityLondon)) return new LinkedList<>(eventList);
        List<String> iteratedArea = Util.list();
        for (Util.Multi<Double, String> distanceArea : areasOrderedByProximityLondon.underlying) {
            String area = distanceArea.b;
            if (Util.containsIgnoreCase(area, iteratedArea)) continue;
            iteratedArea.add(area);
            List<DisplayEvent> byArea = eventList.stream().filter(e -> area.equalsIgnoreCase(e.district)).collect(Collectors.toList());
            if (Util.empty(byArea)) continue;
            byArea.forEach(e -> e.distance = distanceArea.a);
            orderedByArea.addAll(byArea);
        }
        for (DisplayEvent displayEvent : eventList) {
            if (!orderedByArea.contains(displayEvent)) orderedByArea.add(displayEvent);
        }
        return orderedByArea;
    }

    void shortFirst(LinkedList<DisplayEvent> list) {
        DisplayEvent promotedToTop = null;
        for (DisplayEvent displayEvent : Util.sublist(list, 5)) {
            if (displayEvent.title.length() <= 27) {
                promotedToTop = displayEvent;
                break;
            }
        }
        if (promotedToTop != null) {
            list.remove(promotedToTop);
            list.addFirst(promotedToTop);
        }
    }

    void applyColors(LinkedList<DisplayEvent> list) {
        if (Util.empty(list)) return;
        int colorIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            if (((float) i / (float) list.size()) > ((float) colorIndex / (float) Html.colorBlend.size())) {
                colorIndex++;
            }
            list.get(i).colorIndex = colorIndex;
        }
    }

    List<DisplayEvent> generateFestivalEvents(com.events.date.Calendar.Date date) {
        List<DisplayEvent> events = Util.list();
        List<String> refs = loadFestivals(date);
        for (String ref : refs) {
            List<String> urls = inputData.getUrls(ref);
            if (Util.empty(urls)) continue;
            DisplayEvent displayEvent = new DisplayEvent();
            displayEvent.ref = ref;
            displayEvent.title = ref;
            displayEvent.link = urls.get(0);
            events.add(displayEvent);
        }
        return events;
    }

    List<String> loadFestivals(com.events.date.Calendar.Date date) {
        List<String> festivals = Util.list();
        List<String> refs = inputData.getFestivals();
        Map<String, List<com.events.date.Calendar.Date>> festivalDates = parseFestivalDates(refs);
        for (String festival : festivalDates.keySet()) {
            List<com.events.date.Calendar.Date> dates = festivalDates.get(festival);
            if (Util.empty(dates)) continue;
            if (dates.contains(date)) {
                festivals.add(festival);
            }
        }
        return festivals;
    }

    Map<String, List<com.events.date.Calendar.Date>> parseFestivalDates(List<String> refs) {
        LanguageContext languageContext = new LanguageContext("", "", "uk");
        DateContext dateContext = new DateContext("", "");
        Map<String, List<com.events.date.Calendar.Date>> festivalDates = map();
        for (String ref : refs) {
            String notes = inputData.getNotes(ref);
            String dateStatic = Context.Interpret.bang("_D", notes);
            List<com.events.date.Calendar.Date> dates = DateMatcher.match(new Util.StringMutable(dateStatic), languageContext, dateContext);
            festivalDates.put(ref, dates);
        }
        return festivalDates;
    }

    List<DisplayEvent> generateEvents(String data) {
        List<DisplayEvent> events = Util.list();
        if (Util.empty(data)) return events;
        List<String> refs = inputData.ABFestivalLocalLong();
        for (String record : data.split(Shared.DELIMITER_RECORD_READABLE)) {
            DisplayEvent event = DisplayEvent.create(record);
            if (!refs.contains(event.ref)) continue;
            if (event == null) continue;
            events.add(event);
        }
        events = mergeEvents(events);
        for (DisplayEvent event : events) {
            cleanEvent(event);
        }
        for (DisplayEvent event : events) {
            if (event.title.length() > 40) {
                event.title = event.title.substring(0, 40) + "\u2026";
            }
        }
        return events;
    }

    void cleanEvent(DisplayEvent event) {
        event.title = Util.split(event.title, ": ").get(0).trim();
        event.title = Util.split(event.title, "\\? ").get(0).trim();
        event.title = Util.split(event.title, "! ").get(0).trim();
        event.title = event.title.replaceAll(" & the ", " + The ");
        event.title = event.title.replaceAll("(?i), the ", "/The ");
        event.title = event.title.replaceAll(", ", "/");
        event.title = event.title.replaceAll("(?i) And ", " & ");
        event.title = Util.split(event.title, " [\\+|&] ").reconstruct(2, " + ");
        if (event.title.endsWith(" +")) event.title = event.title.substring(0, event.title.length() - 2);
        cleanCapitalAfterSlash(event);
        cleanBetweenBrackets(event);
        cleanRatingEnd(event);
        if (event.title.matches((".*\\((19|20|21|22)[0-9]{2}\\)$"))) {
            event.title = event.title.substring(0, event.title.length() - 6).trim();
        }
        event.title = event.title.replaceAll("(?i) ft$", "");
        cleanSlashSpace(event);
        cleanFestivalEnding(event);
        cleanLongTitle(event);
        cleanCountries(event);
        cleanHangingWords(event);
        cleanStartPunctuation(event);
    }

    List<DisplayEvent> mergeEvents(List<DisplayEvent> events) {
        List<DisplayEvent> tempMerge = Util.list();
        for (DisplayEvent event : events) {
            if (event.source.equals(event.link)) {
                tempMerge.add(event);
                continue;
            }
            boolean foundMatch = false;
            for (DisplayEvent existing : tempMerge) {
                if (existing.ref.equals(event.ref) && existing.time.equals(event.time) && Merger.urlsEquivalent(existing.link, event.link)) {
                    foundMatch = true;
                    break;
                }
                if (existing.ref.equals(event.ref) && existing.title.equals(event.title) && Merger.urlsEquivalent(existing.link, event.link)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) {
                tempMerge.add(event);
            }
        }
        return tempMerge;
    }

    void cleanLongTitle(DisplayEvent displayEvent) {
        if (displayEvent.title.length() > 35) cleanWithSplit(displayEvent);
        if (displayEvent.title.length() > 35) cleanBySplit(displayEvent);
        if (displayEvent.title.length() > 35) cleanWordSplit(displayEvent, "through", 4);
        if (displayEvent.title.length() > 35) cleanOfSplit(displayEvent);
        if (displayEvent.title.length() > 35) cleanAndSplit(displayEvent);
        if (displayEvent.title.length() > 35) cleanInConversation(displayEvent);
        if (displayEvent.title.length() > 30 && PhraseUtil.num("/", displayEvent.title) > 1) {
            displayEvent.title = displayEvent.title.substring(0, displayEvent.title.lastIndexOf("/"));
        }
        if (displayEvent.title.length() > 30) splitSlash(displayEvent);
        if (displayEvent.title.length() > 30) splitFeaturing(displayEvent);
        if (displayEvent.title.length() > 35) cleanX(displayEvent);
        if (displayEvent.title.length() > 30) cleanFeaturing(displayEvent);
        if (displayEvent.title.length() > 35) cleanWordSplit(displayEvent, "or", 7);
        if (displayEvent.title.length() > 45) cleanWordSplit(displayEvent, "of", 5); //should be in.
        if (displayEvent.title.length() > 50) cleanAndSplitFinal(displayEvent);
        if (displayEvent.title.length() > 50) cleanExclamation(displayEvent);
    }

    void cleanStartPunctuation(DisplayEvent displayEvent) {
        ArrayList<String> singlequotes = Util.list("'", "‘");
        for (String singlequote : singlequotes) {
            if (displayEvent.title.startsWith(singlequote) && PhraseUtil.num(singlequote.charAt(0), displayEvent.title) == 1 && PhraseUtil.num('’', displayEvent.title) == 0) {
                displayEvent.title = Util.substringRemoveFirst(displayEvent.title);
                break;
            }
        }
        if (displayEvent.title.startsWith("- ") || displayEvent.title.startsWith("+ ")) {
            displayEvent.title = Util.substringRemoveFirst(displayEvent.title);
        }
        displayEvent.title = displayEvent.title.trim();
    }

    void cleanHangingWords(DisplayEvent displayEvent) {
        if (displayEvent.title.toLowerCase().endsWith(" dr") && displayEvent.title.length() > 10) {
            displayEvent.title = displayEvent.title.substring(0, displayEvent.title.length() - 3);
        }
    }

    void cleanCountries(DisplayEvent displayEvent) {
        List<String> countries = Util.list("uk", "us", "usa", "cz", "hu");
        for (String country : countries) {
            displayEvent.title = displayEvent.title.replaceAll("(?i)\\(" + country + "\\)", "");
        }
    }

    void cleanInConversation(DisplayEvent displayEvent) {
        String substring = displayEvent.title.substring(18);
        ArrayList<String> inconversation = Util.list(" in conversation", " in-conversation");
        int index = Util.indexOf(substring.toLowerCase(), inconversation);
        if (index > 0) {
            displayEvent.title = displayEvent.title.substring(0, 20) + substring.substring(0, index);
            return;
        }
        index = substring.toLowerCase().indexOf(" in association");
        if (index > 0) displayEvent.title = displayEvent.title.substring(0, index);
    }

    void cleanFeaturing(DisplayEvent displayEvent) {
        NList split = Util.split(displayEvent.title, " ");
        if (split.size() > 3) {
            for (int i = 2; i < split.size(); i++) {
                if ("featuring".equalsIgnoreCase(split.get(i))) {
                    displayEvent.title = split.reconstruct(i);
                    return;
                }
            }
        }
    }

    void splitSlash(DisplayEvent displayEvent) {
        Matcher matcher = Util.matcher(".*\\S(/)\\S.*", displayEvent.title.substring(10));
        if (matcher.find()) {
            displayEvent.title = displayEvent.title.substring(0, 10) + displayEvent.title.substring(10, 10 + matcher.start(1));
        }
    }

    void splitFeaturing(DisplayEvent displayEvent) {
        if (displayEvent.title.indexOf(" ft. ") > 10) {
            displayEvent.title = Util.get(Util.splitList(displayEvent.title, " ft\\. "), 0);
        } else if (displayEvent.title.indexOf(" ft ") > 10) {
            displayEvent.title = Util.get(Util.splitList(displayEvent.title, " ft "), 0);
        }
    }

    void cleanX(DisplayEvent displayEvent) {
        String substring = displayEvent.title.substring(30);
        int index = substring.toLowerCase().indexOf(" x ");
        if (index > 0) displayEvent.title = displayEvent.title.substring(0, index);
    }

    void cleanExclamation(DisplayEvent displayEvent) {
        String substring = displayEvent.title.substring(25);
        int index = substring.toLowerCase().indexOf("! ");
        if (index > 0) displayEvent.title = displayEvent.title.substring(0, index);
    }

    void cleanWithSplit(DisplayEvent displayEvent) {
        NList split = Util.split(displayEvent.title, " ");
        if (split.size() > 3) {
            for (int i = 3; i < split.size(); i++) {
                if ("with".equalsIgnoreCase(split.get(i))) {
                    displayEvent.title = split.reconstruct(i);
                    return;
                }
            }
        }
        if (split.size() > 2 && (split.get(0).length() + split.get(1).length() >= 20)) {
            for (int i = 2; i < split.size(); i++) {
                if ("with".equalsIgnoreCase(split.get(i))) {
                    displayEvent.title = split.reconstruct(i);
                    return;
                }
            }
        }
    }

    void cleanBySplit(DisplayEvent displayEvent) {
        NList split = Util.split(displayEvent.title, " ");
        if (split.size() > 4) {
            for (int i = 4; i < split.size(); i++) {
                if ("by".equalsIgnoreCase(split.get(i))) {
                    if ("hosted".equals(split.get(i - 1))) {
                        displayEvent.title = split.reconstruct(i - 1);
                    } else {
                        displayEvent.title = split.reconstruct(i);
                    }
                    return;
                }
            }
        }
    }

    void cleanOfSplit(DisplayEvent displayEvent) {
        cleanWordSplit(displayEvent, "of", 6);
        if (displayEvent.title.endsWith(" of")) displayEvent.title = displayEvent.title.substring(0, displayEvent.title.length() - 3);
    }

    void cleanWordSplit(DisplayEvent displayEvent, String word, int minWordIndex) {
        NList split = Util.split(displayEvent.title, " ");
        if (split.size() > minWordIndex) {
            for (int i = minWordIndex; i < split.size(); i++) {
                if (word.equalsIgnoreCase(split.get(i))) {
                    displayEvent.title = split.reconstruct(i);
                    return;
                }
            }
        }
    }

    void cleanAndSplitFinal(DisplayEvent displayEvent) {
        NList split = Util.split(displayEvent.title, " ");
        List<String> andWords = andWords(displayEvent.languages);
        if (split.size() > 3) {
            for (int i = 3; i < split.size(); i++) {
                if (andWords.contains(split.get(i).toLowerCase())) {
                    displayEvent.title = split.reconstruct(i);
                    return;
                }
            }
        }
    }

    void cleanAndSplit(DisplayEvent displayEvent) {
        NList split = Util.split(displayEvent.title, " ");
        int cumulative = 0;
        List<String> andWords = andWords(displayEvent.languages);
        for (int i = 0; i < split.size(); i++) {
            if (cumulative > 20) {
                if (andWords.contains(split.get(i).toLowerCase())) {
                    displayEvent.title = split.reconstruct(i);
                    break;
                }
            }
            cumulative = cumulative + split.get(i).length() + 1;
        }
    }

    void cleanBetweenBrackets(DisplayEvent displayEvent) {
        NList split = Util.split(displayEvent.title, " ");
        if (split.size() > 2) {
            List<Integer> indexesToRemove = Util.list();
            boolean removing = false;
            for (int i = 2; i < split.size(); i++) {
                String s = split.get(i);
                if (s.contains("(")) removing = true;
                if (removing) indexesToRemove.add(i);
                if (s.contains(")")) removing = false;
            }
            displayEvent.title = split.reconstructExcept(indexesToRemove);
        }
    }

    void cleanFestivalEnding(DisplayEvent displayEvent) {
        NList split1 = Util.split(displayEvent.title, " ");
        if (split1.size() == 2 && "festival".equalsIgnoreCase(split1.get(1))) {
            displayEvent.title = split1.get(0);
        }
    }

    void cleanRatingEnd(DisplayEvent displayEvent) {
        List<String> suffixFilmIdentifiers = Util.list("u", "pg", "15", "18", "12a", "(#)", "(12a)");
        if (displayEvent.displayOrganizer.toLowerCase().endsWith("cinema")) {
            for (String string : suffixFilmIdentifiers) {
                if (displayEvent.title.toLowerCase().endsWith(" " + string)) {
                    displayEvent.title = displayEvent.title.substring(0, displayEvent.title.length() - string.length()).trim();
                }
            }
        }
    }

    void cleanSlashSpace(DisplayEvent displayEvent) {
        NList split = Util.split(displayEvent.title, " ");
        for (int i = 0; i < split.size(); i++) {
            if (split.get(i).length() > 25) split.set(i, split.get(i).replaceAll("/", " / "));
        }
        displayEvent.title = split.reconstruct();
    }

    void cleanCapitalAfterSlash(DisplayEvent displayEvent) {
        if (displayEvent.title.contains("/")) {
            int slashindex = displayEvent.title.indexOf("/");
            if (slashindex < displayEvent.title.length() - 2 && slashindex > 0) {
                displayEvent.title = displayEvent.title.substring(0, slashindex) + "/" + Character.toString(displayEvent.title.charAt(slashindex + 1)).toUpperCase() + displayEvent.title.substring(slashindex + 2);
            }
        }
        if (displayEvent.title.contains("\\")) {
            int slashindex = displayEvent.title.indexOf("\\");
            if (slashindex < displayEvent.title.length() - 2 && slashindex > 0) {
                displayEvent.title = displayEvent.title.substring(0, slashindex) + "\\" + Character.toString(displayEvent.title.charAt(slashindex + 1)).toUpperCase() + displayEvent.title.substring(slashindex + 2);
            }
        }
    }

    void filterType(List<DisplayEvent> eventList, String type) {
        if (type == null) return;
        final String typelocal = "film".equals(type) ? "cinema" : type;
        eventList.removeIf(e -> !typelocal.equalsIgnoreCase(e.category));
        if ("music".equals(type)) {
            eventList.removeIf(e -> Util.contains(e.title.toLowerCase(), musicalExclusions));
        }
    }

    void filterTypeByCompleteClassification(List<DisplayEvent> eventList, String type) {
        if (type == null) return;
        final String typelocal = "film".equals(type) ? "cinema" : type;
        eventList.removeIf(e -> !typelocal.equalsIgnoreCase(e.completeClassification));
        eventList.removeIf(e -> !Util.empty(e.symbol));
    }

    void filterRepeatNames(List<DisplayEvent> eventList) {
        List<String> longnames = Util.list();
        List<DisplayEvent> toRemove = Util.list();
        for (DisplayEvent a : eventList) {
            if (longnames.contains(a.title)) {
                toRemove.add(a);
                continue;
            }
            if (a.title.length() > 20) longnames.add(a.title);
        }
        eventList.removeAll(toRemove);
    }

    void filterRepeatEvents(List<DisplayEvent> eventList) {
        List<DisplayEvent> toRemove = Util.list();
        for (DisplayEvent a : eventList) {
            if (toRemove.contains(a)) continue;
            if (a.title.length() < 20) continue;
            for (DisplayEvent b : eventList) {
                if (b == a) continue;
                if (b.title.toLowerCase().startsWith(a.title.toLowerCase())) {
                    if (b.time.equals(a.time)) {
                        toRemove.add(b);
                    }
                }
            }
        }
        eventList.removeAll(toRemove);
    }

    void filterRepeatEventsSameOrganizer(List<DisplayEvent> eventList) {
        List<DisplayEvent> toRemove = Util.list();
        for (DisplayEvent a : eventList) {
            if (toRemove.contains(a)) continue;
            for (DisplayEvent b : eventList) {
                if (b == a) continue;
                if (b.title.equalsIgnoreCase(a.title) && b.time.equals(a.time) && b.displayOrganizer.equals(a.displayOrganizer)) {
                    toRemove.add(b);
                }
            }
        }
        eventList.removeAll(toRemove);
    }

    void filterRepeatFilms(List<DisplayEvent> eventList) {
        List<DisplayEvent> filmLike = Util.list();
        for (DisplayEvent htmlEvent : eventList) {
            if (htmlEvent.title.endsWith(" 15") && htmlEvent.title.length() > 8) {
                filmLike.add(htmlEvent);
            }
        }
        List<DisplayEvent> blacklist = Util.list();
        for (DisplayEvent filmlikeevent : filmLike) {
            for (DisplayEvent event : eventList) {
                if (event == filmlikeevent) continue;
                if (event.time.equals(filmlikeevent.time) && event.title.equals(filmlikeevent.title.substring(0, filmlikeevent.title.length() - 3))) {
                    blacklist.add(filmlikeevent);
                }
            }
        }
        eventList.removeAll(blacklist);
    }

    LinkedList<DisplayEvent> orderEventsInCity(com.events.date.Calendar.CalendarDate baseDate, List<DisplayEvent> events) {
        LinkedList<DisplayEvent> orderedEvents = new LinkedList<>();
        Set<String> iteratedTitles = Util.set();
        List<String> trustedReferences = Util.list();
        List<String> A1References = Util.list();
        LinkedList<DisplayEvent> trustedEvents = new LinkedList<>();
        LinkedList<DisplayEvent> A1Events = new LinkedList<>();
        LinkedList<DisplayEvent> repeatedList = new LinkedList<>();
        LinkedList<DisplayEvent> untrustedlist = new LinkedList<>();
        LinkedList<DisplayEvent> mediumLongTitles = new LinkedList<>();
        LinkedList<DisplayEvent> mediumTitles = new LinkedList<>();
        LinkedList<DisplayEvent> longTitles = new LinkedList<>();
        LinkedList<DisplayEvent> relegatedList = new LinkedList<>();
        LinkedList<DisplayEvent> latelist = new LinkedList<>();
        for (DisplayEvent event : events) {
            if (event.title.length() > 60 || PhraseUtil.wordCount(event.title) >= 10) {
                longTitles.add(event);
            } else if (removedCyrillic(event)) {
                relegatedList.add(event);
            } else if (containsProfanityWeak(event)) {
                relegatedList.add(event);
            } else if (Integer.parseInt(event.time.split(":")[0]) > 21) {
                latelist.add(event);
            } else if (event.title.length() > 35) {
                mediumLongTitles.add(event);
            } else if ("Y".equals(event.soldout)) {
                relegatedList.add(event);
            } else if (trustedOrgs.contains(event.ref) && !trustedReferences.contains(event.ref) && event.title.length() < 25 && !event.title.toLowerCase().contains(" talk")) {
                trustedEvents.add(event);
                trustedReferences.add(event.ref);
            } else if (A1Orgs.contains(event.ref) && !A1References.contains(event.ref) && event.title.length() < 25 && !event.title.toLowerCase().contains(" talk")) {
                A1Events.add(event);
                A1References.add(event.ref);
            } else if (event.title.length() > 25) {
                mediumTitles.add(event);
            } else if (untrustedOrgs.contains(event.ref)) {
                untrustedlist.add(event);
            } else if (Util.contains(event.title.toLowerCase(), RELEGATION_STRINGS)) {
                relegatedList.add(event);
            } else if (iteratedTitles.contains(event.title)) {
                relegatedList.add(event);
            } else if (numDatesAfterBaseDay(event.dates, baseDate) > 3) {
                repeatedList.add(event);
            } else {
                if (!orderedEvents.contains(event)) {
                    orderedEvents.add(event);
                }
            }
            iteratedTitles.add(event.title);
        }
        Collections.shuffle(trustedEvents);
        Collections.shuffle(A1Events);
        Collections.shuffle(untrustedlist);
        List<DisplayEvent> trustedQuality = Util.list(); //top
        List<DisplayEvent> trustedLowerquality = Util.list();
        deprioritizeLowerQuality(trustedEvents, trustedQuality, trustedLowerquality);
        List<DisplayEvent> A1Quality = Util.list(); //second
        List<DisplayEvent> A1Lowerquality = Util.list();
        deprioritizeLowerQuality(A1Events, A1Quality, A1Lowerquality);
        addOrPromote(orderedEvents, A1Lowerquality);
        addOrPromote(orderedEvents, trustedLowerquality);
        addOrPromote(orderedEvents, A1Quality);
        addOrPromote(orderedEvents, trustedQuality);
        addOrPromoteHighestTrustedQuality(orderedEvents, trustedQuality);
        addOrRelegate(orderedEvents, latelist); //bottom
        addOrRelegate(orderedEvents, relegatedList);
        addOrRelegate(orderedEvents, repeatedList);
        addOrRelegate(orderedEvents, untrustedlist);
        int lastsize = mediumLongTitles.size() + longTitles.size() + mediumTitles.size();         //shuffle in longer title events at the end of the list.
        List<DisplayEvent> existingLowPriorityEvents = new ArrayList<>(Util.sublist(orderedEvents, Math.max(orderedEvents.size() - lastsize, 0), orderedEvents.size()));
        orderedEvents.removeAll(existingLowPriorityEvents);
        existingLowPriorityEvents.addAll(mediumTitles);
        existingLowPriorityEvents.addAll(mediumLongTitles);
        existingLowPriorityEvents.addAll(longTitles);
        Collections.shuffle(existingLowPriorityEvents);
        orderedEvents.addAll(existingLowPriorityEvents);
        return orderedEvents;
    }

    void addOrRelegate(LinkedList<DisplayEvent> orderedEvents, LinkedList<DisplayEvent> latelist) {
        for (DisplayEvent event : latelist) {
            orderedEvents.remove(event);
            orderedEvents.addLast(event);
        }
    }

    void addOrPromoteHighestTrustedQuality(LinkedList<DisplayEvent> orderedEvents, List<DisplayEvent> trustedQuality) {
        DisplayEvent first = null;
        for (DisplayEvent displayEvent : trustedQuality) {
            if ("The O2 Arena".equals(displayEvent.ref)) {
                first = displayEvent;
                break;
            }
        }
        if (first != null) {
            orderedEvents.remove(first);
            orderedEvents.addFirst(first);
        }
    }

    void addOrPromote(LinkedList<DisplayEvent> events, List<DisplayEvent> eventsToAddOrPromote) {
        for (DisplayEvent event : eventsToAddOrPromote) {
            events.remove(event);
            events.addFirst(event);
        }
    }

    void deprioritizeLowerQuality(LinkedList<DisplayEvent> trustedEvents, List<DisplayEvent> trustedQuality, List<DisplayEvent> trustedLowerquality) {
        for (DisplayEvent event : trustedEvents) {
            if (!DEPRIORITIZED_CATEGORIES.contains(Util.lowercase(event.completeClassification)) && event.title.length() < 20 && !Util.contains(Util.lowercase(event.title), DEPRIORITIZED_WORDS)) {
                trustedQuality.add(event);
            } else {
                trustedLowerquality.add(event);
            }
        }
    }

    void filterBadCinema(List<DisplayEvent> eventList) {
        eventList.removeIf(e -> e.displayOrganizer.toLowerCase().endsWith("cinema") && Util.contains(e.title.toLowerCase(), BAD_CINEMA));
    }

    void filterUncertainTitles(List<DisplayEvent> eventList) {
        List<String> uncertainStrings = Util.list("to be confirmed shortly");
        eventList.removeIf(e -> Util.contains(e.title.toLowerCase(), uncertainStrings));
    }

    void filterCommonEvents(List<DisplayEvent> eventList) {
        eventList.removeIf(e -> "quiz night".equalsIgnoreCase(e.title));
    }

    static List<String> musicalExclusions = Util.list("quiz", "comedy", "theatre", "cabaret");

    void classify(List<DisplayEvent> events) {
        Map<String, String> classificationSymbol = map();
        classificationSymbol.put("music", "\u2669");
        List<String> cinemaIndicators = Util.list("cinema", "prince charles");
        List<String> comedyIndicators = Util.list("comedy", "bill murray", "camden head");
        List<String> comedyTitleIndicators = Util.list("comedy");
        List<String> theatreIndicators = Util.list("theatre");
        List<String> literatureIndicators = Util.list("literature");
        for (DisplayEvent n : events) {
            String selectedSymbol = Util.safeNull(classificationSymbol.get(Util.lowercase(n.category)));
            if (Util.empty(selectedSymbol) && n.link.contains("dice.fm")) {
                selectedSymbol = classificationSymbol.get("music");
            }
            String organizer = n.displayOrganizer.toLowerCase();
            String writtenClass = "";
            if (Util.contains(organizer.toLowerCase(), cinemaIndicators) || Util.lowercase(n.category).equals("cinema")) {
                writtenClass = "cinema";
            } else if (Util.contains(organizer.toLowerCase(), comedyIndicators) || Util.contains(n.title.toLowerCase(), comedyTitleIndicators) || Util.lowercase(n.category).equals("comedy")) {
                writtenClass = "comedy";
            } else if (Util.contains(organizer.toLowerCase(), theatreIndicators) || Util.lowercase(n.category).equals("theatre")) {
                writtenClass = "theatre";
            } else if (Util.contains(organizer.toLowerCase(), literatureIndicators) || Util.lowercase(n.category).equals("literature")) {
                writtenClass = "literature";
            } else if (Util.lowercase(n.category).equals("music") && !Util.contains(n.title.toLowerCase(), musicalExclusions)) {
                writtenClass = "music";
            } else if (n.link.contains("dice.fm") && !Util.contains(n.title.toLowerCase(), musicalExclusions)) {
                writtenClass = "music";
            }
            List<String> musicUrls = Util.list("/music/", "/recitals");
            if (Util.empty(writtenClass) && Util.empty(selectedSymbol)) {
                if (Util.contains(n.link.toLowerCase(), musicUrls)) {
                    writtenClass = "music";
                } else if (n.link.toLowerCase().contains("/comedy/")) {
                    writtenClass = "comedy";
                } else if (n.link.toLowerCase().contains("/films/")) {
                    writtenClass = "cinema";
                } else if (n.link.toLowerCase().contains("/exhibition/")) {
                    writtenClass = "exhibition";
                }
            }
            n.completeClassification = writtenClass;
            n.symbol = selectedSymbol;
        }
    }

    static int numDatesAfterBaseDay(List<com.events.date.Calendar.Date> dates, com.events.date.Calendar.CalendarDate baseDate) {
        int numDatesAfterBaseDay = 0;
        for (com.events.date.Calendar.Date date : dates) {
            if (com.events.date.Calendar.Date.strictlyBeforeAfterCheck(date, baseDate.toDate())) continue;
            numDatesAfterBaseDay++;
        }
        return numDatesAfterBaseDay;
    }

    boolean containsProfanityWeak(DisplayEvent displayEvent) {
        List<String> profainitiesWeak = Util.list("shits");
        NList words = Util.split(displayEvent.title, " ");
        for (int i = 0; i < words.size(); i++) {
            if (profainitiesWeak.contains(words.get(i).toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    boolean removedCyrillic(DisplayEvent displayEvent) {
        String title = displayEvent.title;
        if (title == null) return false;
        boolean found = false;
        List<Integer> wordsToRemove = Util.list();
        NList split = Util.split(title, " ");
        for (int j = 0; j < split.size(); j++) {
            String word = split.get(j);
            for (int i = 0; i < word.length(); i++) {
                int charencoding = Character.codePointAt(word, i);
                if (Util.between(charencoding, 1024, 1279)) {
                    wordsToRemove.add(j);
                    found = true;
                }
            }
        }
        displayEvent.title = split.reconstructExcept(wordsToRemove);
        return found;
    }
}