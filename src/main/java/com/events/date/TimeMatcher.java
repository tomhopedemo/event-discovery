package com.events.date;

import com.events.Context;
import com.events.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.events.EventLink.LINK_TIME_AVOID_PHRASES_DIRECTIONAL;
import static com.events.EventLink.LINK_TIME_AVOID_PHRASES_NON_DIRECTIONAL;
import static com.events.date.M_Static.SPACESO;
import static com.events.Util.list;

public abstract class TimeMatcher {
    abstract List<Time> match(Util.StringMutable clean);

    List<Time> match(Util.StringMutable clean, boolean exact_match) {
        return match(clean);
    }

    Time match_single(Util.StringMutable clean, boolean exact_match) {
        List<Time> match = match(clean, exact_match);
        if (Util.empty(match)) return null;
        for (Time time : match) {
            if (time.priority) {
                return time;
            }
        }
        return match.get(0);
    }

    List<Time> matchPreferredFirst(Util.StringMutable clean, boolean exact_match) {
        List<Time> matches = match(clean, exact_match);
        if (Util.empty(matches)) return null;
        return preferredTime(matches);
    }

    static final String M_TIME = "([0-9]{1,2})" + "[:|\\.]" + "([0-5][0|5])";
    static final String M_TIME_AM = M_TIME + SPACESO + "(am|pm|p\\.m\\.)";
    static final String M_TIME_AMO = M_TIME + SPACESO + "(am|pm)?";

    static List<Time> preferredTime(List<Time> match) {
        List<Time> preferred = list();
        List<Time> unpreferred = list();
        List<Time> other = list();
        for (Time time : match) {
            if (time == null) continue;
            if (time.priority) {
                preferred.add(time);
            } else if (Time.beforeAfterCheck(new Time("21", "50"), time) || Time.beforeAfterCheck(time, new Time("11", "50"))) {
                unpreferred.add(time);
            } else {
                other.add(time);
            }
        }
        preferred.addAll(other);
        preferred.addAll(unpreferred);
        return preferred;
    }

    public static Time matchOverall(Util.StringMutable clean, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        return match(clean, langCtx, timeCtx, true);
    }

    public static Time match(Util.StringMutable clean, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        return match(clean, langCtx, timeCtx, false);
    }

    public static Time match(Util.StringMutable clean, Context.LanguageContext langCtx, Context.TimeContext timeCtx, boolean overall) {
        Time preferredTime = null;
        Util.MultiList<Integer, Integer> indexPairsInclExclDouble = new Util.MultiList<>();
        List<TimeMatcher> doubleMatchers = matchersDouble(langCtx, timeCtx);
        for (TimeMatcher matcher : doubleMatchers) {
            List<Time> times = matcher.matchPreferredFirst(clean, false);
            if (Util.empty(times)) continue;
            if (overall) {
                preferredTime = preferredTime(list(times.get(0), preferredTime)).get(0);
            } else {
                if (preferredTime == null) preferredTime = times.get(0);
            }
            for (Time time : times) {
                indexPairsInclExclDouble.underlying.add(new Util.Multi<>(time.startIndex, time.endIndex));
            }
        }
        if (preferredTime != null) DateTimeUtils.removeIndexPairs(clean, indexPairsInclExclDouble);
        List<TimeMatcher> singleMatchers = matchersSingle(langCtx, timeCtx);
        singleMatchers.add(new TimeText());
        Util.MultiList<Integer, Integer> indexPairsInclExclSingle = new Util.MultiList<>();
        for (TimeMatcher matcher : singleMatchers) {
            List<Time> times = matcher.matchPreferredFirst(clean, false);
            if (Util.empty(times)) continue;
            if (overall) {
                preferredTime = preferredTime(list(times.get(0), preferredTime)).get(0);
            } else {
                if (preferredTime == null) preferredTime = times.get(0);
            }
            for (Time time : times) {
                indexPairsInclExclSingle.underlying.add(new Util.Multi<>(time.startIndex, time.endIndex));
            }
        }
        if (preferredTime != null) DateTimeUtils.removeIndexPairs(clean, indexPairsInclExclSingle);
        return preferredTime;
    }

