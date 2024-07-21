package com.events.date;

import com.events.Util;

import java.util.List;

class OpenDaily extends DateMatcher {
    public DateMeta matchInternal(String text) {
        String regex = "open daily";
        if (!text.contains(regex)) return null;
        Calendar.CalendarDate temporaryToDate = Calendar.get(Calendar.indexOfCurrent() + 90);
        DateMeta meta = new DateMeta();
        List<Calendar.Date> dates = constructIntermediateDates(Calendar.currentDate(), temporaryToDate.toDate());
        for (Calendar.Date dateIterate : dates) {
            Util.MultiList<Integer, Integer> indexPairsToRemove = new Util.MultiList<>();
            indexPairsToRemove.add(new Util.Multi(text.indexOf(regex), text.indexOf(regex + 10)));
            dateIterate.indexPairs = indexPairsToRemove;
        }
        dates.forEach(d -> d.note = getClass().getSimpleName());
        meta.betweenList.add(dates);
        meta.between = Util.get(meta.betweenList, 0);
        return meta;
    }
}