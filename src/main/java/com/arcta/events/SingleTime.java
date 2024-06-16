package com.arcta.events;

import java.util.List;
import java.util.regex.Matcher;

class SingleTime extends TimeMatcher {
    boolean pm;
    boolean doors;

    SingleTime(boolean pm, boolean doors) {
        this.pm = pm;
        this.doors = doors;
    }

    List<Time> match(Util.StringMutable clean) {
        return match(clean, false);
    }

    List<Time> match(Util.StringMutable clean, boolean exact_match) {
        String timex = "([0-9]{1,2})[:|\\.]([0-9])(0|5)";
        String regex = "(?<!£|£ |£\\d+|£\u00A0|0|1|2)(time|onstage|time |at|doors|start| kl\\. )?" + "[:]?" + "(^|\\s*|\\()" + timex + "(?![0-9])";
        String text = clean.string;
        Matcher matcher = Util.matcher(regex, text);
        List<Time> to_return = Util.list();
        while (matcher.find()) {
            int start = Util.list("time", "at", "doors").contains(matcher.group(1)) ? matcher.start() : matcher.start(3);
            int end = matcher.end();
            if (exact_match) {
                if (start != 0 || end != text.length()) continue;
            }
            Time timeIndicative = new Time();
            List<String> preferredPrefixes = Util.list("time", "time ", "onstage", "start");
            if (preferredPrefixes.contains(matcher.group(1))) timeIndicative.priority = true;
            Integer hourInt = Integer.valueOf(matcher.group(3).trim());
            if (doors && "doors".equals(matcher.group(1))) {
                hourInt = hourInt + 1;
            }
            timeIndicative.setHour(String.valueOf(hourInt));
            if (Integer.parseInt(timeIndicative.getHour()) > 23) continue;
            timeIndicative.timeMinute = matcher.group(4).trim() + matcher.group(5);
            if (pm) timeIndicative.amPm = "pm";
            if (Util.list("time", "time ").contains(matcher.group(1))) {
                timeIndicative.convertTo24H(8);
            } else {
                timeIndicative.convertTo24H();
            }
            if (Util.between(Integer.valueOf(timeIndicative.getHour()), 3, 9)) continue;
            timeIndicative.startIndex = start;
            timeIndicative.endIndex = end;
            timeIndicative.provenance = getClass().getSimpleName();
            to_return.add(timeIndicative);
        }
        return to_return;
    }
}