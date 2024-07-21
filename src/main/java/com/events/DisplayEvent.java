package com.events;

import com.events.date.Calendar;

import java.io.Serializable;
import java.util.List;

import static com.events.Shared.DELIMITER_INLINE;
import static com.events.Util.list;
import static com.events.Util.string;

public class DisplayEvent implements Serializable {
    String time;         //merge field      //----- REQUIRED FIELDS ---
    String title;        //merge field
    String link;         //merge field
    String ref;          //merge field x
    String displayOrganizer;
    String district;
    List<Calendar.Date> dates;
    String category;
    String source;
    String id;
    String soldout;
    List<Util.Lang> languages;
    String fullTitle;
    //not persisted
    String completeClassification;
    String symbol;
    Double distance;
    int colorIndex;

    static String saveFormatShort(DisplayEvent event) {
        List<String> list = list();
        list.add(event.time);
        list.add(event.title);
        list.add(event.link);
        list.add(event.ref);
        list.add(event.displayOrganizer);
        list.add(event.district);
        list.add(dateListFormat(event.dates));
        list.add(event.category);
        list.add(event.source);
        list.add(event.id);
        list.add(event.soldout);
        list.add(langListFormat(event.languages));
        list.add(event.fullTitle);
        return string(list, DELIMITER_INLINE);
    }

    static DisplayEvent create(String event_raw) {
        if (Util.empty(event_raw)) return null;
        DisplayEvent event = new DisplayEvent();
        Util.NList split = Util.split(event_raw, DELIMITER_INLINE);
        event.time = split.get(0);
        event.title = split.get(1);
        event.link = split.get(2);
        event.ref = split.get(3);
        event.displayOrganizer = split.get(4);
        event.district = split.get(5);
        event.dates = reconstructDates(split.get(6));
        event.category = split.get(7);
        event.source = split.get(8);
        event.id = split.get(9);
        event.soldout = split.get(10);
        event.languages = reconstructLanguages(split.get(12));
        event.fullTitle = split.get(13);
        return event;
    }

    static List<Util.Lang> reconstructLanguages(String input) {
        if (Util.empty(input)) return list();
        List<String> underlying = Util.split(input, "#LAN#").underlying;
        List<Util.Lang> toReturn = list();
        for (String s : underlying) {
            try {
                Util.Lang language = Util.Lang.valueOf(s);
                toReturn.add(language);
            } catch (Exception ignored) {
            }
        }
        return toReturn;
    }

    static List<Calendar.Date> reconstructDates(String input) {
        List<Calendar.Date> dates = list();
        if (Util.empty(input)) return dates;
        List<String> split = Util.split(input, "#DAT#").underlying;
        for (String s : split) {
            Calendar.Date createdDate = Calendar.Date.create(s);
            dates.add(createdDate);
        }
        return dates;
    }

    static String dateListFormat(List<Calendar.Date> dates) {
        List<String> dateStrings = list();
        if (Util.empty(dates)) return "";
        for (Calendar.Date date : dates) {
            dateStrings.add(Calendar.Date.saveFormat(date));
        }
        return string(dateStrings, "#DAT#");
    }

    static String langListFormat(List<Util.Lang> languages) {
        if (Util.empty(languages)) return "";
        return string(languages, "#LAN#");
    }
}