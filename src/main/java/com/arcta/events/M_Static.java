package com.arcta.events;
import java.util.*;
import static com.arcta.events.M_Month.M_MONTHO_ENG;
import static com.arcta.events.M_Month.M_MONTH_ENG;
import static com.arcta.events.Util.map;
class M_Static { static final List<String> MONTHS_LONG = Util.list("january", "february", "march", "april", "may", "june", "july", "august", "september","october", "november" , "december" );
    static final List<String> MONTHS_ORDER = Util.list("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec");
    static final List<String> DAY_ORDINAL = Util.list( "st","nd","rd","th","th","th","th","th","th","th", "th","th","th","th","th","th","th","th","th","th","st","nd","rd","th","th","th","th","th","th","th","st");
    static final Map<String, Integer> MONTH_DAY_MAP; static final Map<String, Calendar.Date> SEASONS;
    static final String M_YEAR = "(20)?([0-9]{2})";
    static final String M_YEARO = "(20)?([0-9]{2})?";
    static final String M_YEAR_STRONG = "(20[0-9]{2})";
    static final String M_YEAR_STRONGO = "(20[0-9]{2})?";
    static final String M_SEASON = "(spring|summer|autumn|winter)";
    static final String M_DAY = "([0-9]{1,2})";
    static final String M_DAYO = M_DAY + "?";
    static final String M_ORDINAL_ENG = "(th|nd|st|rd)";
    static final String M_ORDINALO_ENG = M_ORDINAL_ENG + "?";
    static final String MW = "[\\s|\u00A0]";
    static final String MWO = MW + "?";
    static final String MWO2 = MW + "{0,2}";
    static final String MWO3 = MW + "{0,3}";
    static final String SPACES = MW + "+";
    static final String SPACESO = MW + "*";
    static final String M_DAY_ORDINAL = M_DAY + MWO + M_ORDINAL_ENG;
    static final String M_DAY_ORDINALO = M_DAY + MWO + M_ORDINALO_ENG;
    static final String NEGATIVE_LOOKBEHIND_DIGITS = "(?<!:|0|1|2|3|4|5|6|7|8|9)";
    static final String NEGATIVE_LOOKAHEAD_DIGITS = "(?!:|0|1|2|3|4|5|6|7|8|9)";
    static final String M_DAY_MONTHO_YEARO; static final String M_DAY_MONTH_YEAR; static final String M_DAY_MONTH_YEARO; static final String M_DAYO_MONTH_YEARO;     // -------------- FULL DATE MATCHERS ----------------
    static final String M_START_INDICATORS_ENG = "(starts |starting |start |entry | the | on )";
    static final String M_START_INDICATORSO_ENG = M_START_INDICATORS_ENG + "?";
    static final String M_EMPTY = "()";
    static {SEASONS = map(); SEASONS.put("spring", new Calendar.Date("1", "mar", null)); SEASONS.put("summer", new Calendar.Date("1", "jun", null)); SEASONS.put("autumn", new Calendar.Date("1", "sep", null)); SEASONS.put("winter", new Calendar.Date("1", "dec", null));
        MONTH_DAY_MAP = map(); MONTH_DAY_MAP.put("jan", 31); MONTH_DAY_MAP.put("feb", 28); MONTH_DAY_MAP.put("mar", 31); MONTH_DAY_MAP.put("apr", 30); MONTH_DAY_MAP.put("may", 31); MONTH_DAY_MAP.put("jun", 30); MONTH_DAY_MAP.put("jul", 31); MONTH_DAY_MAP.put("aug", 31); MONTH_DAY_MAP.put("sep", 30); MONTH_DAY_MAP.put("oct", 31); MONTH_DAY_MAP.put("nov", 30); MONTH_DAY_MAP.put("dec", 31);
        M_DAY_MONTHO_YEARO = M_DAY_ORDINALO + SPACESO + M_MONTHO_ENG + "[,]?" + MWO + M_YEARO;
        M_DAY_MONTH_YEAR = M_DAY + MW + M_MONTH_ENG + "[,]? " + M_YEAR;
        M_DAY_MONTH_YEARO = M_DAY_ORDINALO + SPACES + M_MONTH_ENG + "[,]?" + MWO3 + M_YEARO;
        M_DAYO_MONTH_YEARO = M_DAYO + MWO + M_MONTH_ENG + "[,]?" + MWO + M_YEARO;}
    static List<String> order(Collection<String> collection){ List<String> ordered = new ArrayList<>(collection);
        Collections.sort(ordered);
        Collections.reverse(ordered); return ordered;}
    static String constructMatcher(Util.MapList<String,String> input){return constructMatcher(input, "|");}
    static String constructMatcher(Util.MapList<String,String> input, String delimiter){return "(" + Util.string(order(input.listvalues()), delimiter) + ")";}
    static String constructMatcher(Map<String,String> input){return constructMatcher(input, "|");}
    static String constructMatcher(Map<String,String> input, String delimiter){ return "(" + Util.string(order(input.keySet()), delimiter) + ")";}}
