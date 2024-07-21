package com.events.date;

import com.events.*;

import java.util.*;
import java.util.regex.Matcher;

import static com.events.date.DateMatcher.MatcherLists.*;
import static com.events.Util.Lang.ENG;
import static com.events.Util.*;
import static com.events.date.DateTimeUtils.removeIndexPairs;
import static com.events.date.M_Lang.*;
import static com.events.date.M_Month.M_MONTH_ENG;
import static com.events.date.Months.MONTHS_STANDARD_ENG;
import static com.events.date.Weekdays.WEEKDAYS_ORDER;
import static java.lang.Integer.parseInt;
import static java.util.Collections.sort;

public abstract class DateMatcher {
    protected abstract DateMeta matchInternal(String text);

    public static List<com.events.date.Calendar.Date> matchSingle(String text) {
        List<com.events.date.Calendar.Date> dates = list();
        for (DateMatcher matcher : MATCHERS_SINGLE_DATE_READABLE) {
            DateMeta meta = matcher.matchInternal(text);
            if (meta != null) {
                addAll(dates, meta.dateList);
            }
        }
        return dates;
    }

    public static List<com.events.date.Calendar.Date> constructIntermediateDates(com.events.date.Calendar.Date parsedSimpleFrom, com.events.date.Calendar.Date parsedSimpleTo) {
        return constructIntermediateDates(parsedSimpleFrom, parsedSimpleTo, false);
    }

    public static List<com.events.date.Calendar.Date> constructIntermediateDates(com.events.date.Calendar.Date parsedSimpleFrom, com.events.date.Calendar.Date parsedSimpleTo, boolean withWeekday) {
        List<com.events.date.Calendar.Date> dates = list();
        for (com.events.date.Calendar.CalendarDate calendarDate : listDates(parsedSimpleFrom, parsedSimpleTo)) {
            com.events.date.Calendar.Date date = new com.events.date.Calendar.Date(calendarDate.dateDay, calendarDate.dateMonth, calendarDate.dateYear);
            if (withWeekday) {
                date.dayOfWeek = calendarDate.dayOfWeek;
            }
            dates.add(date);
        }
        sort(dates);
        return dates;
    }

    static List<com.events.date.Calendar.CalendarDate> listDates(com.events.date.Calendar.Date from, com.events.date.Calendar.Date to) {
        List<com.events.date.Calendar.CalendarDate> dates = list();
        if (com.events.date.Calendar.indexOf(from) == -1 || com.events.date.Calendar.indexOf(to) == -1) return dates;
        if (com.events.date.Calendar.indexOf(from) <= com.events.date.Calendar.indexOf(to)) {
            for (int i = com.events.date.Calendar.indexOf(from); i < com.events.date.Calendar.indexOf(to) + 1; i++) {
                dates.add(com.events.date.Calendar.get(i));
            }
        }
        return dates;
    }

    public static List<com.events.date.Calendar.Date> match(Util.StringMutable text, Context.LanguageContext langCtx, Context.DateContext dateCtx) {
        return match(text, langCtx, dateCtx, false);
    }