    public static Time matchAvoidIndices(Util.StringMutable clean, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        List<Integer> avoidIndicesDirectional = Util.indices(clean.string, LINK_TIME_AVOID_PHRASES_DIRECTIONAL);
        List<Integer> avoidIndicesNonDirectional = Util.indices(clean.string, LINK_TIME_AVOID_PHRASES_NON_DIRECTIONAL);
        Time bestCandidateOverall = null;
        Util.MultiList<Integer, Integer> indexPairsInclExcl = new Util.MultiList<>();
        for (Time time : matchDouble(clean, langCtx, timeCtx)) {
            indexPairsInclExcl.add(new Util.Multi<>(time.startIndex, time.endIndex));
            bestCandidateOverall = best_candidate(avoidIndicesDirectional, avoidIndicesNonDirectional, bestCandidateOverall, time);
        }
        DateTimeUtils.removeIndexPairs(clean, indexPairsInclExcl);
        avoidIndicesDirectional = Util.indices(clean.string, LINK_TIME_AVOID_PHRASES_DIRECTIONAL);
        avoidIndicesNonDirectional = Util.indices(clean.string, LINK_TIME_AVOID_PHRASES_NON_DIRECTIONAL);
        for (Time time : matchSingle(clean, langCtx, timeCtx)) {
            indexPairsInclExcl.add(new Util.Multi<>(time.startIndex, time.endIndex));
            bestCandidateOverall = best_candidate(avoidIndicesDirectional, avoidIndicesNonDirectional, bestCandidateOverall, time);
        }
        DateTimeUtils.removeIndexPairs(clean, indexPairsInclExcl);
        return bestCandidateOverall;
    }

    public static Time matchSingle(String text, boolean exactMatch, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        if (Util.empty(text)) return null;
        for (TimeMatcher matcher : matchersSingle(langCtx, timeCtx)) {
            Time match = matcher.match_single(new Util.StringMutable(text), exactMatch);
            if (match != null) return match;
        }
        return null;
    }

    public static void cleanTimes(Util.StringMutable clean) {
        if (Util.empty(clean)) return;
        clean.set(clean.string.replaceAll("( at )?[0-9]{2}(:|\\.)[0-9]{2}[ ]?(pm|am)(?![a-zA-Z])", ""));
        clean.set(clean.string.replaceAll("( at )?[0-9]{2}(:|\\.)[0-9]{2}[ ]?", ""));
        clean.set(clean.string.replaceAll("( at )?[0-9](:|\\.)[0-9]{2}[ ]?(pm|am)(?![a-zA-Z])", ""));
        clean.set(clean.string.replaceAll("( at )?[0-9](:|\\.)[0-9]{2}[ ]?", ""));
        clean.set(clean.string.replaceAll("( at )?[1-9]\\-[1-9](pm|am)(?![a-zA-Z])", ""));
        clean.set(clean.string.replaceAll("( at )?[0-9]{2}[ ]?(pm|am)(?![a-zA-Z])", ""));
        clean.set(clean.string.replaceAll("( at )?[0-9][ ]?(pm|am)(?![a-zA-Z])", ""));
        clean.set(clean.string.replaceAll("open til", ""));
        clean.set(clean.string.replaceAll("open until", ""));
    }

    public static List<Time> matchNoOverlaps(String text, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        List<Integer> no_index_overlap = list();
        List<Time> times = matchDoublePrioritized(text, langCtx, timeCtx);
        List<Time> time_representatives = list();
        for (Time time : times) {
            if (time != null) {
                time_representatives.add(time);
            }
        }
        time_candidate:
        for (int i = 0; i < time_representatives.size(); i++) {
            Time candidate_time = time_representatives.get(i);
            if (Util.empty(no_index_overlap)) {
                no_index_overlap.add(i);
            } else {
                Integer candidate_start = candidate_time.startIndex;
                Integer candidate_end = candidate_time.endIndex;
                for (Integer time_index : no_index_overlap) {
                    Time time = time_representatives.get(time_index);
                    if (Util.overlap(candidate_start, candidate_end, time.startIndex, time.endIndex)) continue time_candidate;
                }
                no_index_overlap.add(i);
            }
        }
        List<Time> to_return = new ArrayList<>();
        for (Integer index : no_index_overlap) {
            to_return.add(times.get(index));
        }
        to_return.sort(new Comparator<Time>() {
            public int compare(Time o1, Time o2) {
                return o1.startIndex.compareTo(o2.startIndex);
            }
        });
        return to_return;
    }

