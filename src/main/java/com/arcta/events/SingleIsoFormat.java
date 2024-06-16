package com.arcta.events;

import java.time.Instant;

import static com.arcta.events.M_Static.MONTHS_ORDER;

class SingleIsoFormat extends DateMatcher {
    public DateMeta matchInternal(String text) {
        Instant instant = null;
        try {
            instant = Instant.parse(text);
        } catch (Exception e) {
        }
        if (instant == null) return null;
        java.util.Date java_date = java.util.Date.from(instant);
        DateMeta date_internal = new DateMeta();
        Calendar.Date date = new Calendar.Date(String.valueOf(java_date.getDate()), MONTHS_ORDER.get(java_date.getMonth()), String.valueOf(java_date.getYear() + 1900));
        date.note = getClass().getSimpleName();
        date_internal.dateList.add(date);
        return date_internal;
    }
}