    static List<com.events.date.Calendar.Date> match(Util.StringMutable text, Context.LanguageContext langCtx, Context.DateContext dateCtx, boolean allowOld) {
        if (empty(text)) return null;
        MultiList<Integer, Integer> indexPairsInclExcl = new MultiList<>();
        List<com.events.date.Calendar.Date> dates = null;
        String stringWithoutSpecialCharacters = text.string.replaceAll("\u2021", " ").replaceAll(": ", "  ");
        DateMeta exclDateMeta = new ExcludedWeekdays().matchInternal(stringWithoutSpecialCharacters);
        if (empty(exclDateMeta.dateList)) {
            exclDateMeta = new ExcludedWeekday().matchInternal(stringWithoutSpecialCharacters);
        }
        indexPairsInclExcl.addAll(exclDateMeta.indexPairsInclExcl);
        List<DateMatcher> m_double_date = matchersDouble(langCtx, dateCtx);
        for (DateMatcher matcher : m_double_date) {
            DateMeta dateMeta = matcher.matchInternal(stringWithoutSpecialCharacters);
            if (DateMeta.empty(dateMeta)) continue;
            indexPairsInclExcl.addAll(dateMeta.indexPairsInclExcl);
            if (dates == null) {
                dates = dateMeta.between;
            }
        }
        for (DateMatcher matcher : matchersSingle(langCtx, dateCtx)) {
            DateMeta dateMeta = matcher.matchInternal(stringWithoutSpecialCharacters);
            if (DateMeta.empty(dateMeta)) continue;
            indexPairsInclExcl.addAll(dateMeta.indexPairsInclExcl);
            if (empty(dateMeta.dateList)) continue;
            if (matcher instanceof SingleDateMonth && dates != null && dates.size() == 1) {
                if (dates.get(0) != null && SingleDate.class.getSimpleName().equals(dates.get(0).note)) {
                    if (com.events.date.Calendar.Date.strictlyBeforeAfterCheck(dates.get(0), com.events.date.Calendar.currentDate())) {
                        com.events.date.Calendar.Date singledate = dates.get(0);
                        dateMeta.dateList.removeIf(d -> singledate.dateMonth.equals(d.dateMonth) && singledate.dateDay.equals(d.dateDay));
                    }
                }
            }
            if (empty(dateMeta.dateList)) continue;
            com.events.date.Calendar.Date date = dateMeta.dateList.get(0);
            for (com.events.date.Calendar.Date attempt : dateMeta.dateList) {
                if (com.events.date.Calendar.Date.beforeAfterCheck(com.events.date.Calendar.currentDate(), attempt)) {
                    date = attempt;
                    break;
                }
            }
            if (empty(dates) || (dates.size() == 1 && isOld(dates.get(0)))) {
                dates = list(date);
            }
        }
        if (empty(dates)) return null;
        removeIndexPairs(text, indexPairsInclExcl);
        if (!empty(exclDateMeta.dateList)) {
            List<String> weekdays_to_exclude = list();
            for (com.events.date.Calendar.Date date : exclDateMeta.dateList) {
                weekdays_to_exclude.add(date.dayOfWeek);
            }
            com.events.date.Calendar.setDayOfWeek(dates);
            dates.removeIf(e -> weekdays_to_exclude.contains(e.dayOfWeek));
        }
        sort(dates);
        if (!allowOld && com.events.date.Calendar.Date.strictlyBeforeAfterCheck(Util.last(dates), com.events.date.Calendar.currentDate())) return null;
        return dates;
    }

    static boolean isOld(com.events.date.Calendar.Date date) {
        if (date.dateYear == null) return false;
        return parseInt(date.dateYear) < 2022;
    }

    public static List<com.events.date.Calendar.Date> matchMonth(String text, Context.LanguageContext langCtx) {
        text = Util.lowercase(text);
        for (Util.Lang lang : Util.Lang.values()) {
            if (langCtx.lang(lang)) {
                DateMeta dateMeta = new Month(LANG_M_MONTH.get(lang), LANG_MONTHS_STANDARD.get(lang)).matchInternal(text);
                if (!empty(dateMeta.dateList)) return dateMeta.dateList;
            }
        }
        DateMeta dateMeta = new Month(M_MONTH_ENG, MONTHS_STANDARD_ENG).matchInternal(text);
        return DateMeta.dateList(dateMeta);
    }

    static List<com.events.date.Calendar.Date> matchMonthStrong(String text, Context.LanguageContext langCtx) {
        text = Util.lowercase(text);
        for (Util.Lang lang : Util.Lang.values()) {
            if (langCtx.lang(lang)) {
                DateMeta dateMeta = new MonthExact(LANG_M_MONTH.get(lang), LANG_MONTHS_STANDARD.get(lang)).matchInternal(text);
                if (!empty(dateMeta.dateList)) return dateMeta.dateList;
            }
        }
        DateMeta dateMeta = new MonthExact(M_MONTH_ENG, MONTHS_STANDARD_ENG).matchInternal(text);
        return DateMeta.dateList(dateMeta);
    }

