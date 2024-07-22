package com.events.date;

import com.events.Util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.events.Util.*;
import static com.events.date.M_Static.MONTHS_ORDER;
import static com.events.date.M_Static.MONTH_DAY_MAP;
import static com.events.date.Weekdays.WEEKDAYS_ORDER;

public class Calendar {
    static final List<CalendarDate> YEAR_CALENDAR = list(); //single year
    static final List<CalendarDate> LEAP_YEAR_CALENDAR = list(); //single year
    static final List<CalendarDate> DAY_CALENDAR = list(); //multiple years
    static final CalendarDate CURRENT_CALENDAR_DATE;
    static String NEXT_YEAR_ABBREV = "25";
    /**
     * To be updated manually around new years day
     */
    static String CURRENT_YEAR_ABBREV = "24";
    static String PREVIOUS_YEAR_ABBREV = "23";
    static final String M_YEAR_CUR_OR_NEX = "(" + CURRENT_YEAR_ABBREV + "|" + NEXT_YEAR_ABBREV + ")";
    static final List<String> FIRST_THREE_MONTHS = list("jan", "feb", "mar");
    /**
     * These are maintained for the purposes of intuiting year by date matchers which don't identify year directly,
     */
    static final List<String> LAST_THREE_MONTHS = list("oct", "nov", "dec");

    static {
        for (String month : MONTHS_ORDER) {
            for (int i = 1; i <= MONTH_DAY_MAP.get(month); i++) {
                CalendarDate calendarDate = new CalendarDate();
                calendarDate.dateDay = String.valueOf(i);
                calendarDate.dateMonth = month;
                YEAR_CALENDAR.add(calendarDate);
            }
        }
        for (String month : MONTHS_ORDER) {
            Integer numdays = MONTH_DAY_MAP.get(month);
            if ("feb".equals(month)) numdays = 29;
            for (int i = 1; i <= numdays; i++) {
                CalendarDate calendarDate = new CalendarDate();
                calendarDate.dateDay = String.valueOf(i);
                calendarDate.dateMonth = month;
                LEAP_YEAR_CALENDAR.add(calendarDate);
            }
        }
        int dayIndex = 0;
        int startyear = 2024;         //max index to be increased yearly
        for (int i = 0; i < 2; i++) {
            int year = startyear + i;
            List<CalendarDate> yearCalendar = year % 4 == 0 ? LEAP_YEAR_CALENDAR : YEAR_CALENDAR;
            for (CalendarDate yearCalendarDate : yearCalendar) {
                CalendarDate calendarDate = yearCalendarDate.cloneMe();
                calendarDate.dateYear = String.valueOf(year);
                calendarDate.dayOfWeek = WEEKDAYS_ORDER.get(dayIndex++ % 7);
                DAY_CALENDAR.add(calendarDate);
            }
        }
        CalendarDate tmp = new CalendarDate();
        java.util.Date date = new java.util.Date();
        tmp.dateDay = String.valueOf(date.getDate());
        tmp.dateMonth = MONTHS_ORDER.get(date.getMonth());
        tmp.dateYear = String.valueOf(date.getYear() + 1900);
        CURRENT_CALENDAR_DATE = get(indexOf(tmp) - 1);
    }

    public static int indexOf(CalendarDate calendarDate) {
        return DAY_CALENDAR.indexOf(calendarDate);
    }

    public static int indexOf(Date date) {
        CalendarDate calendarDate = date.toCalendarDate();
        return indexOf(calendarDate);
    }

    public static List<CalendarDate> nextWeekdays(String weekday, int num) {
        List<CalendarDate> sublist = Util.sublist(DAY_CALENDAR, indexOfCurrent(), indexOfCurrent() + (num * 7));
        sublist.removeIf(e -> !weekday.equals(e.dayOfWeek));
        return sublist;
    }

    public static List<CalendarDate> nextWeekdays(int weeks) {
        return Util.sublist(DAY_CALENDAR, indexOfCurrent(), indexOfCurrent() + (weeks * 7));
    }

    static String defaultYearAbbrev(String month) {
        if (LAST_THREE_MONTHS.contains(CURRENT_CALENDAR_DATE.dateMonth)) {
            if (FIRST_THREE_MONTHS.contains(month)) {
                return NEXT_YEAR_ABBREV;
            }
        }
        return CURRENT_YEAR_ABBREV;
    }

    static String defaultYearAbbrev() {
        return CURRENT_YEAR_ABBREV;
    }

