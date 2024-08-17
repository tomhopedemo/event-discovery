package com.events.date;

import com.events.Util;

import java.io.Serializable;
import java.util.Objects;

public class Time implements Serializable {
    static String serialVersionUID = "4";
    private String timeHour; //e.g. 1,2,3,...23
    public String timeMinute; //e.g. 1,2,...59
    public String amPm;     // ---------------- SHORT LIVED & DEBUG FIELDS (  ALSO NOT PART OF EQUALS ) ----------
    public String textTime; //textual version of time ( that yet to write parser for )
    public Integer startIndex; //matcher start index     //notes
    public Integer endIndex; //matcher end index
    public Integer distance; //distance
    public String provenance; //methodology
    public boolean priority = false; //high_priority

    public String getHour() {
        return timeHour;
    }

    public void setHour(String timeHour) {
        if (timeHour.length() > 5) {
            System.out.println("hi: " + timeHour);
        }
        this.timeHour = timeHour;
    }

    public Time(String timeHour, String timeMinute, String amPm) {
        setHour(timeHour);
        this.timeMinute = timeMinute;
        this.amPm = amPm;
    }

    public Time(String timeHour, String timeMinute) {
        this(timeHour, timeMinute, null);
    }

    public Time() {
    }

    public static Time getStartDayTime() {
        Time startdaytime = new Time("12", "00");
        startdaytime.provenance = "StartDay";
        return startdaytime;
    }

    public Time cloneMe() {
        Time time = new Time(this.timeHour, this.timeMinute, this.amPm);
        time.provenance = this.provenance;
        return time;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Time time = (Time) o;
        if (!Objects.equals(timeHour, time.timeHour)) return false;
        return Objects.equals(timeMinute, time.timeMinute);
    }

    public int hashCode() {
        int result = timeHour != null ? timeHour.hashCode() : 0;
        result = 31 * result + (timeMinute != null ? timeMinute.hashCode() : 0);
        return result;
    }

    static boolean beforeAfterCheck(Time before, Time after) {
        if (before == null) return false;
        return before.compareTo(after) <= 0;
    }

    public int compareTo(Time time) {
        if (time == null) return -1;
        if (Integer.parseInt(this.timeHour) < Integer.parseInt(time.timeHour)) return -1;
        if (Integer.parseInt(this.timeHour) > Integer.parseInt(time.timeHour)) return 1;
        if (Integer.parseInt(this.timeMinute) < Integer.parseInt(time.timeMinute)) return -1;
        if (Integer.parseInt(this.timeMinute) > Integer.parseInt(time.timeMinute)) return 1;
        return 0;
    }

    void intuitiveConversion(Integer implied_pm) {
        int implied_pm_limit = implied_pm != null ? implied_pm : 6;
        if (Util.between(Integer.parseInt(timeHour), 1, implied_pm_limit)) {
            setHour(String.valueOf(Integer.parseInt(timeHour) + 12));
        }
    }

    public static String intuitiveConversionA(int from_hour, int to_hour, String to_hour_am_pm) {
        if (from_hour <= to_hour) {
            return to_hour_am_pm;
        } else if (from_hour == 12 && "pm".equals(to_hour_am_pm)) {
            return to_hour_am_pm;
        } else {
            return ("am".equals(to_hour_am_pm)) ? "pm" : "am";
        }
    }

    public void convertTo24H() {
        convertTo24H(null);
    }

    public void convertTo24H(Integer implied_pm) {
        if (amPm == null) {
            intuitiveConversion(implied_pm);
            return;
        }
        if (amPm.equals("am")) {
            if ("12".equals(timeHour)) {
                setHour("0");
            }
        } else if (amPm.equals("pm")) {
            Integer time_hour_int = Integer.valueOf(timeHour);
            if (time_hour_int < 12) {
                setHour(String.valueOf(time_hour_int + 12));
            }
        } else if (amPm.equals("noon")) {
        }
        amPm = null;
    }

    public String pretty() {
        if (textTime != null) return textTime;
        if (timeHour == null) return "";
        String minute = timeMinute == null ? "00" : timeMinute;
        String hour = timeHour.length() == 1 ? "0" + timeHour : timeHour;
        return hour + ":" + minute;
    }

    public String write() {
        String x = this.getHour() + "\t" + this.timeMinute + "\t";
        if (provenance != null) {
            x = x + provenance;
        }
        x = x + "\t";
        if (textTime != null) {
            x = x + textTime;
        }
        return x;
    }

    public static Time create(String timeRaw) {
        if (Util.empty(timeRaw)) return null;
        Util.NList split = Util.split(timeRaw, "\t");
        Time time = new Time(split.get(0), split.get(1));
        time.provenance = split.get(2);
        time.textTime = split.get(3);
        return time;
    }
}