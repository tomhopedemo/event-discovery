package com.arcta.events;
import java.util.List;
import java.util.regex.Matcher;
import static com.arcta.events.Time.intuitiveConversionA;
import static com.arcta.events.HyphenMatchers.M_HYPHENS_TO_UNTIL;
import static com.arcta.events.Util.list;
class BetweenTwoTimes extends TimeMatcher { boolean pm_override;
    BetweenTwoTimes(boolean pm_override) {
        this.pm_override = pm_override;
    }
    List<Time> match(Util.StringMutable clean) {         List<Time> to_return = list();
        String text = clean.string;
        if (Util.empty(text)) return null;
        String matched_string = "(time)?[:]?(^|\\s*|\\()([0-9]{1,2})[:|\\.]([0-9])(0|5)[ ]?(am|pm|noon)?[\\)]?\\s*" + M_HYPHENS_TO_UNTIL + "\\s*[\\(]?([0-9]{1,2})[:|\\.]([0-9])(0|5|9)[ ]?(\\s*am|\\s*pm|\\s*noon)?(?!am|pm|noon)";
        Matcher matcher = Util.matcher(matched_string, text);
        while (matcher.find()) { int start = "time".equals(matcher.group(1)) ? matcher.start() : matcher.start(3);
            int end = matcher.end();
            Time timeIndicative = new Time();
            timeIndicative.setHour(String.valueOf(Integer.valueOf(matcher.group(3).trim())));
            if (Integer.valueOf(timeIndicative.getHour()) > 23) continue;
            timeIndicative.timeMinute = matcher.group(4).trim() + matcher.group(5).trim();
            String from_am_pm = matcher.group(6);
            int to_hour = Integer.valueOf(matcher.group(8));
            if (to_hour > 24) continue;
            if (pm_override) {
                if (Util.empty(timeIndicative.amPm)) {timeIndicative.amPm = "pm";}
            } else {
                if (!Util.empty(from_am_pm)) { timeIndicative.amPm = from_am_pm;
                } else { timeIndicative.amPm = intuitiveConversionA(Integer.valueOf(timeIndicative.getHour()), to_hour, Util.trim(matcher.group(11)));}}
            timeIndicative.convertTo24H();
            if (matcher.group(10).equals("9") && !(matcher.group(9).equals("5") && matcher.group(8).equals("23"))) {continue;}
            if (matcher.group(10).equals("9") && timeIndicative.getHour().equals("00") && timeIndicative.timeMinute.equals("00")) { //00:00 - 23:59 case
                timeIndicative = new Time();
                timeIndicative.setHour("12");
                timeIndicative.timeMinute = "00";}
            timeIndicative.startIndex = start;
            timeIndicative.endIndex = end;
            timeIndicative.provenance = getClass().getSimpleName();
            if (Integer.valueOf(matcher.group(8)) > 24) continue;
            to_return.add(timeIndicative);} return to_return;}}                   //59 allowed - its when pms not required