    public static List<com.events.date.Calendar.Date> matchMonthYear(String text) {
        return DateMeta.dateList(new MonthYear().matchInternal(text));
    }

    public static List<com.events.date.Calendar.Date> matchDay(Util.StringMutable clean, Context.LanguageContext langCtx, Context.DateContext dateCtx) {
        List<DateMatcher> matchers = list();
        for (Util.Lang lang : Util.Lang.values()) {
            if (langCtx.lang(lang)) {
                matchers.add(new DayWeekday(lang));
                matchers.add(new WeekdayDay(lang));
            }
        }
        matchers.add(new WeekdayDay(ENG));
        matchers.add(new DayWeekday(ENG));
        List<com.events.date.Calendar.Date> dates = list();
        clean.string = clean.string.replaceAll("\u2021", " ").replaceAll("\u00A0", " ");
        MultiList<Integer, Integer> indexPairsInclExcl = new MultiList<>();
        for (DateMatcher partialMatcher : matchers) {
            DateMeta dateMeta = partialMatcher.matchInternal(clean.string);
            indexPairsInclExcl.underlying.addAll(dateMeta.indexPairsInclExcl);
            addAll(dates, DateMeta.dateList(dateMeta));
        }
        removeIndexPairs(clean, indexPairsInclExcl);
        return dates;
    }

    public static List<com.events.date.Calendar.Date> matchNoOverlapsPartial(String text, Context.LanguageContext langCtx, Context.DateContext dateCtx, com.events.date.Calendar.Date urlmonth) {
        List<com.events.date.Calendar.Date> noIndexOverlap = list();
        List<com.events.date.Calendar.Date> dates_to_return = list();
        com.events.date.Calendar.Date urlmonthdate = null;
        List<com.events.date.Calendar.Date> dates_month = matchMonthYear(text);
        if (empty(dates_month)) {
            dates_month = matchMonthStrong(text, langCtx);
        }
        MultiList<Integer, com.events.date.Calendar.Date> index_my = new MultiList<>();
        for (com.events.date.Calendar.Date date : dates_month) {
            index_my.add(new Util.Multi<>(date.indexPairs.get(0).a, date));
        }
        List<com.events.date.Calendar.Date> dates = matchDay(new Util.StringMutable(text), langCtx, dateCtx);
        for (com.events.date.Calendar.Date date : dates) {
            com.events.date.Calendar.Date date_my;
            if (urlmonthdate != null) {
                date_my = urlmonthdate.cloneMe();
            } else {
                date_my = Util.interpolateBefore(index_my, date.indexPairs.get(0).a);
            }
            com.events.date.Calendar.Date merge = com.events.date.Calendar.Date.merge(date_my, date);
            if (merge != null) dates_to_return.add(merge);
        }
        date_candidate:
        for (com.events.date.Calendar.Date candidate : dates_to_return) {
            if (empty(noIndexOverlap)) {
                noIndexOverlap.add(candidate);
            } else {
                Integer candidate_start = candidate.indexPairs.get(0).a;
                Integer candidate_end = Util.last(candidate.indexPairs.underlying).b;
                for (com.events.date.Calendar.Date chosen : noIndexOverlap) {
                    if (Util.overlap(candidate_start, candidate_end, chosen.indexPairs.get(0).a, Util.last(chosen.indexPairs.underlying).b)) {
                        continue date_candidate;
                    }
                }
                noIndexOverlap.add(candidate);
            }
        }
        noIndexOverlap.sort(Comparator.comparing(date -> date.indexPairs.get(0).a));
        return noIndexOverlap;
    }

