package com.events.date;

import com.events.Context;
import com.events.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.events.date.M_Month.M_MONTH_DEU;
import static com.events.date.M_Month.M_MONTH_ENG;
import static com.events.date.M_Static.*;
import static com.events.date.M_Weekday.*;
import static com.events.Util.Lang.DEU;
import static com.events.Util.Lang.ENG;
import static com.events.date.Months.MONTHS_STANDARD_DEU;
import static com.events.date.Months.MONTHS_STANDARD_ENG;
import static com.events.date.Weekdays.WEEKDAYS_STANDARD_DEU;
import static com.events.date.Weekdays.WEEKDAYS_STANDARD_ENG;

public class M_Lang {
    public static final Map<Util.Lang, Map<String, String>> LANG_MONTHS_STANDARD;
    public static final Map<Util.Lang, Util.MapList<String, String>> LANG_WEEKDAYS_STANDARD;
    public static final Map<Util.Lang, String> LANG_M_MONTH;
    public static final Map<Util.Lang, String> LANG_M_WEEKDAY;
    public static final Map<Util.Lang, String> LANG_M_START_INDICATORS;
    public static final Map<Util.Lang, String> LANG_M_WEEKDAYO_SPACE_AFTER;
    public static final Map<Util.Lang, String> LANG_M_ORDINAL;
    public static final Map<Util.Lang, List<String>> LANG_PHRASES_TO_CLEAN;
    public static final Map<Util.Lang, List<String>> LANG_LOWERCASE;
    public static List<String> PHRASES_TO_CLEAN_ENG = Util.list("special event", "new exhibition", "limited availability", "available", "order tickets", "buy tickets", "book tickets", "book ticket", "book online", "book a table", "booking required", "get tickets", "additional tickets", "tickets coming soon", "tickets", "booking fee", "booking", "members go free", "a free", "free entry", "free event", "foyer", "add to wishlist", "bank holiday sunday", "bank holiday", "lecture series", "seminar series", "on the gate", "cash on the door", "on the door", "ticket holder", "ticket price", "advance day ticket", "day pass", "google calendar ics", "last entry", "google maps", "google map", "visually impaired only", "visually impaired", "public", "for visit", "register", "on-sale", "on sale now", "on sale", "dates and times vary", "dates & times", "dates and times", "various dates", "various times", "find out more", "more details to follow", "more details", "find out", "read more", "doors open at", "book now", "category:", "more information", "more info", "view availability", "find tickets", "tickets now", "this week", "this month", "short courses", "every week", "now playing", "buy now", "buy");

    static {
        LANG_LOWERCASE = new HashMap<>();
        LANG_LOWERCASE.put(ENG, Context.LanguageContext.LOWERCASE_ENG);
        LANG_PHRASES_TO_CLEAN = new HashMap<>();
        LANG_PHRASES_TO_CLEAN.put(ENG, PHRASES_TO_CLEAN_ENG);
        LANG_M_ORDINAL = new HashMap<>();
        LANG_M_ORDINAL.put(ENG, M_ORDINAL_ENG);
        LANG_M_ORDINAL.put(DEU, M_EMPTY);
        LANG_M_WEEKDAYO_SPACE_AFTER = new HashMap<>();
        LANG_M_WEEKDAYO_SPACE_AFTER.put(ENG, M_WEEKDAYO_SPACE_AFTER_ENG);
        LANG_M_WEEKDAYO_SPACE_AFTER.put(DEU, M_WEEKDAYO_SPACE_AFTER_DEU);
        LANG_M_WEEKDAY = new HashMap<>();
        LANG_M_WEEKDAY.put(ENG, M_WEEKDAY_ENG);
        LANG_M_WEEKDAY.put(DEU, M_WEEKDAY_DEU);
        LANG_M_START_INDICATORS = new HashMap<>();
        LANG_M_START_INDICATORS.put(ENG, M_START_INDICATORS_ENG);
        LANG_M_START_INDICATORS.put(DEU, M_EMPTY);
        LANG_M_MONTH = new HashMap<>();
        LANG_M_MONTH.put(ENG, M_MONTH_ENG);
        LANG_M_MONTH.put(DEU, M_MONTH_DEU);
        LANG_MONTHS_STANDARD = new HashMap<>();
        LANG_MONTHS_STANDARD.put(ENG, MONTHS_STANDARD_ENG);
        LANG_MONTHS_STANDARD.put(DEU, MONTHS_STANDARD_DEU);
        LANG_WEEKDAYS_STANDARD = new HashMap<>();
        LANG_WEEKDAYS_STANDARD.put(ENG, WEEKDAYS_STANDARD_ENG);
        LANG_WEEKDAYS_STANDARD.put(DEU, WEEKDAYS_STANDARD_DEU);
    }
}
