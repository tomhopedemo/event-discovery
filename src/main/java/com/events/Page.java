package com.events;

import com.events.date.Calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.events.date.DateMatcher.constructIntermediateDates;
import static com.events.date.M_Static.*;
import static com.events.Util.*;
import static com.events.date.Weekdays.WEEKDAYS_LONG;
import static com.events.date.Weekdays.WEEKDAYS_ORDER;

class Page {
    static List<Url> generateUrls(Context ctx, InputData inputData) {
        List<Url> urls = urlConversion(ctx, inputData);
        urls.removeIf(e -> (e == null || empty(e.url)));
        return urls;
    }

    static List<Url> urlConversion(Context ctx, InputData inputData) {
        Set<Url> toReturn = set();
        List<String> urls = inputData.getUrls(ctx.ref);
        if (empty(urls)) return list();
        for (String url : urls) {
            Set<Url> dateModifiedUrls = set();
            List<Url> paginatedUrls = list();
            Calendar.CalendarDate from = Calendar.currentCalendarDate();
            int fromIndex = Calendar.indexOf(from);
            List<Calendar.Date> dates;
            Calendar.CalendarDate until;
            if (ctx.pageWeekly()) {
                until = Calendar.get(fromIndex + 40);
                dates = constructIntermediateDates(from.toDate(), until.toDate(), true);
                dates.removeIf(date -> !date.dayOfWeek.equals(from.dayOfWeek));
            } else if (ctx.pageMonthly() || Util.contains(url, list("ZZZZ-NN-DD", "VVVVVVVVVV"))) {
                until = Calendar.get(fromIndex + 40);
                dates = constructIntermediateDates(from.toDate(), until.toDate(), true);
                dates.removeIf(d -> !(d.equals(from.toDate()) || d.dateDay.equals("1")));
            } else {
                until = Calendar.get(fromIndex + 20);
                dates = constructIntermediateDates(from.toDate(), until.toDate(), true);
            }
            List<String> dateTemplates = list("YYYY", "AAAAAAAAAA", "month=M", "month/MM", "month=mmmm", "XXXXXXXXXX", "VVVVVVVVVV", "MMMM", "/mmmm");
            boolean containsDateTemplate = Util.contains(url, dateTemplates);
            boolean containsSingleDateTemplate = Util.contains(url, list("YYYYMMDD-YYYYMMDD", "ZZZZ-NN-DD"));
            List<String> pageTemplates = list("page/P", "page=P", "Page=P", "pg=P", "rank=RR", "page=Q", "page-P", "paged=P", "paged=Q", "page/L");
            String pageTemplate = null;
            for (String template : pageTemplates) {
                if (url.contains(template)) pageTemplate = template;
            }
            if (url.contains("YYYYMMDD-YYYYMMDD")) {
                String daypadleft = Util.padleft(from.dateDay, '0', 2);
                String dayuntilPadleft = Util.padleft(until.dateDay, '0', 2);
                String M = String.valueOf(MONTHS_ORDER.indexOf(from.dateMonth) + 1);
                String mUntil = String.valueOf(MONTHS_ORDER.indexOf(until.dateMonth) + 1);
                String yyyymmdd = from.dateYear + Util.padleft(M, '0', 2) + daypadleft;
                String yyyymmddUntil = until.dateYear + Util.padleft(mUntil, '0', 2) + dayuntilPadleft;
                dateModifiedUrls.add(new Url(url.replaceAll("YYYYMMDD-YYYYMMDD", yyyymmdd + "-" + yyyymmddUntil)));
            } else if (url.contains("m=N")) {
                String newUrl = url.replace("YYYY", from.dateYear).replace("ZZZZ", until.dateYear).replace("m=M", "m=" + (MONTHS_ORDER.indexOf(from.dateMonth) + 1)).replace("m=N", "m=" + (MONTHS_ORDER.indexOf(until.dateMonth) + 1)).replace("d=D", "d=" + from.dateDay).replace("d=E", "d=" + until.dateDay);
                dateModifiedUrls.add(new Url(newUrl));
            } else if (containsDateTemplate) {
                for (Calendar.Date date : dates) {
                    Calendar.CalendarDate plusOneMonth = Calendar.get(Calendar.indexOf(date) + 31);
                    Calendar.Date plusOneMonthPadded = Calendar.Date.paddedDate(plusOneMonth.toDate());
                    java.util.Calendar calendar = new java.util.GregorianCalendar();
                    calendar.set(Integer.valueOf(date.dateYear), MONTHS_ORDER.indexOf(date.dateMonth), Integer.valueOf(date.dateDay), 0, 0, 0);
                    String milliTime = String.valueOf(calendar.getTimeInMillis());
                    String milliTimeBuffered = String.valueOf(calendar.getTimeInMillis() - 100000);
                    String milliTimeNextDay = String.valueOf(calendar.getTimeInMillis() + 90000000);
                    String daypadleft = Util.padleft(date.dateDay, '0', 2);
                    String M = String.valueOf(MONTHS_ORDER.indexOf(date.dateMonth) + 1);
                    String N = String.valueOf(MONTHS_ORDER.indexOf(plusOneMonthPadded.dateMonth) + 1);
                    String NN = Util.padleft(N, '0', 2);
                    String MM = Util.padleft(M, '0', 2);
                    String mmm = MONTHS_ORDER.get(MONTHS_ORDER.indexOf(date.dateMonth)).toLowerCase();
                    String mmmm = MONTHS_LONG.get(MONTHS_ORDER.indexOf(date.dateMonth)).toLowerCase();
                    String yyyy_mm_dd = date.dateYear + "-" + MM + "-" + daypadleft;
                    String dd_mm_yyyy = daypadleft + "-" + MM + "-" + date.dateYear;
                    String zzzz_nn_dd = plusOneMonthPadded.dateYear + "-" + NN + "-" + plusOneMonthPadded.dateDay;
                    String yyyysmmsdd = date.dateYear + "/" + MM + "/" + daypadleft;
                    String ddsmmsyyyy = daypadleft + "/" + MM + "/" + date.dateYear;
                    String yyyysmmmsdd = date.dateYear + "/" + mmm + "/" + daypadleft;
                    String yyyymmdd = date.dateYear + MM + daypadleft;
                    String yyyy_mm = date.dateYear + "-" + MM;
                    String yyyymm = date.dateYear + MM;
                    String mmsyyyy = MM + "/" + date.dateYear;
                    String modifiedUrl = url.replaceAll("YYYY-MM-DD", yyyy_mm_dd);
                    modifiedUrl = modifiedUrl.replaceAll("DD-MM-YYYY", dd_mm_yyyy);
                    modifiedUrl = modifiedUrl.replaceAll("YYYY/MM/DD", yyyysmmsdd);
                    modifiedUrl = modifiedUrl.replaceAll("YYYY/MMM/DD", yyyysmmmsdd);
                    modifiedUrl = modifiedUrl.replaceAll("DD/MM/YYYY", ddsmmsyyyy);
                    modifiedUrl = modifiedUrl.replaceAll("YYYY\\-MM", yyyy_mm);
                    modifiedUrl = modifiedUrl.replaceAll("YYYYMMDD", yyyymmdd);
                    modifiedUrl = modifiedUrl.replaceAll("YYYYMM", yyyymm);
                    modifiedUrl = modifiedUrl.replaceAll("MM/YYYY", mmsyyyy);
                    modifiedUrl = modifiedUrl.replaceAll("YYYY/MM", date.dateYear + "/" + MM);
                    modifiedUrl = modifiedUrl.replaceAll("YYYY/mmmm", date.dateYear + "/" + mmmm);
                    modifiedUrl = modifiedUrl.replaceAll("year=YYYY", "year=" + date.dateYear);
                    modifiedUrl = modifiedUrl.replaceAll("month=MM", "month=" + MM);
                    modifiedUrl = modifiedUrl.replaceAll("month=M", "month=" + M);
                    modifiedUrl = modifiedUrl.replaceAll("month/MM", "month/" + MM);
                    modifiedUrl = modifiedUrl.replaceAll("month=mmmm", "month=" + mmmm);
                    modifiedUrl = modifiedUrl.replaceAll("-mmm-", "-" + mmmm + "-");
                    modifiedUrl = modifiedUrl.replaceAll("mmmm-YYYY", mmmm + "-" + date.dateYear);
                    modifiedUrl = modifiedUrl.replaceAll("/mmmm", "/" + mmmm);
                    modifiedUrl = modifiedUrl.replaceAll("day=DD", "day=" + date.dateDay);
                    modifiedUrl = modifiedUrl.replaceAll("WWWW", date.dayOfWeek);
                    modifiedUrl = modifiedUrl.replaceAll("DDDD", date.dateDay);
                    modifiedUrl = modifiedUrl.replaceAll("MMMM", date.dateMonth);
                    modifiedUrl = modifiedUrl.replaceAll("YYYY", date.dateYear);
                    modifiedUrl = modifiedUrl.replaceAll("ZZZZ-NN-DD", zzzz_nn_dd);
                    modifiedUrl = modifiedUrl.replaceAll("ZZZZ", plusOneMonth.dateYear);
                    modifiedUrl = modifiedUrl.replaceAll("m=N", "m=" + plusOneMonth.dateMonth);
                    modifiedUrl = modifiedUrl.replaceAll("d=E", "d=" + plusOneMonth.dateDay);
                    modifiedUrl = modifiedUrl.replaceAll("XYZDAY", WEEKDAYS_LONG.get(WEEKDAYS_ORDER.indexOf(date.dayOfWeek)));
                    modifiedUrl = modifiedUrl.replaceAll("DST", date.dateDay + DAY_ORDINAL.get(Integer.parseInt(date.dateDay) - 1));
                    modifiedUrl = modifiedUrl.replaceAll("AAAAAAAAAA", milliTimeBuffered.substring(0, 10));
                    modifiedUrl = modifiedUrl.replaceAll("BBBBBBBBBB", milliTimeNextDay.substring(0, 10));
                    modifiedUrl = modifiedUrl.replaceAll("XXXXXXXXXX", milliTime.substring(0, 10));
                    modifiedUrl = modifiedUrl.replaceAll("VVVVVVVVVV", milliTime.substring(0, 10));
                    dateModifiedUrls.add(new Url(modifiedUrl, date));
                }
            } else {
                dateModifiedUrls.add(new Url(url));
            }
            int pNum = 5;
            int qNum = 5;
            int rNum = 5;
            int lNum = 3;
            if (pageTemplate != null) {
                for (Url modifiedUrl : dateModifiedUrls) {
                    List<String> pageTemplates1 = list("page=P", "Page=P", "pg=P", "page=Q", "page-P", "paged=P", "page/P", "paged=Q", "page/L");
                    if (Util.contains(modifiedUrl.url, pageTemplates1)) {
                        paginatedUrls.add(new Url(modifiedUrl.url.replaceAll(pageTemplate, "")));
                        String delimiter = "=";
                        if (pageTemplate.equals("page-P")) delimiter = "-";
                        if (pageTemplate.contains("/")) delimiter = "/";
                        String delimiterRegex = "=";
                        if (pageTemplate.equals("page-P")) delimiterRegex = "\\-";
                        if (pageTemplate.contains("/")) delimiterRegex = "/";
                        if (containsSingleDateTemplate) {
                            for (int i = 1; i < 10; i++) {
                                paginatedUrls.add(new Url(modifiedUrl.url.replaceAll(pageTemplate, Util.split(pageTemplate, delimiterRegex).get(0) + delimiter + i)));
                            }
                        } else if (containsDateTemplate) {
                            paginatedUrls.add(new Url(modifiedUrl.url.replaceAll(pageTemplate, Util.split(pageTemplate, delimiterRegex).get(0) + delimiter + "1")));
                            paginatedUrls.add(new Url(modifiedUrl.url.replaceAll(pageTemplate, Util.split(pageTemplate, delimiterRegex).get(0) + delimiter + "2")));
                        } else {
                            int num;
                            if (Util.contains(modifiedUrl.url, list("page=Q", "paged=Q"))) {
                                num = qNum;
                            } else if (modifiedUrl.url.contains("page/L")) {
                                num = lNum;
                            } else {
                                num = pNum;
                            }
                            for (int i = 0; i < num; i++) {
                                String newUrl = modifiedUrl.url.replaceAll(pageTemplate, Util.split(pageTemplate, delimiterRegex).get(0) + delimiter + i);
                                paginatedUrls.add(new Url(newUrl));
                            }
                        }
                    } else if (modifiedUrl.url.contains("rank=RR")) {
                        for (int i = 0; i < rNum; i++) {
                            paginatedUrls.add(new Url(modifiedUrl.url.replaceAll("rank=RR", "rank=" + (1 + (i * 10)))));
                        }
                    } else {
                        paginatedUrls.add(modifiedUrl);
                    }
                }
            }
            if (!empty(paginatedUrls)) {
                toReturn.addAll(paginatedUrls);
            } else if (!empty(dateModifiedUrls)) {
                toReturn.addAll(dateModifiedUrls);
            } else {
                toReturn.add(new Url(url));
            }
        }
        return new ArrayList<>(toReturn);
    }
}