    public static List<List<com.events.date.Calendar.Date>> matchNoOverlaps(String string, Context.LanguageContext langCtx, Context.DateContext dateCtx) {
        List<Integer> noIndexOverlap = list();
        List<List<com.events.date.Calendar.Date>> dates = matchDoublePrioritized(string, langCtx, dateCtx);
        dateCandidate:
        for (int i = 0; i < dates.size(); i++) {
            com.events.date.Calendar.Date dateRepresentative = Util.get(dates.get(i), 0);
            if (dateRepresentative == null) continue;
            if (empty(noIndexOverlap)) {
                noIndexOverlap.add(i);
            } else {
                Integer candidateStart = dateRepresentative.indexPairs.get(0).a;
                Integer candidateEnd = Util.last(dateRepresentative.indexPairs.underlying).b;
                for (Integer dateIndex : noIndexOverlap) {
                    com.events.date.Calendar.Date date = Util.get(dates.get(dateIndex), 0);
                    if (Util.overlap(candidateStart, candidateEnd, date.indexPairs.get(0).a, Util.last(date.indexPairs.underlying).b)) continue dateCandidate;
                }
                noIndexOverlap.add(i);
            }
        }
        List<List<com.events.date.Calendar.Date>> toReturn = list();
        for (Integer index : noIndexOverlap) {
            toReturn.add(dates.get(index));
        }
        toReturn.sort((o1, o2) -> {
            return o1.get(0).indexPairs.get(0).a.compareTo(o2.get(0).indexPairs.get(0).a);
        });
        return toReturn;
    }

    static List<List<com.events.date.Calendar.Date>> matchDoublePrioritized(String text, Context.LanguageContext langCtx, Context.DateContext dateCtx) {
        List<List<com.events.date.Calendar.Date>> list = list();
        for (DateMatcher matcher : matchersDoubleAllIndices(langCtx, dateCtx)) {
            DateMeta dateInternal = matcher.matchInternal(text);
            if (dateInternal != null) {
                list.addAll(dateInternal.betweenList);
            }
        }
        for (DateMatcher matcher : matchersSingle(langCtx, dateCtx)) {
            DateMeta dateMeta = matcher.matchInternal(text);
            if (dateMeta != null) {
                for (com.events.date.Calendar.Date date : dateMeta.dateList) {
                    list.add(list(date));
                }
            }
        }
        return list;
    }

    static List<DateMatcher> matchersDouble(Context.LanguageContext languageCtx, Context.DateContext dateCtx) {
        List<DateMatcher> matchers = matchersDoublePrioritized(languageCtx);
        if (dateCtx.dateReverse()) {
            matchers.add(new BetweenTwoDatesTextReverseStrong());
        }
        matchers.addAll(MATCHERS_DOUBLE_DATE);
        return matchers;
    }

    static List<DateMatcher> matchersDoubleAllIndices(Context.LanguageContext languageCtx, Context.DateContext dateCtx) {
        List<DateMatcher> matchers = matchersDoublePrioritized(languageCtx);
        if (dateCtx.dateReverse()) {
            matchers.add(new BetweenTwoDatesTextReverseStrong());
        }
        matchers.addAll(MATCHERS_DOUBLE_DATE_ALL_INDICES);
        return matchers;
    }

    static List<DateMatcher> matchersDoublePrioritized(Context.LanguageContext ctx) {
        List<DateMatcher> matchers = list();
        if (ctx.lang(Util.Lang.DEU)) {
        }
        return matchers;
    }

    static void getIndexPairs(String text, DateMeta meta, Matcher matcher) {
        getIndexPairs(text, meta, matcher.start(), matcher.end());
    }

    static void getIndexPairs(String text, DateMeta dateMeta, Integer startIndex, Integer endIndex) {
        dateMeta.indexPairsInclExcl.add(new Util.Multi<>(startIndex, endIndex));
        addProximalDateIndex(text, dateMeta, startIndex);
    }

