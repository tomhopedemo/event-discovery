package com.events.date.time;

import com.events.Util;
import com.events.date.Time;
import com.events.date.TimeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static com.events.date.HyphenMatchers.M_HYPHENS_TO_UNTIL;

public class BetweenTwoTimesAmPm_A extends TimeMatcher {
    public BetweenTwoTimesAmPm_A() {
    }

    protected List<Time> match(Util.StringMutable clean) {
        List<Time> to_return = new ArrayList<>();
        String text = clean.string;
        if (Util.empty(text)) return null;
        String matchedString = "(time|between)?" + "[:]?(^|\\s*|\\()" + "([0-9]{1,2})([:|\\.][0-9][0|5])?" + "[ ]?(am|pm|noon)" + "[\\)]?\\s*" + M_HYPHENS_TO_UNTIL + "\\s*[\\(]?" + "([0-9]{1,2})([:|\\.][0-9][0|5|9])?" + "(\\s*am|\\s*pm|\\s*noon)?";
        Matcher matcher = Util.matcher(matchedString, text);
        while (matcher.find()) {
            int start = !Util.empty(matcher.group(1)) ? matcher.start() : matcher.start(3);
            int end = matcher.end();
            Time timeIndicative = new Time();
            if ("time".equals(matcher.group(1))) {
                timeIndicative.priority = true;
            }
            timeIndicative.setHour(String.valueOf(Integer.valueOf(matcher.group(3).trim())));
            if (Integer.parseInt(timeIndicative.getHour()) > 23) continue;
            timeIndicative.timeMinute = matcher.group(4) == null ? "00" : matcher.group(4).substring(1, 3);
            timeIndicative.amPm = matcher.group(5);
            timeIndicative.convertTo24H();
            timeIndicative.startIndex = start;
            timeIndicative.endIndex = end;
            timeIndicative.provenance = this.getClass().getSimpleName();
            if (Integer.valueOf(matcher.group(7)) > 24) continue;
            to_return.add(timeIndicative);
        }
        return to_return;
    }
}         //am/pm required only at the start - e.g. 3.30pm till 4.