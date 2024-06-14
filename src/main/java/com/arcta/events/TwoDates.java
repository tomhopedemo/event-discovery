package com.arcta.events;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import static com.arcta.events.Util.Months.MONTHS_STANDARD_ENG;
import static com.arcta.events.M_Static.*;
import static com.arcta.events.M_Weekday.M_WEEKDAYO_ENG;
import static com.arcta.events.Util.set;
class TwoDates extends DateMatcher {
    public DateMeta matchInternal(String text) {         Set<List<Calendar.Date>> betweenSet = set();
        String regex = NEGATIVE_LOOKBEHIND_DIGITS + M_WEEKDAYO_ENG + MWO + M_DAY_ORDINALO + SPACES + "&" + SPACES + M_WEEKDAYO_ENG + MWO + M_DAY_MONTH_YEARO;
        DateMeta meta = new DateMeta();
        Matcher matcher = Util.matcher(regex, text);
        while (matcher.find()){ Calendar.Date dateOne = new Calendar.Date();
            Calendar.Date dateTwo = new Calendar.Date();
            dateTwo.dateDay = String.valueOf(Integer.valueOf(matcher.group(5)));
            dateTwo.dateMonth = MONTHS_STANDARD_ENG.get(matcher.group(7));
            dateTwo.dateYear = "20" + (matcher.group(9) == null ? Calendar.defaultYearAbbrev(dateTwo.dateMonth) : matcher.group(9));
            dateOne.dateDay = String.valueOf(Integer.valueOf(matcher.group(2)));
            dateOne.dateMonth = dateTwo.dateMonth;
            dateOne.dateYear = dateTwo.dateYear;
            Util.MultiList<Integer, Integer> indexPairs = new Util.MultiList<>();
            indexPairs.add(new Util.Multi<>(matcher.start(), matcher.end()));
            dateOne.indexPairs = indexPairs; dateTwo.indexPairs = indexPairs;
            dateOne.note = getClass().getSimpleName(); dateTwo.note = getClass().getSimpleName();
            List<Calendar.Date> dates = Util.list(dateOne, dateTwo);
            betweenSet.add(dates);}
        meta.betweenList = new ArrayList<>(betweenSet);
        meta.between = Util.get(meta.betweenList,0); return meta;}}                           // 3 & 4 aug 2020