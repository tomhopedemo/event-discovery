package com.arcta.events;
import java.util.List;
import java.util.regex.Matcher;
class TimeText extends TimeMatcher {
    List<Time> match(Util.StringMutable clean) {String text = clean.string;
        if (Util.empty(text)) return null;
        Matcher matcher = Util.matcher("time:\\s([^\\s]+)\\s(^\\s+)\\s", text);
        List<Time> to_return = Util.list();
        while (matcher.find()) { Time timeIndicative = new Time();
            timeIndicative.textTime = matcher.group(1) + " " + matcher.group(2);
            timeIndicative.startIndex = matcher.start();
            timeIndicative.endIndex = matcher.end();
            timeIndicative.provenance = getClass().getSimpleName();
            to_return.add(timeIndicative);} return to_return;}}