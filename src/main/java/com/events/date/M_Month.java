package com.events.date;

import com.events.Util;

import static com.events.date.M_Static.order;
import static com.events.date.Months.MONTHS_STANDARD_DEU;
import static com.events.date.Months.MONTHS_STANDARD_ENG;

public class M_Month {
    public static final String M_MONTH_ENG;
    public static final String M_MONTH_DEU;
    public static final String M_MONTH_NEG_LOOKAHEAD;
    public static final String M_MONTHO_ENG;
    public static final String M_MONTHS_SPACE_BEFORE_AND_AFTER;
    public static final String M_MONTHS_SPACE_AFTER;

    static {
        M_MONTH_ENG = M_Static.constructMatcher(MONTHS_STANDARD_ENG);
        M_MONTH_DEU = M_Static.constructMatcher(MONTHS_STANDARD_DEU);
        M_MONTH_NEG_LOOKAHEAD = "(?!" + Util.string(order(MONTHS_STANDARD_ENG.keySet())) + ")";
        M_MONTHO_ENG = M_MONTH_ENG + "?";
        StringBuilder monthsSpaceBeforeAfter = new StringBuilder();
        for (String month : order(MONTHS_STANDARD_ENG.keySet())) {
            monthsSpaceBeforeAfter.append(" " + month + " |");
        }
        M_MONTHS_SPACE_BEFORE_AND_AFTER = Util.substringRemoveLast(monthsSpaceBeforeAfter);
        M_MONTHS_SPACE_AFTER = Util.string(order(MONTHS_STANDARD_ENG.keySet()), " |");
    }
}