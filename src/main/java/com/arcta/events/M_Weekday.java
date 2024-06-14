package com.arcta.events;
import static com.arcta.events.Weekdays.*;
import static com.arcta.events.M_Static.order;
import static com.arcta.events.Util.opt;
class M_Weekday { static final String M_WEEKDAY_ENG; static final String M_WEEKDAY_DEU; static final String M_WEEKDAYO_ENG; static final String M_WEEKDAYO_SPACE_AFTER_ENG; static final String M_WEEKDAYO_SPACE_AFTER_DEU; static final String M_WEEKDAY_LONG; static final String M_WEEKDAYO_COMMA; static final String M_WEEKDAY_COMMA_SPACE_AFTER; static final String M_WEEKDAYO_COMMA_SPACE_AFTER_DEU;
    static { M_WEEKDAY_ENG = M_Static.constructMatcher(WEEKDAYS_STANDARD_ENG);
        M_WEEKDAY_DEU = M_Static.constructMatcher(WEEKDAYS_STANDARD_DEU);
        M_WEEKDAYO_ENG = opt(M_WEEKDAY_ENG);
        M_WEEKDAYO_SPACE_AFTER_ENG = opt(M_Static.constructMatcher(WEEKDAYS_STANDARD_ENG, " |"));
        M_WEEKDAYO_SPACE_AFTER_DEU = opt(M_Static.constructMatcher(WEEKDAYS_STANDARD_DEU, " |"));
        StringBuilder weekdayComma = new StringBuilder(); StringBuilder weekdayCommaSpace = new StringBuilder(); StringBuilder weekdayCommaSpaceDeu = new StringBuilder();
        for (String day : order(WEEKDAYS_STANDARD_ENG.listvalues())) { weekdayComma.append(day + "|" + day + ",|");
            weekdayCommaSpace.append(day + " |" + day + ", |");}
        M_WEEKDAYO_COMMA = opt("(" + Util.substringRemoveLast(weekdayComma) + ")");
        M_WEEKDAY_COMMA_SPACE_AFTER = Util.substringRemoveLast(weekdayCommaSpace);
        for (String tag : order(WEEKDAYS_STANDARD_DEU.listvalues())) { weekdayCommaSpaceDeu.append(tag + " |"  + tag + ", |");}
        M_WEEKDAYO_COMMA_SPACE_AFTER_DEU = "(" + Util.substringRemoveLast(weekdayCommaSpaceDeu) + ")?";
        M_WEEKDAY_LONG = "(" + Util.string(WEEKDAYS_LONG, "|") + ")";}}