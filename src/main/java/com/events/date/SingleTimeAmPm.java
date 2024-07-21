package com.events.date;

import com.events.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

class SingleTimeAmPm extends TimeMatcher {
    List<Time> match(Util.StringMutable clean) {
        return match(clean, false);
    }

    List<Time> match(Util.StringMutable clean, boolean exact_match) {
        String regex = "(?<!£|£ |£\\d+|£\u00A0|0|1|2)" + "(time|onstage|at|doors|show start)?" + "[:]?" + "(^|\\s*|\u00A0|\\()" + M_TIME_AM + "(?![a-zA-Z])";
        String text = clean.string;
        Matcher matcher = Util.matcher(regex, text);
        List<Time> to_return = new ArrayList<>();
        while (matcher.find()) {
            int start = Util.list("time", "onstage", "at", "doors", "show start").contains(matcher.group(1)) ? matcher.start() : matcher.start(3);
            int end = matcher.end();
            if (exact_match) {
                if (start != 0 || end != text.length()) continue;
            }
            Time timeIndicative = new Time();
            List<String> preferredPrefixes = Util.list("time", "onstage", "show start");
            if (preferredPrefixes.contains(matcher.group(1))) timeIndicative.priority = true;
            timeIndicative.setHour(String.valueOf(Integer.valueOf(matcher.group(3).trim())));
            if (Integer.parseInt(timeIndicative.getHour()) > 23) continue;
            timeIndicative.timeMinute = matcher.group(4).trim();
            timeIndicative.amPm = matcher.group(5);
            timeIndicative.convertTo24H();
            if (Util.between(Integer.parseInt(timeIndicative.getHour()), 3, 9)) continue;
            timeIndicative.startIndex = start;
            timeIndicative.endIndex = end;
            timeIndicative.provenance = getClass().getSimpleName();
            to_return.add(timeIndicative);
        }
        return to_return;
    }
}