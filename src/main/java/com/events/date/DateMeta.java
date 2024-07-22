package com.events.date;

import com.events.Util;

import java.util.List;

import static com.events.Util.list;

public class DateMeta {
    public List<Calendar.Date> between;     // ----- DOUBLE DATE RETURN STRUCTURES ---------
    public List<List<Calendar.Date>> betweenList = list(); //utilised in Program F ( DATESPLIT )
    public List<Calendar.Date> dateList = list(); //utilised in Program F ( DATESPLIT ) -    // ----- SINGLE DATE RETURN STRUCTURES ---------
    public List<Util.Multi<Integer, Integer>> indexPairsInclExcl = list();

    public static boolean empty(DateMeta date_internal) {
        if (date_internal == null) return true;
        return (Util.empty(date_internal.dateList) && Util.empty(date_internal.between) && Util.empty(date_internal.betweenList));
    }

    public static List<Calendar.Date> dateList(DateMeta date_internal) {
        if (date_internal == null) return null;
        return date_internal.dateList;
    }
}