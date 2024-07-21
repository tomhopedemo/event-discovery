package com.events.date;

import com.events.Util;

import java.util.List;
import java.util.regex.Matcher;

import static com.events.date.HyphenMatchers.M_HYPHENS_TO_UNTIL;
import static com.events.Util.list;
import static com.events.date.Time.intuitiveConversionA;

class BetweenTwoTimesAmPm extends TimeMatcher {
    List<Time> match(Util.StringMutable clean) {
        String text = clean.string;
        if (Util.empty(text)) return null;
        List<Time> to_return = list();
        String matched_string = "(time|between| at)?" + "[:]?(^|\\s*|\\()" + "([0-9]{1,2})([:|\\.][0-9][0|5])?" + "[ ]?(am|pm|noon)?" + "[\\)]?\\s*" + M_HYPHENS_TO_UNTIL + "\\s*[\\(]?" + "([0-9]{1,2})([:|\\.][0-9][0|5|9])?" + "(\\s*am|\\s*pm|\\s*noon)" + "(?![a-zA-Z])";
        Matcher matcher = Util.matcher(matched_string, text);
        while (matcher.find()) {
            int start = !Util.empty(matcher.group(1)) ? matcher.start() : matcher.start(3);
            int end = matcher.end();
            Time timeIndicative = new Time();
            if ("time".equals(matcher.group(1))) {
                timeIndicative.priority = true;
            }
            timeIndicative.setHour(String.valueOf(Integer.valueOf(matcher.group(3).trim())));
            if (Integer.valueOf(timeIndicative.getHour()) > 23) continue;
            timeIndicative.timeMinute = matcher.group(4) == null ? "00" : matcher.group(4).substring(1, 3);
            String from_am_pm = matcher.group(5);
            int to_hour = Integer.valueOf(matcher.group(7));
            if (to_hour > 24) continue;
            if (!Util.empty(from_am_pm)) {
                timeIndicative.amPm = from_am_pm;
            } else {
                timeIndicative.amPm = intuitiveConversionA(Integer.valueOf(timeIndicative.getHour()), to_hour, matcher.group(9).trim());
            }
            timeIndicative.convertTo24H();
            timeIndicative.startIndex = start;
            timeIndicative.endIndex = end;
            timeIndicative.provenance = getClass().getSimpleName();
            if (Integer.valueOf(matcher.group(7)) > 24) continue;
            to_return.add(timeIndicative);
        }
        return to_return;
    }
}           //am/pm required only at the end - e.g. 3-3.30pm.
