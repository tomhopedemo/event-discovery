package com.events;

import com.events.Util.NList;
import com.events.date.Calendar;
import com.events.date.Time;

import java.io.Serializable;
import java.util.*;

import static com.events.Shared.DELIMITER_INLINE;
import static com.events.Util.list;

class Event implements Serializable {
    Event() {
    }

    Event(String organizer) {
        this.organizer = organizer;
    }

    String organizer;    //field 0
    String name;         //field 1
    String originalText;    //field 2
    String link;   //field 3
    String source; //field 4
    Map<Calendar.Date, String> dateUrl = new HashMap<>(); //field 5
    Map<Calendar.Date, Time> dateTimes = new HashMap<>(); //field 6
    String id; //field 7
    String district; //field 11
    boolean curtailed = false; //--------- not persisted
    boolean soldout = false;
    Context ctx;
    String category;

    static List<String> saveFormat(Collection<Event> events) {
        if (events == null) return null;
        List<String> rawEvents = list();
        for (Event event : events) {
            rawEvents.add(saveFormat(event));
        }
        return rawEvents;
    }

    static List<Event> create(List<String> rawEvents) {
        List<Event> events = list();
        if (Util.empty(rawEvents)) return events;
        for (String rawEvent : rawEvents) {
            Event event = create(rawEvent);
            if (event == null) continue;
            events.add(event);
        }
        return events;
    }

    static String saveFormat(Event event) {
        List<String> list = list();
        list.add(event.organizer);                      //field 0
        list.add(event.name);                           //field 1
        list.add(event.originalText);                  //field 2
        list.add(event.link);                 //field 3
        list.add(event.source);                         //field 4
        list.add(dateUrlFormat(event.dateUrl));      //field 5
        list.add(dateTimeFormat(event.dateTimes));   //field 6
        list.add(event.id);   //field 7
        list.add(event.district);  //field 11
        return Util.string(list, DELIMITER_INLINE);
    }

    static String dateUrlFormat(Map<Calendar.Date, String> dateUrl) {
        List<String> dateUrls = list();
        if (Util.empty(dateUrl)) return "";
        for (Calendar.Date date : dateUrl.keySet()) {
            dateUrls.add(Calendar.Date.saveFormat(date) + "#DUF#" + dateUrl.get(date));
        }
        return Util.string(dateUrls, "#DUFDUF#");
    }

    static String dateTimeFormat(Map<Calendar.Date, Time> dateTime) {
        List<String> dateTimes = list();
        if (Util.empty(dateTime)) return "";
        for (Calendar.Date date : dateTime.keySet()) {
            dateTimes.add(Calendar.Date.saveFormat(date) + "#DAT#" + dateTime.get(date).write());
        }
        return Util.string(dateTimes, "#DATDAT#");
    }

    static Map<Calendar.Date, Time> reconstructDateTimeMap(String input) {
        Map<Calendar.Date, Time> date_time = new HashMap<>();
        if (Util.empty(input)) return date_time;
        List<String> split = Util.split(input, "#DATDAT#").underlying;
        for (String raw : split) {
            List<String> subsplit = Util.split(raw, "#DAT#").underlying;
            String date = subsplit.get(0);
            String time = subsplit.get(1);
            Calendar.Date createdDate = Calendar.Date.create(date);
            Time createdTime = Time.create(time);
            date_time.put(createdDate, createdTime);
        }
        return date_time;
    }

    static Map<Calendar.Date, String> reconstructDateUrlMap(String input) {
        Map<Calendar.Date, String> dateUrl = new HashMap<>();
        if (Util.empty(input)) return dateUrl;
        List<String> split = Util.split(input, "#DUFDUF#").underlying;
        for (String raw : split) {
            List<String> subsplit = Util.split(raw, "#DUF#").underlying;
            String date = subsplit.get(0);
            String url = subsplit.get(1);
            Calendar.Date createdDate = Calendar.Date.create(date);
            dateUrl.put(createdDate, url);
        }
        return dateUrl;
    }

    static Event create(String rawEvent) {
        if (Util.empty(rawEvent)) return null;
        NList split = Util.split(rawEvent, DELIMITER_INLINE);
        Event event = new Event(split.get(0));
        event.name = split.get(1);
        event.originalText = split.get(2);
        event.link = split.get(3);
        event.source = split.get(4);
        event.dateUrl = reconstructDateUrlMap(split.get(5));
        event.dateTimes = reconstructDateTimeMap(split.get(6));
        event.id = split.get(7);
        event.district = split.get(8);
        return event;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        if (!Objects.equals(organizer, event.organizer)) return false;
        return Objects.equals(name, event.name);
    }

    public int hashCode() {
        int result = organizer != null ? organizer.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}