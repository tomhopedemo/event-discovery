package com.arcta.events;

import java.util.List;

class IntermediateData {
    static List<String> getUnmergedEvents(String ref, Dirs dirs) {
        String events = Util.readString(dirs.getUnmergedDir() + ref + ".txt");
        if (Util.empty(events)) return null;
        return Util.splitList(events, Shared.DELIMITER_RECORD);
    }

    static List<Event> getMergedEventList(String ref, Dirs dirs) {
        String events = Util.readString(dirs.getMergedDir() + ref + ".txt");
        if (Util.empty(events)) return null;
        List<String> mergedEvents = Util.splitList(events, Shared.DELIMITER_RECORD);
        return Event.create(mergedEvents);
    }
}