    static String cleanText(String text) {
        return text.replaceAll("\u2021", " ").replaceAll("\u00A0", " ");
    }

    static void addProximalDateIndex(String text, DateMeta meta, int startIndex) {
        Integer dateIndex = Util.proximalIndex(text, startIndex, "date", 10);
        if (dateIndex != null) {
            meta.indexPairsInclExcl.add(new Util.Multi<>(dateIndex, dateIndex + 4));
        }
    }

    static Set<String> months(List<com.events.date.Calendar.Date> dates) {
        Set<String> months = Util.set();
        for (com.events.date.Calendar.Date date : dates) {
            months.add(date.dateMonth);
        }
        return months;
    }

    public static void cleanDates(Util.StringMutable clean, Collection<com.events.date.Calendar.Date> datesParameter, Context.LanguageContext langCtx) {
        List<com.events.date.Calendar.Date> dates = Util.sort(datesParameter);
        Set<String> months = months(dates);
        Set<String> weekdays = Util.set();
        if (Util.size(dates) == 1) {
            weekdays.add(com.events.date.Calendar.getDayOfWeek(dates.get(0)));
        }
        if (empty(dates) || empty(clean)) return;
        clean.string = clean.string.replaceAll("\u00A0", " ");
        Set<String> toReplace = Util.set("2017", "2018", "2019", "2020", "2021");
        for (Util.Lang lang : Util.Lang.values()) {
            if (langCtx.lang(lang)) {
                Map<String, String> langMonths = LANG_MONTHS_STANDARD.get(lang);
                if (!empty(langMonths)) {
                    toReplace.addAll(Util.keysForValues(months, langMonths));
                }
                Util.MapList<String, String> wdayLocalized = LANG_WEEKDAYS_STANDARD.get(lang);
                if (!empty(wdayLocalized)) {
                    for (String weekday : weekdays) {
                        List<String> localized = wdayLocalized.get(weekday);
                        if (empty(localized)) continue;
                        toReplace.addAll(localized);
                    }
                }
            }
        }
        toReplace.addAll(Util.keysForValues(months, MONTHS_STANDARD_ENG));
        toReplace.addAll(WEEKDAYS_ORDER);
        toReplace.remove("sun");
        toReplace.remove("may");
        for (String monthOrDay : toReplace) {
            clean.set(PhraseUtil.removeWordSafely(clean.string, monthOrDay));
        }
        List<com.events.date.Calendar.Date> startEndDates = list(dates.get(0));
        if (dates.size() > 1) {
            startEndDates.add(Util.last(dates));
        }
        for (com.events.date.Calendar.Date date : startEndDates) {
            clean.set(PhraseUtil.removeWordSafely(clean.string, "[0]?" + date.dateDay + "(th|nd|st|rd) of"));
            clean.set(PhraseUtil.removeWordSafely(clean.string, "( the )?[0]?" + date.dateDay + "(th|nd|st|rd)?"));
        }
        for (com.events.date.Calendar.Date date : startEndDates) {
            clean.set(PhraseUtil.removeWordSafely(clean.string, date.dateDay));
        }
        clean.set(PhraseUtil.removeWordSafely(clean.string, "open daily"));
        clean.set(PhraseUtil.removeWordSafely(clean.string, "daily"));
    }

    public abstract static class DayTimeMatcher {
        abstract Map<String, Time> match(String text);

        public static Map<String, Time> matchWeekdayTime(String text) {
            HashMap<String, Time> map = new HashMap<>();
            for (DayTimeMatcher dayTimeMatcher : WEEKDAY_MATCHERS) {
                Map<String, Time> newMap = dayTimeMatcher.match(text);
                Util.addSafe(map, newMap);
            }
            return map;
        }