    static List<Time> matchDoublePrioritized(String text, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        List<Time> times = new ArrayList<>();
        for (TimeMatcher matcher : matchers(langCtx, timeCtx)) {
            List<Time> match = matcher.match(new Util.StringMutable(text));
            Util.safeAdd(times, match);
        }
        return times;
    }

    public static Time exactMatchSingle(String text, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        return matchSingle(text, true, langCtx, timeCtx);
    }

    static List<Time> matchDouble(Util.StringMutable clean, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        List<Time> times = new ArrayList<>();
        List<TimeMatcher> matchers = matchersDouble(langCtx, timeCtx);
        matchers.forEach(m -> Util.safeAdd(times, m.match(clean)));
        return times;
    }

    public static List<Time> matchSingle(Util.StringMutable clean, Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        List<Time> times = new ArrayList<>();
        for (TimeMatcher matcher : matchersSingle(langCtx, timeCtx)) {
            List<Time> time_indicative = matcher.match(clean);
            if (Util.empty(time_indicative)) continue;
            times.addAll(time_indicative);
        }
        return times;
    }

    static Time best_candidate(List<Integer> avoidIndicesDirectional, List<Integer> avoid_indices__non_directional, Time incumbent, Time candidate) {
        Integer distanceDirectional = Util.minDifferenceDirectional(avoidIndicesDirectional, candidate.startIndex);
        if (distanceDirectional == null) distanceDirectional = 1000000;
        Integer distanceNonDirectional = Util.minDifference(avoid_indices__non_directional, candidate.startIndex);
        if (distanceNonDirectional == null) distanceNonDirectional = 1000000;
        Integer distance = Math.min(distanceDirectional, distanceNonDirectional);
        if (incumbent == null || (incumbent.distance < 100 && distance > incumbent.distance) || (incumbent.distance > 100 && Integer.valueOf(incumbent.getHour()) < 13 && Integer.valueOf(candidate.getHour()) >= 13)) {
            candidate.distance = distance;
            return candidate;
        }
        return incumbent;
    }

    static List<TimeMatcher> matchers(Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        List<TimeMatcher> matchers = matchersDouble(langCtx, timeCtx);
        matchers.addAll(matchersSingle(langCtx, timeCtx));
        return matchers;
    }

    static List<TimeMatcher> matchersSingle(Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        List<TimeMatcher> matchers = matchersSinglePrioritized(langCtx);
        matchers.add(new SingleTimeAmPm());
        matchers.add(new SingleTime(timeCtx.timePm(), timeCtx.timeDoors()));
        matchers.add(new HourAmPm());
        return matchers;
    }

    static List<TimeMatcher> matchersDouble(Context.LanguageContext langCtx, Context.TimeContext timeCtx) {
        List<TimeMatcher> matchers = matchersDoublePrioritized(langCtx);
        matchers.add(new BetweenTwoTimes(timeCtx.timePm()));
        matchers.add(new BetweenTwoTimesAmPm_A());
        matchers.add(new BetweenTwoTimesAmPm());
        return matchers;
    }

    static List<TimeMatcher> matchersDoublePrioritized(Context.LanguageContext ctx) {
        List<TimeMatcher> matchers = list();
        if (ctx.lang(Util.Lang.DEU)) {
        }
        return matchers;
    }

    static List<TimeMatcher> matchersSinglePrioritized(Context.LanguageContext ctx) {
        List<TimeMatcher> matchers = list();
        if (ctx.lang(Util.Lang.DEU)) {
        }
        return matchers;
    }
}