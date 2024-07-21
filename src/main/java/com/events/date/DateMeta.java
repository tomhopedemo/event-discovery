package com.events.date;

import com.events.Util;

import java.util.List;

import static com.events.Util.list;

class DateMeta {
    List<Calendar.Date> between;     // ----- DOUBLE DATE RETURN STRUCTURES ---------
    List<List<Calendar.Date>> betweenList = list(); //utilised in Program F ( DATESPLIT )
    List<Calendar.Date> dateList = list(); //utilised in Program F ( DATESPLIT ) -    // ----- SINGLE DATE RETURN STRUCTURES ---------
    List<Util.Multi<Integer, Integer>> indexPairsInclExcl = list();

    static boolean empty(DateMeta date_internal) {
        if (date_internal == null) return true;
        return (Util.empty(date_internal.dateList) && Util.empty(date_internal.between) && Util.empty(date_internal.betweenList));
    }

    static List<Calendar.Date> dateList(DateMeta date_internal) {
        if (date_internal == null) return null;
        return date_internal.dateList;
    }
}