    static String defaultYearFull() {
        return "20" + defaultYearAbbrev();
    }

    static String defaultYearFull(String month) {
        return "20" + defaultYearAbbrev(month);
    }

    public static void setDayOfWeek(Collection<Date> dates) {
        if (dates == null) return;
        for (Date date : dates) {
            setDayOfWeek(date);
        }
    }

    public static void setDayOfWeek(Date date) {
        CalendarDate element = Util.getElement(DAY_CALENDAR, date.toCalendarDate());
        if (element == null) return;
        date.dayOfWeek = element.dayOfWeek;
    }

    public static String getDayOfWeek(Date date) {
        CalendarDate element = Util.getElement(DAY_CALENDAR, date.toCalendarDate());
        if (element == null) return null;
        return element.dayOfWeek;
    }

    public static Date currentDate() {
        return currentCalendarDate().toDate();
    }

    public static CalendarDate currentCalendarDate() {
        return CURRENT_CALENDAR_DATE;
    }

    public static CalendarDate get(int index) {
        return DAY_CALENDAR.get(index);
    }

    public static int indexOfCurrent() {
        return indexOf(CURRENT_CALENDAR_DATE);
    }

    public static class CalendarDate {
        public String dateDay;
        public String dateMonth;
        public String dateYear; //e.g. 1,2,...30//e.g. Oct, ...//e.g. 2017
        public String dayOfWeek;     //additional - does not form part of equals method

        public CalendarDate(String dateDay, String dateMonth, String dateYear) {
            this.dateDay = dateDay;
            this.dateMonth = dateMonth;
            this.dateYear = dateYear;
        }

        public CalendarDate() {
        }

        CalendarDate cloneMe() {
            CalendarDate calendarDate = new CalendarDate(this.dateDay, this.dateMonth, this.dateYear);
            calendarDate.dayOfWeek = this.dayOfWeek;
            return calendarDate;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CalendarDate that = (CalendarDate) o;
            if (!Objects.equals(dateDay, that.dateDay)) return false;
            if (!Objects.equals(dateMonth, that.dateMonth)) return false;
            return Objects.equals(dateYear, that.dateYear);
        }

        public int hashCode() {
            int result = dateDay != null ? dateDay.hashCode() : 0;
            result = 31 * result + (dateMonth != null ? dateMonth.hashCode() : 0);
            result = 31 * result + (dateYear != null ? dateYear.hashCode() : 0);
            return result;
        }

        Date toDate(boolean withWeekday) {
            Date date = new Date(this.dateDay, this.dateMonth, this.dateYear);
            if (withWeekday) {
                date.dayOfWeek = this.dayOfWeek;
            }
            return date;
        }

        public Date toDate() {
            return toDate(false);
        }

        public static int diffFromCurrent(Date date) {
            return diff(date, currentCalendarDate());
        }

        static int diff(Date date1, CalendarDate date2) {
            return Util.diff(Calendar.indexOf(date1.toCalendarDate()), Calendar.indexOf(date2));
        }
    }

    public static class Date implements Serializable, Comparable<Date> {
        static String serialVersionUID = "2";
        public String dateDay;
        public String dateMonth;
        public String dateYear; //e.g. 1,2,...30                //field 0//e.g. Oct, ...//e.g. 2017                    //field 2
        public String dayOfWeek; //e.g. Mon, Tue, ...         //field 3     // ----- NOT PART OF EQUALS METHOD ------------
        public Util.MultiList<Integer, Integer> indexPairs = new Util.MultiList<>(); //matcher start index     // --------------------- ADDITIONAL FIELDS ----------
        public String note;  //provenance

        public Date(String dateDay, String dateMonth, String dateYear) {
            this.dateDay = dateDay;
            this.dateMonth = dateMonth;
            this.dateYear = dateYear;
        }

        public Date() {
        }

        public static Integer daysBetween(Date dateOne, Date dateTwo) {
            return daysBetween(list(dateOne, dateTwo));
        }

        public static Date paddedDate(Date existingDate) {
            Date date = new Date(Util.padleft(existingDate.dateDay, '0', 2), Util.padleft(existingDate.dateMonth, '0', 2), existingDate.dateYear);
            date.dayOfWeek = existingDate.dayOfWeek;
            return date;
        }

        public static Date previous(Date date) {
            int i = indexOf(date.toCalendarDate());
            if (i < 1) return null;
            return get(i - 1).toDate();
        }