        static List<Integer> weekdayBetweenIndices(String weekdayOne, String weekdayTwo) {
            List<Integer> weekdayIndices;
            if (WEEKDAYS_ORDER.indexOf(weekdayOne) < 0 || WEEKDAYS_ORDER.indexOf(weekdayTwo) < 0) return null;
            if (WEEKDAYS_ORDER.indexOf(weekdayTwo) >= WEEKDAYS_ORDER.indexOf(weekdayOne)) {
                weekdayIndices = Util.between(WEEKDAYS_ORDER.indexOf(weekdayOne), WEEKDAYS_ORDER.indexOf(weekdayTwo));
            } else {
                weekdayIndices = Util.between(0, WEEKDAYS_ORDER.indexOf(weekdayTwo));
                weekdayIndices.addAll(Util.between(WEEKDAYS_ORDER.indexOf(weekdayOne), 6));
            }
            return weekdayIndices;
        }
    }

    static class MatcherLists {
        static final List<DateMatcher> MATCHERS_SINGLE_DATE_READABLE = list(new SingleDate(ENG), new SingleDateMonth(ENG), new SingleDateHyphenatedYMD(), new SingleDateHyphenatedDMY(), new SingleDateSlashDot(), new SingleDateReverseStrong(), new SingleDateReverseOrdinal(), new SingleDateReverseMDW(), new SingleDateReverseWMD());
        static final List<DateMatcher> MATCHERS_DOUBLE_DATE = list(new BetweenTwoDatesText(), new TwoDates(), new BetweenTwoDatesTextReverse(), new BetweenTwoDatesNumeric(), new BetweenDateSeason(), //EXCLUSIONARY - but this order is important
                new FromDate(), new UntilDate(), new OpenDaily());
        static final List<DateMatcher> MATCHERS_DOUBLE_DATE_ALL_INDICES = list(new BetweenTwoDatesText(true), new TwoDates(), new BetweenTwoDatesTextReverse(), new BetweenTwoDatesNumeric(), new BetweenDateSeason(), //EXCLUSIONARY - but this order is important
                new FromDate(), new UntilDate(), new OpenDaily());
        static final List<DateMatcher> MATCHERS_SINGLE_DATE_NON_TEXTUAL = list(new SingleMillis(), new SingleIsoFormat());
        static final List<DateMatcher> MATCHERS_SINGLE_DATE = union(MATCHERS_SINGLE_DATE_READABLE, MATCHERS_SINGLE_DATE_NON_TEXTUAL);
        static final List<DayTimeMatcher> WEEKDAY_MATCHERS = list(new BetweenDaysSingleTime(), new BetweenDaysSingleTimeAmPm(), new WeekdaysSingleTime(), new WeekdaysSingleTimeAmPm(), new SingleTimeWeekdayBracketed());
    }

    public static List<String> matchersDoubleNames(Context.LanguageContext languageCtx, Context.DateContext dateCtx) {
        List<String> list = list();
        for (DateMatcher dateMatcher : matchersDouble(languageCtx, dateCtx)) {
            list.add(dateMatcher.getClass().getSimpleName());
        }
        return list;
    }

    public static List<DateMatcher> matchersSingle(Context.LanguageContext langCtx, Context.DateContext dateCtx) {
        List<DateMatcher> matchers = matchersSinglePrioritized(langCtx);
        if (dateCtx.dateReverse()) {
            matchers.add(new SingleDateReverse());
            matchers.add(new SingleDateReverseEnd());
        } else if (dateCtx.dateMwd()) {
            matchers.add(new SingleDateMWDAlt());
        } else if (dateCtx.dateDottedWdmy()) {
            matchers.add(new SingleDateDottedWDMYAlt());
        } else {
            matchers.addAll(MATCHERS_SINGLE_DATE);
        }
        return matchers;
    }

    static List<DateMatcher> matchersSinglePrioritized(Context.LanguageContext langCtx) {
        List<DateMatcher> matchers = list();
        for (Util.Lang lang : Util.Lang.values()) {
            if (langCtx.lang(lang)) {
                matchers.add(new SingleDate(lang));
                matchers.add(new SingleDateMonth(lang));
            }
        }
        return matchers;
    }
}