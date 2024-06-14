package com.arcta.events;
import static com.arcta.events.Util.Months.*;
import static com.arcta.events.M_Static.order;
class M_Month { static final String M_MONTH_ENG; static final String M_MONTH_DEU; static final String M_MONTH_NEG_LOOKAHEAD; static final String M_MONTHO_ENG; static final String M_MONTHS_SPACE_BEFORE_AND_AFTER; static final String M_MONTHS_SPACE_AFTER;
    static { M_MONTH_ENG = M_Static.constructMatcher(MONTHS_STANDARD_ENG); M_MONTH_DEU = M_Static.constructMatcher(MONTHS_STANDARD_DEU);
        M_MONTH_NEG_LOOKAHEAD = "(?!" + Util.string(order(MONTHS_STANDARD_ENG.keySet())) + ")";
        M_MONTHO_ENG = M_MONTH_ENG + "?";
        StringBuilder monthsSpaceBeforeAfter = new StringBuilder();
        for (String month : order(MONTHS_STANDARD_ENG.keySet())) {monthsSpaceBeforeAfter.append(" " + month + " |");}
        M_MONTHS_SPACE_BEFORE_AND_AFTER = Util.substringRemoveLast(monthsSpaceBeforeAfter);
        M_MONTHS_SPACE_AFTER = Util.string(order(MONTHS_STANDARD_ENG.keySet())," |");}}