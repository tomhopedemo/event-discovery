package com.events.date.time;

import com.events.Util;
import com.events.date.Time;
import com.events.date.TimeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class TimeText extends TimeMatcher {
    public List<Time> match(Util.StringMutable clean) {
        String text = clean.string;
        if (Util.empty(text)) return null;
        Matcher matcher = Util.matcher("time:\\s([^\\s]+)\\s(^\\s+)\\s", text);
        List<Time> to_return = new ArrayList<>();
        while (matcher.find()) {
            Time timeIndicative = new Time();
            timeIndicative.textTime = matcher.group(1) + " " + matcher.group(2);
            timeIndicative.startIndex = matcher.start();
            timeIndicative.endIndex = matcher.end();
            timeIndicative.provenance = getClass().getSimpleName();
            to_return.add(timeIndicative);
        }
        return to_return;
    }
}