        public static Integer daysBetween(Collection<Date> days) {
            if (empty(days)) return null;
            List<Integer> indices = list();
            for (Date date : days) {
                int i = Calendar.indexOf(date);
                if (i == -1) continue;
                indices.add(i);
            }
            if (empty(indices)) return null;
            Collections.sort(indices);
            return diff(indices.get(0), indices.get(indices.size() - 1));
        }

        public Date cloneMe() {
            Date date = new Date(this.dateDay, this.dateMonth, this.dateYear);
            date.dayOfWeek = this.dayOfWeek;
            return date;
        }

        public static List<Date> cloneMe(List<Date> dates) {
            if (dates == null) return null;
            List<Date> list = list();
            for (Date date : dates) {
                list.add(date.cloneMe());
            }
            return list;
        }

        public String prettyDay() {
            String date_day_ = Util.safeNull(dateDay);
            return (date_day_.length() == 1 ? " " + date_day_ : date_day_) + " " + dateMonth;
        }

        CalendarDate toCalendarDate() {
            return new CalendarDate(this.dateDay, this.dateMonth, this.dateYear);
        }

        public int compareTo(Date o) {     //-1 means the object is after this.
            CalendarDate objectCalendarDate = o.toCalendarDate();
            CalendarDate calendarDateThis = this.toCalendarDate();
            if (indexOf(objectCalendarDate) == indexOf(calendarDateThis)) {
                return 0;
            }
            return indexOf(objectCalendarDate) >= indexOf(calendarDateThis) ? -1 : 1;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Date date = (Date) o;
            if (!Objects.equals(dateDay, date.dateDay)) return false;
            if (!Objects.equals(dateMonth, date.dateMonth)) return false;
            return Objects.equals(dateYear, date.dateYear);
        }

        public int hashCode() {
            int result = dateDay != null ? dateDay.hashCode() : 0;
            result = 31 * result + (dateMonth != null ? dateMonth.hashCode() : 0);
            result = 31 * result + (dateYear != null ? dateYear.hashCode() : 0);
            return result;
        }

        public static boolean strictlyBeforeAfterCheck(Date strictlyBefore, Date strictlyAfter) {
            return strictlyBefore.compareTo(strictlyAfter) == -1;
        }        //means after is properly after before

        public static boolean beforeAfterCheck(Date before, Date after) {
            return before.compareTo(after) <= 0;
        }

        public static Date create(String dateRaw) {
            if (dateRaw == null) return null;
            Util.NList split = Util.split(dateRaw, "\t");
            Date date = new Date(split.get(0), split.get(1), split.get(2));
            date.dayOfWeek = split.get(3);
            date.note = split.get(4);
            return date;
        }

        public static String saveFormat(Date date) {
            if (date == null) return null;
            return string(list(date.dateDay, date.dateMonth, date.dateYear, date.dayOfWeek, date.note), "\t");
        }

        public static Date merge(Date a, Date b) {
            Date date = new Date();
            if (a == null || b == null) return null;
            if (b.dateYear != null) {
                if (a.dateYear != null) {
                    if (!a.dateYear.equals(b.dateYear)) return null;
                } else {
                    date.dateYear = b.dateYear;
                }
            } else {
                date.dateYear = a.dateYear;
            }
            if (b.dateMonth != null) {
                if (a.dateMonth != null) {
                    if (!a.dateMonth.equals(b.dateMonth)) return null;
                } else {
                    date.dateMonth = b.dateMonth;
                }
            } else {
                date.dateMonth = a.dateMonth;
            }
            if (b.dateDay != null) {
                if (a.dateDay != null) {
                    if (!a.dateDay.equals(b.dateDay)) return null;
                } else {
                    date.dateDay = b.dateDay;
                }
            } else {
                date.dateDay = a.dateDay;
            }
            Calendar.setDayOfWeek(date);
            date.indexPairs = b.indexPairs;
            int index = Calendar.indexOf(date);         // ------------ CONFIRM DATE EXISTS IN CALENDAR ------------
            if (index == -1) return null;
            date.note = "PARTIAL";
            return date;
        }

        static String monthReadable(String month) {
            int monthReadable = MONTHS_ORDER.indexOf(month) + 1;
            return Util.padleft(String.valueOf(monthReadable), '0', 2);
        }

        public String fileDate() {
            monthReadable(dateMonth);
            return dateYear + "-" + monthReadable(dateMonth) + "-" + Util.padleft(String.valueOf(dateDay), '0', 2);
        }
    }
}