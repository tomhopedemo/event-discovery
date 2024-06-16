package com.arcta.events;

import java.util.List;
import java.util.regex.Matcher;

import static com.arcta.events.Util.list;

class HourAmPm extends TimeMatcher {
    List<Time> match(Util.StringMutable clean) {
        return match(clean, false);
    }

    List<Time> match(Util.StringMutable clean, boolean exact_match) {
        String matched_string = "(?<!0|1|2|3|4|5|:)(doors|onstage|time| at)?[:]?(^|\\s*|\u00A0)([0-9]{1,2})[ ]?(pm|am)(?![a-zA-Z])";
        String text = clean.string;
        Matcher matcher = Util.matcher(matched_string, text);
        List<Time> to_return = list();
        while (matcher.find()) {
            int start = !Util.empty(matcher.group(1)) ? matcher.start() : matcher.start(3);
            int end = matcher.end();
            if (exact_match) {
                if (start != 0 || end != text.length() - 1) continue;
            }
            Time timeIndicative = new Time();
            String hour = matcher.group(3);
            if (Integer.parseInt(hour) > 12 || Integer.parseInt(hour) < 1) continue;
            timeIndicative.setHour(String.valueOf(Integer.valueOf(hour)));
            if (Integer.parseInt(timeIndicative.getHour()) > 23) continue;
            timeIndicative.timeMinute = "00";
            timeIndicative.amPm = matcher.group(4);
            timeIndicative.startIndex = start;
            timeIndicative.endIndex = end;
            timeIndicative.provenance = getClass().getSimpleName();
            timeIndicative.convertTo24H();
            to_return.add(timeIndicative);
        }
        return to_return;
    }
}