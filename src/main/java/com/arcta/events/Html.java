package com.arcta.events;
import com.arcta.events.Calendar.CalendarDate;

import java.io.File;
import java.util.*;

import static com.arcta.events.Html.HtmlHelper.href;
import static com.arcta.events.Html.HtmlHelper.hrefUnderline;
import static com.arcta.events.Util.*;
import static com.arcta.events.Weekdays.WEEKDAYS_LONG;
import static com.arcta.events.Weekdays.WEEKDAYS_ORDER;
import static com.arcta.events.M_Static.MONTHS_ORDER;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
class Html { Set<String> outputReferences = Util.set();
    static final String FAVICON = "/favicons/favicon_l.ico";
    Dirs dirs; InputData inputData;
    Html(Dirs dirs, InputData inputData) {this.dirs = dirs; this.inputData = inputData;}
    Map<Integer, DisplayEvent> execute(HtmlContext ctx) { File[] files = new File(dirs.getOutputDir()).listFiles();
        if (files == null) files = new File[0];
        Map<Calendar.Date, File> datefile = map();
        for (File file : files) {datefile.put(extractDate(file.getName()), file);}
        int currentIndex = Calendar.indexOfCurrent();
        CalendarDate baseDate = Calendar.get(currentIndex + 1);
        String lhsWeekday = baseDate.dayOfWeek;
        int numdays = 8;
        if (ctx.extended) { numdays = 30;}
        boolean indexWritten = false;
        for (int plusDays = 0; plusDays < numdays; plusDays++) { CalendarDate cd = Calendar.get(currentIndex + 1 + plusDays);
            Calendar.Date idate = cd.toDate();
            File ifile = datefile.get(idate);
            String data = null;
            if (ifile != null) {data = Util.readString(ifile.getAbsolutePath());}
            Calendar.Date date = new Calendar.Date(idate.dateDay, idate.dateMonth, null);
            if (isBankHoliday(date)) {data = null;}
            String weekday = WEEKDAYS_LONG.get(WEEKDAYS_ORDER.indexOf(cd.dayOfWeek));
            Util.PageData pageData;
            if ("festival".equals(ctx.type)){ pageData = new DataHtml(inputData).generateFestivalPageData(idate);
            } else { pageData = new DataHtml(inputData).generatePageData(data, ctx, baseDate);}
            Util.Multi<String, DisplayEvent> htmlMain = generateHtml(cd.dayOfWeek, lhsWeekday, baseDate, cd, ctx, pageData);
            if (ctx.reference != null) continue;
            indexWritten = writeHtmls(ctx, indexWritten, plusDays, date, weekday, htmlMain, pageData, idate.dateYear);}
        return null;}
    boolean writeHtmls(HtmlContext ctx, boolean indexWritten, int plusDays, Calendar.Date date, String weekday, Util.Multi<String, DisplayEvent> htmlMain, Util.PageData pageData, String year) { String typepart = ctx.type == null ? "" : ctx.type.toLowerCase() + "/";
        String areapart = ctx.area == null ? "" : ctx.area.toLowerCase() + "/";
        String html = htmlMain.a;
        String htmlFile = dirs.getHtmlDir() + typepart + areapart + weekday + "-" + date.dateDay + "-" + date.dateMonth;
        String property = System.getProperty("os.name");
        if ("Mac OS X".equals(property)) {htmlFile = htmlFile + ".html";}
        sout("Writing: " + htmlFile);
        Util.write(htmlFile, html);
        if (("anywhere".equals(ctx.area))){ sout("writing tsv");
            String month = padleft(valueOf((MONTHS_ORDER.indexOf(date.dateMonth) + 1)), '0', 2);
            String paddedDay = padleft(valueOf(date.dateDay), '0',2);
            String tsvFile = dirs.getTsvDir() + year + "-" + month + "-" + paddedDay + ".tsv";
            StringBuilder sb = new StringBuilder();
            sb.append("title\torganizer\ttime\turl\n");
            for (DisplayEvent event : pageData.events) {sb.append(event.title + "\t" + event.ref + "\t" + event.time + "\t" + event.link +"\n");}
            Util.write(tsvFile, sb.toString());
            String tsvMetaFile = dirs.getTsvMetaDir() + year + "-" + month + "-" + paddedDay + ".tsv";
            sb = new StringBuilder();
            sb.append("title\torganizer\ttime\turl\tstatus\nfull title\n\n");
            for (DisplayEvent event : pageData.events) { String status = "Y".equals(event.soldout) ? "SOLDOUT" : "AVAILABLE";
                sb.append(event.title + "\t" + event.ref + "\t" + event.time + "\t" + event.link + "\t" + status + "\n" + event.fullTitle + "\n\n");}
            Util.write(tsvMetaFile, sb.toString());}
        if (plusDays == 0 && !isBankHoliday(date)) { writeIndex(typepart, areapart, html);
            indexWritten = true;
        } else if (plusDays == 1 && !isBankHoliday(date) && !indexWritten) { writeIndex(typepart, areapart, html);
            indexWritten = true;
        } else if (plusDays == 2 && !isBankHoliday(date) && !indexWritten) { writeIndex(typepart, areapart, html);}return indexWritten;}
    void writeIndex(String typepart, String areapart, String html) { String path = dirs.getHtmlDir() + typepart + areapart + "index.html"; sout("Writing: " + path);
        Util.write(path, html);}
    boolean isBankHoliday(Calendar.Date date) { return ("24".equals(date.dateDay) || "25".equals(date.dateDay) || "26".equals(date.dateDay)) && "dec".equals(date.dateMonth);}
    Calendar.Date extractDate(String fileName) {
        String[] split = fileName.substring(0, fileName.length() - 4).split("-");
        if (split.length < 3) return null;
        Calendar.Date date = new Calendar.Date(valueOf(parseInt(split[3])), MONTHS_ORDER.get(parseInt(split[2]) - 1), split[1]);
        Calendar.setDayOfWeek(date); return date;}
    String generateStartMenu(HtmlContext ctx, String datePart, String dayOfWeek){ String areaOrTypeToDisplay;
            if (!Util.empty(ctx.type)){ areaOrTypeToDisplay = ctx.type;
            } else {
                if (Util.empty(ctx.area)){ areaOrTypeToDisplay = inputData.getCity();
                } else { areaOrTypeToDisplay = ctx.area;}}
            return generateMenu(datePart, areaOrTypeToDisplay, !Util.empty(ctx.type), inputData.getCity(), dayOfWeek);}
    Util.Multi<String, DisplayEvent> generateHtml(String currentPageWeekday, String lhsWeekday, CalendarDate baseDate, CalendarDate currentPageDate, HtmlContext ctx, Util.PageData pageData) {         StringBuilder html = new StringBuilder();
        String datePart = Util.lowercase(Html.generateDatePart(currentPageWeekday, 0, currentPageDate));
        String header = generateHeader(ctx.area, ctx.type, currentPageWeekday, inputData.getCity());
        String head = generateStart(currentPageWeekday, lhsWeekday, baseDate, currentPageDate, ctx, datePart);
        html.append(header).append(head);
        String style = " style=\"padding-top:0px;margin-top:0px;margin-left:auto;margin-right:auto;" + "max-width: 500px;width: calc(100vw - 80px);min-width: 340px;\"";
        html.append("      <ul class=\"xw3-ul \"" + style +">\n  ");
        int numEventsToDisplay = pageData.events.size();
        numEventsToDisplay = ("anywhere".equals(ctx.area) || Util.empty(ctx.area)) ? numEventsToDisplay : Math.min(numEventsToDisplay, 20) ;
        for (int index = 0; index < numEventsToDisplay; index++) {
            DisplayEvent event = pageData.events.get(index);
            if (Util.empty(event.title)){ continue;}
            outputReferences.add(event.ref);
            html.append(constructElement(Html.colorBlend, event));
            if (event.ref.equalsIgnoreCase(ctx.reference)) { sout(currentPageWeekday.toUpperCase() + "\t\t" + event.title);}
            if (index == 0){ html.append("      <ul class=\"xw3-ul \"" + " style=\"margin-top:0px;padding-top:0px\"" +">\n  ");}}
        html.append("</ul></ul>\n").append(footer(false)); return new Util.Multi<>(html.toString(), pageData.mainpageEvent);}
    String constructElement(List<String> colorblend, DisplayEvent event) { String elmt = "<a class=\"asty\" href=\"" + event.link + "\" " + "target=\"_blank\"" + ">\n" +"<li class=\"licla\">\n" + "<span class=\"cutive\">";
        elmt = elmt + "<span class=\"interior\"" + " " + "style=\"" +  getColorOverride(colorblend, event) + "\"" + ">" + "<i style=\"white-space:pre;"+ (event.title.length() > 30 ? "font-size:13px;": "") + "\"> " + event.title + " " + "<span style=\"font-size:13px\">" + event.time + "</span> " + "</i>" + "</span></span>";
        String displayUnder;
        if (Util.empty(event.displayOrganizer) && Util.empty(event.district)){ displayUnder = "";
        } else if (!Util.empty(event.displayOrganizer) && event.displayOrganizer.contains(event.district)){ displayUnder = event.displayOrganizer;
        } else { displayUnder = event.displayOrganizer + " | " + event.district;}
        return elmt + "<span style=\"text-align:right;\" class=\"cutive\"><i style=\"white-space:pre;font-size:12px\">" + displayUnder + " </i></span>" + "</li>\n" + "</a>\n";}
    String getColorOverride(List<String> colorblend, DisplayEvent displayEvent) {return "background:#" + colorblend.get(Math.min(displayEvent.colorIndex, colorBlend.size() -1));}
    String generateStart(String currentPageWeekday, String lhsWeekday, CalendarDate baseDate, CalendarDate currentPageDate, HtmlContext ctx, String datePart) {
        String head = "<body class=\"cool\" style=\"padding:0;margin:0;text-align:center;background:#fbfbfb\">\n";
        head = head +  generateWeekdays(currentPageWeekday, lhsWeekday, ctx.area, baseDate, 0, currentPageDate, "10", null, inputData.getCity());
        if (ctx.extended){ head = head +  generateWeekdays(currentPageDate.dateDay, lhsWeekday, ctx.area, baseDate, 1, currentPageDate, "10", null, inputData.getCity());
            head = head +  generateWeekdays(currentPageDate.dateDay, lhsWeekday, ctx.area, baseDate, 2, currentPageDate, "10", null, inputData.getCity());
            head = head +  generateWeekdays(currentPageDate.dateDay, lhsWeekday, ctx.area, baseDate, 3, currentPageDate, "10", null, inputData.getCity());}
        head = head + generateStartMenu(ctx, datePart, currentPageWeekday); return head;}
    String generateMenu(String datepart, String areaOrType, boolean isType, String city, String dayOfWeek) {String displayspaced;String part;
        String weekday = WEEKDAYS_LONG.get(WEEKDAYS_ORDER.indexOf(dayOfWeek)).toUpperCase();
        if ("anywhere".equals(areaOrType)){ displayspaced = displaySpaced("THE " + weekday + " GUIDE");
            part = datepart;
        } else if (!Util.empty(areaOrType)) {
            if (isType){areaOrType = areaOrType + " " + weekday;}
            displayspaced = displaySpaced(areaOrType);
            part = datepart;
        } else { displayspaced = displaySpaced(areaOrType);
            part = city + "/" + datepart;}
        String imgAndText = "<img height=\"32px\" width=\"32px\" style=\"vertical-align:middle;\" src=\"/up3.svg\">";// + text;
        String href = "london".equals(city) ? href(imgAndText, "/" + part) : HtmlHelper.hrefNowhere("   " + displayspaced + "   ");
        return "<p style=\"padding-top:9px;padding-bottom:15px;margin-bottom:0px;margin-top:0px;white-space:pre;text-align:center;\">" +"<span style=\"font-size:16px;color:#202020;white-space:pre\">"+ href + "</span></p>";}
    static String generateHeader(String area, String type, String dayOfWeek, String city){
        String title = generateTitle((Util.empty(area) ? type : area), WEEKDAYS_LONG.get(WEEKDAYS_ORDER.indexOf(dayOfWeek)), city); return HtmlHeader.generate(title, FAVICON);}
    static String generateTitle(String areaOrType, String dayOfTheWeek, String city) {String title;
        if (Util.empty(city)){title = "";
        } else if ("london".equals(city)) {
            if (Util.empty(areaOrType) || "anywhere".equals(areaOrType)){ title = "The " +  uppercaseFirstLetter(dayOfTheWeek) + " Guide - Londonoo";
            } else { title = PhraseUtil.uppercaseAllWords(areaOrType) + " - Londonoo";
            }} else {
            title = uppercaseFirstLetter(city) + " | Find events for this week in " + uppercaseFirstLetter(city);} return title;}
    static String footer(boolean showCalendarLink){String footer = "";
        if (showCalendarLink){ String elmt = "<a class=\"asty\" style=\"padding-top:10px;padding-right:10px;\" href=\"https://londonoo.com/west-end-musicals\">\n" + "<span class=\"interiorloc\" style=\"background:#fbfbfb\"><b>" + " West End + Musicals" + "</b></span>" +"</span>" +  "\n" +"</a>\n";;
            footer = elmt +"<a class=\"asty\" style=\"padding-top:10px\" href=\"https://londonoo.com/calendar\">\n"+ "<span class=\"interiorloc\" style=\"background:#fbfbfb\"><b>" + " Calendar View " + "</b></span>"+ "</span>"+ "\n" +"</a>\n";}
        return footer + "<p style=\"padding-top:20px;padding-bottom:30px;margin-bottom:0px;margin-top:0px;white-space:pre;text-align:center;\">" + "<span style=\"font-size:14px;color:#202020;white-space:pre\">" + "© londonoo.com 2024</span></p>" + "</body>\n" + "</html>\n";}
    static List<String> colorBlend = Util.list("F5BEB6","F1BFB8","EEC1BA","EAC2BC","E6C3BE","E2C4C0","DFC6C2","DBC7C4","D7C8C6","D3C9C8","D0CBCA","CCCCCC");
    static String generateWeekdays(String selected, String currentWeekday, String area, CalendarDate baseDate, int week, CalendarDate currentPageDate, String marginbottom, String country, String city) {
        if (Util.empty(marginbottom)) marginbottom = "210";
        List<String> weekdays = HtmlHelper.nextDays(currentWeekday);
        StringBuilder sb = new StringBuilder();
        sb.append("<p style=\"padding-top:"+ (week > 0 ? "0" : "10") + "px;margin-top:15px;margin-bottom:" + marginbottom + "px;white-space:pre;text-align:center;font-size:15px\">");
        String citypart = "";
        if (!Util.empty(city)){
            if ("london".equalsIgnoreCase(city)){
                if (!Util.empty(area)){
                    if ("anywhere".equals(area)){ citypart = "/guide/";
                    } else {citypart = "/" + area.toLowerCase() + "/";}
                } else {citypart = "";}
            } else { citypart = "/" + city.toLowerCase() + "/";}}
        if (!Util.empty(country)){citypart = "https://" + country + ".londonoo.com/";}
        for (int i = 0; i < 7; i++) { String datepart = generateDatePart(currentWeekday, i + (week * 7), baseDate);
            CalendarDate offsetDate = generateOffsetDate(i + (week * 7), baseDate);
            String display = week == 0 ?  uppercaseFirstLetter(weekdays.get(i).toLowerCase()) : ' ' + offsetDate.dateDay + ' ';
            if (display.toLowerCase().trim().equals(selected.toLowerCase()) && currentPageDate.equals(offsetDate)) {
                    sb.append("   " + "<span style=\"color:#505050;font-size:17px;\">" + hrefUnderline(display, citypart + datepart) + "</span>" + "  ");
                } else { sb.append("   " + "<span style=\"color:#505050;font-size:17px;\">" + href(display, citypart + datepart) + "</span>" + "  ");}}
        sb.append(" </p>"); return sb.toString();}
    static String displaySpaced(String performanceString) { if (performanceString == null) return null;
        performanceString = performanceString.toUpperCase();
        StringBuilder builder = new StringBuilder();
        for (char c : performanceString.toCharArray()) { builder.append(c + " ");}
        if (builder.length() > 0) {performanceString = builder.substring(0, builder.length() - 1);} return performanceString;}
    static String generateDatePart(String currentWeekday, int offset, CalendarDate baseDate){ List<String> weekdaysLong = HtmlHelper.nextDaysLong(currentWeekday);
        List<CalendarDate> calendarDays = HtmlHelper.nextDays(baseDate);
        return weekdaysLong.get(offset) + "-" + calendarDays.get(offset).dateDay + "-" + calendarDays.get(offset).dateMonth;}
    static CalendarDate generateOffsetDate(int offset, CalendarDate baseDate){ List<CalendarDate> calendarDays = HtmlHelper.nextDays(baseDate); return calendarDays.get(offset);}
    static class HtmlWestEndLondon { final Dirs dirs; final InputData londonInputData;
        HtmlWestEndLondon(Dirs dirs, InputData londonInputData) {this.dirs = dirs;this.londonInputData = londonInputData;}
        void execute() { Map<String,String> refTheatre = map();
            List<String> refs = londonInputData.getWestEndData();
            LinkedHashMap<String,String> refUrl = new LinkedHashMap<>();
            for (String ref : refs) { refUrl.put(ref, londonInputData.getUrls(ref).get(0));
                refTheatre.put(ref, londonInputData.getDistrict(ref));}
            String path = dirs.getHtmlDir() + "west-end-musicals";
            sout("Writing: " + path);
            Util.write(path, HtmlHeader.generate("Londonoo", FAVICON) + generate(refs, refUrl, refTheatre) + footer(false));}
        String generate(List<String> refs, Map<String,String> refUrl, Map<String, String> refTheatre) { StringBuilder sb = new StringBuilder();
            String head = "<body class=\"cool\" style=\"padding:0;margin:0;text-align:center;background:#fbfbfb\">\n";
            String menu = "<p style=\"padding-top:10px;padding-bottom:0px;margin-bottom:10px;margin-top:15px;white-space:pre;text-align:center;\">"+ "<span style=\"font-size:16px;color:#ffffff;white-space:pre\">"+ HtmlHelper.hrefPad("   " + displaySpaced("WEST END + MUSICALS") + "   ", "#")+ "</span></p>";
            String href = href("<img height=\"32px\" width=\"32px\" style=\"vertical-align:middle;\" src=\"/up3.svg\">", "https://londonoo.com");
            String logo = "<p style=\"padding-top:9px;padding-bottom:15px;margin-bottom:15px;margin-top:0px;white-space:pre;text-align:center;\">"+ "<span style=\"font-size:16px;color:#202020;white-space:pre\">"+ href+ "</span></p>";
            String style = " style=\"padding-top:0px;margin-top:0px;margin-left:auto;margin-right:auto;" + "max-width: 500px;width: calc(100vw - 80px);min-width: 340px;\"";
            sb.append(head).append(menu).append(logo).append("      <ul class=\"xw3-ul \"" + style +">\n  ");
            for (int index = 0; index < refUrl.size(); index++) { String ref = refs.get(index);
                String elmt = constructElement(ref, refUrl.get(ref),  refTheatre.get(ref));
                sb.append(elmt);
                if (index == 0){sb.append("      <ul class=\"xw3-ul \"" + " style=\"margin-top:0px;padding-top:0px\"" +">\n  ");}}
            sb.append("</ul></ul>\n"); return sb.toString();}
        String constructElement(String ref, String link, String theatre) {
        String elmt = "<a class=\"asty\" href=\"" + link + "\" " + "target=\"_blank\"" + ">\n";
        elmt = elmt + "<li class=\"licla\">\n";
        elmt = elmt + "<span class=\"cutive\">";
        elmt = elmt + "<span class=\"interior\"" + " " + "style=\"" + "background:#F5BEB6" + "\"" + ">" + "<i " + "style=\"white-space:pre;" + "\"" + "> " + ref + " " + "</i>" + "</span>" + "</span>";
        elmt = elmt + "<span style=\"text-align:center;\" class=\"cutive\"><i style=\"white-space:pre;font-size:12px\">" + Util.safeNull(theatre) + " </i></span>";
        elmt = elmt + "</li>\n" + "</a>\n"; return elmt;}}
    static class HtmlAreasLondon {Dirs dirs; String city;
        HtmlAreasLondon(Dirs dirs, String city) {this.dirs = dirs;this.city = city;}
        void execute() { List<String> areasDisplayLondon = London.areasDisplay();
            Collections.sort(areasDisplayLondon);
            int currentIndex = Calendar.indexOfCurrent();
            CalendarDate baseDate = Calendar.get(currentIndex + 1);
            for (int plusDays = 0; plusDays < 7; plusDays++) { CalendarDate cd = Calendar.get(currentIndex + 1 + plusDays);
                Calendar.Date date = cd.toDate();
                String weekday = WEEKDAYS_LONG.get(WEEKDAYS_ORDER.indexOf(cd.dayOfWeek));
                String generated = generate(areasDisplayLondon, cd.dayOfWeek, baseDate.dayOfWeek, baseDate, cd);
                String html = generateHeader() + generated + footer(true);
                String path = dirs.getHtmlDir() + weekday + "-" + date.dateDay + "-" + date.dateMonth;
                sout("Writing: " + path);
                Util.write(path, html);
                if (plusDays == 0) {Util.write(dirs.getHtmlDir() + "index.html", html);}}}
        String generate(List<String> list, String currentPageWeekday, String lhsWeekday, CalendarDate baseDate, CalendarDate currentPageDate){
            StringBuilder sb = new StringBuilder();
            String datePart = Util.lowercase(generateDatePart(currentPageWeekday, 0, currentPageDate));
            String head = "<body class=\"cool\" style=\"padding:0;margin:0;text-align:center;background:#fbfbfb\">\n";
            head = head +  generateWeekdays(currentPageWeekday, lhsWeekday, null, baseDate, 0, currentPageDate, "10", null, city);
            sb.append(head);
//            String menu = generateMenuNoLink("30", "LONDONOO");
//            sb.append(menu);
            List<String> types = Util.list("Music", "Theatre","Comedy","Film");
            List<String> secondaryLocations = new ArrayList<>(list);
            List<String> areas = Util.list();
            areas.addAll(types);
            areas.add("anywhere");
            areas.addAll(secondaryLocations);
            sb.append("<ul class=\"xw3-ul\" style=\"padding-top:0px;margin-top:0px;margin-left:auto;margin-right:auto;" + "max-width: 500px;width: calc(100vw - 80px);min-width: 340px;\">");
            sb.append("      <ul class=\"xw3-ul\" " + ">" +"\n  ");
            String padding = " style=\"margin-top:10px\"";
            sb.append("      <ul class=\"xw3-ul\"" + padding + ">" +"\n  ");
            for (int i = 0; i < areas.size(); i++) { String area = areas.get(i);
                String additionalStyle = "";
                if (types.contains(area)) { additionalStyle = "style=\"background:#fbfbfb\"";
                } else if ("anywhere".equals(area)){ additionalStyle = "style=\"background:#fdf3f2;white-space:pre\"";}
                String elmt = "<a class=\"asty\" href=\"" + generateUrl(area, datePart) + "\">\n"  + "<li class=\"licla\">\n" + "<span class=\"futura-large\">";
                String uppercased = "anywhere".equals(area) ? "The " + uppercaseFirstLetter(WEEKDAYS_LONG.get(WEEKDAYS_ORDER.indexOf(currentPageWeekday))) + " Guide" : PhraseUtil.uppercaseAllWords(area);
                elmt = elmt + "<span class=\"" + "interiorloc" + "\" " + additionalStyle + "><i>" + uppercased + "</i></span>"+ "</span>" + "</li>\n" +"</a>\n";
                sb.append(elmt); }
            sb.append(" </ul>\n").append(" </ul>\n");
            sb.append(" </ul>\n"); return sb.toString();}
        static String generateMenuNoLink(String marginbottom, String display) { display = displaySpaced(display);
            return "<p style=\"padding-top:9px;padding-bottom:0px;margin-bottom:" + marginbottom +"px;margin-top:0px;white-space:pre;text-align:center;\">" + "<span style=\"font-size:16px;color:#ffffff;white-space:pre\">" + HtmlHelper.hrefPad("   " + display + "   ", "#")+ "</span></p>";}
        String generateUrl(String area, String datepart) { String areaurl = "anywhere".equalsIgnoreCase(area) ?  "/guide/" + datepart :  "/" + area + "/" + datepart; return Util.lowercase(areaurl);}
        String generateHeader() {return HtmlHeader.generate("Londonoo", FAVICON);}}
    static class HtmlHelper {
        static List<String> nextDays(String currentWeekday){ int i = WEEKDAYS_ORDER.indexOf(currentWeekday);
            List<String> weekdays = list();
            for (int j = 0; j < 31; j++) { String s = WEEKDAYS_ORDER.get((i + j) % 7);
                weekdays.add(uppercaseFirstLetter(s));} return weekdays;}
        static List<CalendarDate> nextDays(CalendarDate baseDate){ int i = Calendar.indexOf(baseDate);
            List<CalendarDate> toReturn = list();
            for (int j = 0; j < 31; j++) { toReturn.add(Calendar.get(j + i));} return toReturn;}
        static List<String> nextDaysLong(String currentWeekday){ int i = WEEKDAYS_ORDER.indexOf(currentWeekday);
            List<String> weekdays = list();
            for (int j = 0; j < 31; j++) { String s = WEEKDAYS_LONG.get((i + j) % 7);
                weekdays.add(uppercaseFirstLetter(s));} return weekdays;}
        static String href(String text, String link){
            return "<a style=\"text-decoration:none\" href=\"" + link.toLowerCase() + "\">" + text + "</a>";
        }
        static String hrefNowhere(String text){
            return "<a style=\"text-decoration:none\" href=\"#\">" + text + "</a>";
        }
        static String hrefPad(String text, String link){
            return "<a style=\"text-decoration:none;padding-left:10px;padding-right:10px\" href=\"" + link.toLowerCase() + "\">" + text + "</a>";
        }
        static String hrefUnderline(String display, String link){return "<a style=\"text-decoration:none;border-bottom:" + "1px solid #202020\" href=\"" + link.toLowerCase() + "\">" + display + "</a>";}}
    static class HtmlHeader {
        static String generate(String title, String favicon) { return "<!DOCTYPE html>\n" + "<html>\n" + "<title>" + title + "</title>\n" + generateMeta() + generateLinks(favicon) + generateStyle();}
        static String generateLinks(String favicon) {return "<link rel=\"icon\" href=\"" + favicon + "\"/>";}
        static String generateMeta() {return "<meta charset=\"UTF-8\">\n" +"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +"<meta name=\"description\" content=\"Check out the most exciting events in London today. Browse from hundreds of your favourite London venues to plan your perfect night out.\">\n";}
        static String generateStyle() {
                        return "<style>\n" + ".cool {" +"background-color:#ffffff;\n" +"}\n" +
                                "a:visited {color:#202020}\n" +"a:active {color:#202020}\n" +"a:link {color:#202020}\n" +
                                "li:hover {" +"background-color: transparent;\n" +"}\n" +
                                ".licla {line-height:1.2;padding-top:14px;padding-bottom:6px;padding-left:9px;padding-right:9px;}\n" +
                                ".xw3-ul {list-style-type:none;padding:0;}.xw3-ul li:last-child{border-bottom:none}\n" +
                                ".interior {" +"box-decoration-break:clone;" +"-webkit-box-decoration-break:clone;" +"background:#fbe6e3;" +"border-radius:5px;" +"padding-top:5px;" +"padding-bottom:4px;" +"padding-left:7px;" +"padding-right:7px;" +"}\n" +
                                ".interiorloc {" +"box-decoration-break:clone;" +"-webkit-box-decoration-break:clone;" +"background:#fbe6e3;" +"border-radius:0px;" +"border:1px solid #404040;" +"padding-left:8px;" +"padding-right:8px;" +"padding-top:5px;" +"padding-bottom:5px;"+ "}\n" +
                                ".interiorx {box-decoration-break:clone;" +"-webkit-box-decoration-break:clone;" +"border-radius:0px;" +"padding-left:7px;" +"border-left:1px solid #202020;" +"padding-right:7px;" +"padding-top:4px;" +"padding-bottom:4px;" +"background-color:#202020;" +"color:#f5f5f5;" +"font-size:19px;" +"border:2px solid #f5f5f5;" +"}\n" +
                                ".asty {-webkit-tap-highlight-color:transparent;display:inline-block;text-decoration:none;}\n" +
                                ".cutive {" +"font-family: 'Futura';" +"display:block;" +"padding-bottom:1px;" +"font-size:19px;" +"color:#000000" +"}" +
                                ".futura-large {" +"font-family: 'Futura';" +"display:block;" +"padding-bottom:1px;" +"font-size:19px;" +"color:#000000" +"}" +
                                ".tooltip {\n" +"  position: relative;\n" +"  display: inline-block;\n" +"}" +
                                ".tooltip .tooltiptext {\n" +"  visibility: hidden;\n" +"  width: 120px;\n" +"  background-color: black;\n" +"  color: #fff;\n" +"  text-align: center;\n" +"  padding: 5px 0;\n" +"  border-radius: 6px;\n" +"  position: absolute;\n" +"  z-index: 1;\n" +"}" +
                                ".tooltip:hover .tooltiptext {\n" +"  visibility: visible;\n" +"}  " +
                                "   body,h1,h2,h3,h4,h5,h6 {font-family: \"Arial\", sans-serif;font-size:15px;line-height:1.5}\n" +
                                "@media only screen and (max-width:450px){body{" +"}}\n" +"</style>\n";}}
    static class HtmlContext { String type; String area; String reference; boolean extended; boolean majorsOnly = false;}}