package com.arcta.events;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import static com.arcta.events.Util.*;
class Context { String ref; String notes; List<String> statuses; Interpret interpret; List<Util.Url> urls;
    static boolean GOOGLE_OVERRIDE = false;

    LanguageContext lang; DateContext date; TimeContext time; DateTimeContext datetime;
    List<Util.Url> urls(){return urls;} void setUrls(List<Util.Url> urls){this.urls = urls;}
    Context(String ref, String notes, String country, List<String> statuses) { this.ref = ref; this.notes = notes;
        this.interpret = new Interpret(ref, notes); this.statuses = statuses;
        this.lang = new LanguageContext(ref, notes, country); this.date = new DateContext(ref, notes);
        this.time = new TimeContext(ref, notes); this.datetime = new DateTimeContext(ref, notes);}
    static final List<String> festivalStatuses = Util.list("F1", "F2", "F3");
    boolean isFestival(){ if (statuses == null) return false; return Util.intersection(statuses, festivalStatuses);}
    static Context make(String ref, InputData inputData){return make(ref, null, inputData);}
    static Context make(String ref, String additionalCtxNotes, InputData inputData) { String notes = Util.safeNull(inputData.getNotes(ref));
        if (!Util.empty(additionalCtxNotes)) {notes = notes + " " + additionalCtxNotes;}
        return new Context(ref, notes, "uk", inputData.getStatuses(ref));}
    boolean pageWeekly() {return interpret.contains("_WEEKLY_");}
    boolean pageMonthly() {return interpret.contains("_MONTHLY_");}
    boolean urlDate() {return interpret.contains("_URLDATE_");}
    String primaryFestivalDate() {return interpret.bang("_D");}
    boolean downloadCurl() {return interpret.contains("_CURL_");}
    boolean downloadPhantom() {return interpret.contains("_PHANTOM_", "_YPHANTOM_");}
    boolean downloadGoogle() {return GOOGLE_OVERRIDE || interpret.contains("_GOOGLE_");}
    boolean downloadDelay() {return interpret.contains("_DELAY_");}
    boolean downloadScroll() {return interpret.contains("_SCROLL_");}
    String downloadClickWait() {return interpret.normal("_CLICKWAIT");}
    List<String> staticDay() {return interpret.bangList("_DAY");}
    List<String> datesplitRestrictToTags() { String rtag = interpret.bang("_RTAG");if (Util.empty(rtag)) return null; return Util.list(rtag);}
    List<String> datesplitRestrictToClasses() { String rclass = interpret.bang("_RCLASS");if (Util.empty(rclass)) return null; return Util.list(rclass);}
    String datesplitAfter() {return interpret.bang("_AFTER");}
    String datesplitBefore() {return interpret.bang("_BEFORE");}
    boolean datesplitNoTime() { return interpret.contains("_DATESPLITX_");}
    boolean datesplitReverse() { return interpret.contains("_REVSPLIT_");}
    String timeManual() { return interpret.normal("_TIME");}
    String timeSkip() { return interpret.normal("_SKIPTIME");}
    String linkBadHref() {return interpret.normal("_BADHREF");}
    String linkPoorHref() {return interpret.normal("_POORHREF");}
    String linkGoodHref() {return interpret.normal("_GOODHREF");}
    String linkGoodText() {return interpret.bang("_GOODTEXT");}
    boolean link1Download() {return interpret.contains("_XHEAD_");}
    boolean link1Phantom() {return interpret.contains("_PHANTOM_", "_PHANTOMX_");}
    boolean link1Google() {return GOOGLE_OVERRIDE || interpret.contains("_GOOGLE_", "_GOOGLEX_");}
    boolean link1Iframe() {return interpret.contains("_IFRAMES_");}
    boolean link1ComboOnly() {return interpret.contains("_COMBO_");}
    String link1Class() {return interpret.normal("_LINKTIME");}
    String link1Req() {return interpret.bang("_REQLINK");}
    boolean link1Force() {return interpret.contains("_LINK_");}
    boolean link2Partial() {return interpret.contains("_XXPP_") ;}
    String link2Specific() {return interpret.bang("_SECOND");}
    String link2Href() {return interpret.normal("_HREF2");}
    boolean link2Permitted() {return interpret.contains("_SECOND_", "_SECOND=", "_THIRD=");}
    boolean link2Download() {return interpret.contains("_XHEAD_", "_XXHEAD_");}
    boolean link2Phantom() {return interpret.contains("_PHANTOM_", "_XXPHANTOM_");}
    String link3Text() {return interpret.bang("_THIRD");}
    boolean link3Download() {return interpret.contains("_XHEAD_", "_XXHEAD_");}
    boolean link3Phantom() {return interpret.contains("_PHANTOM_", "_XXPHANTOM_");}
    Util.Multi<String, String> classReqpair() {return interpret.bangPair("_XREQ");}
    Util.Multi<String, String> classExcpair() {return interpret.bangPair("_XEXC");}
    String classReqclass() {return interpret.normal("_REQCLASS");}
    String classExcWithClass() {return interpret.normal("_XWC");}
    List<String> classReqtext0() {return interpret.bangList("_CREQ0");}
    List<String> classReqtext() {return interpret.bangList("_CREQ");}
    List<String> classReqText2() {return interpret.bangList("_CREQ2");}
    List<String> classExcText() {return interpret.bangList("_CEXC");}
    boolean partialEntire() {return notes.contains("_XPART_") || notes.contains("_XMY=");}
    String partialTag() {return interpret.normal("_TAGMY");}
    String partialClass() {String my = interpret.normal("_MY");
        if (Util.empty(my)){my = interpret.normal("_XMY");}return my;}
    String parseGap() {return interpret.normal("_GAP");}
    boolean parseLong() {return interpret.contains("_LONG_");}
    String parseTagGap() {return interpret.normal("_TAGGAP");}
    boolean parseTitleUrl() {return notes.contains("_TITLEURL_");}
    boolean parseNolink() {return interpret.contains("_NOLINK_");}
    boolean remRemDays() {return interpret.contains("_REMDAYS_");}
    boolean remRemDay() {return interpret.contains("_REMDAY_");}
    boolean parseBlank() {return interpret.contains("_BLANK_");}
    String parseExcClasses() {return interpret.bang("_XC");}
    String parseExcTags() {return interpret.bang("_XTAG");}
    String parseExcAttr() {return interpret.bang("_XATTR");}
    String parseSeptextClass() {String septext = interpret.normal("_SEPTEXT");if (septext == null) {septext = interpret.normal("_S");}return septext;}
    String parseSeptextTag() {String septag = interpret.normal("_SEPTAG");if (septag == null){septag = interpret.normal("_ST");}return septag;}
    boolean parseNotitle() {return interpret.contains("_NOTITLE_");}
    boolean parseLate() {return interpret.contains("_LATE_");}
    String parseBeforeHourIncl() {return interpret.normal("_BEFOREHOUR");}
    boolean parseAnchorId() {return interpret.contains("_ANCHORID_");}
    boolean parseAnchorName() {return interpret.contains("_ANCHORNAME_");}
    String parseCsub() {return interpret.normal("_CSUB");}
    boolean parseCsubx() {return interpret.contains("_CSUBX_");}
    boolean parseTimesplit() {return interpret.contains("_TIMESPLIT_");}
    boolean tertiaryPresents() {return interpret.contains("_PRES_");}
    boolean primaryListcut() {return interpret.contains("_LISTCUT_");}
    boolean primaryDatesplit() {return interpret.contains("_DATESPLIT_","_DATESPLITX_");}
    boolean primaryPartial() {return interpret.contains("_PMETH_", "_MY=", "_TAGMY=");}
    String primaryClass() {return interpret.normal("_C");}
    String primaryListClass() {return interpret.normal("_L");}
    String primaryTag() {return interpret.normal("_TAG");}
    String primaryListTag() {return interpret.normal("_LTAG");}
    String primaryId() {return interpret.normal("_ID");}
    String parentId() {return interpret.normal("_PID");}
    String primaryListId() {return interpret.normal("_LID");}
    String primaryClassholder() {return interpret.normal("_CLASSHOLD");}
    String primaryClassholdId() {return interpret.normal("_CLASSHOLDID");}
    boolean primaryList() {return !Util.empty(primaryListClass()) || !Util.empty(primaryListId()) || !Util.empty(primaryListTag());}
    boolean primaryTable() {return interpret.contains("_TMETH_");}
    boolean primaryBreaksplit() {return interpret.contains("_BREAKSPLIT_");}
    String primaryStatic() {return interpret.bang("_STATIC");}
    boolean primaryIframe() {return interpret.contains("_IFRAME_") || interpret.contains("_IFRAME=")|| interpret.contains("_IFRAMEX_") || interpret.contains("_IFRAMEG_") || interpret.contains("_IFRAME2_");}
    String iframeClass() {return interpret.normal("_IFRAME");}
    boolean primaryIframe2() {return interpret.contains("_IFRAME2_");}
    boolean primaryIframex() {return interpret.contains("_IFRAMEX_");}
    boolean primaryIframeGoogle() {return interpret.contains("_IFRAMEG_");}
    String primaryIdRestriction() {return interpret.normal("_RES");}
    String primaryClassRestriction() {return interpret.normal("_CRES");}
    String primaryClassRemoval() {return interpret.normal("_CREM");}
    Util.Multi<String, String> primaryRequiredAttribute() {return interpret.noteMatchPair("_ATTREQ");}
    String primaryExceptClass() {return interpret.normal("_XC1");}
    String primaryClick() {return interpret.normal("_CLICK");}
    String linkxClass() {return interpret.normal("_LINKC");}
    boolean parseKeepRef() {return interpret.contains("_REF_");}
    String secondaryAlternativeName() {return interpret.bang("_ALT");}
    boolean tertiaryExhibition() {return interpret.contains("_EXHIB_");}
    String tertiaryRestrictToDay() {return interpret.normal("_RDAY");}
    boolean tertiaryNotTheatre() {return interpret.contains("_XTHEATRE_") ;}
    boolean tertiaryTheatre() {return interpret.contains("_THEATRE_");}
    boolean tertiaryHome() {return interpret.contains("_HOME_");}
    String tertiaryHomeParam() {return interpret.normal("_HOME");}
    List<String> tertiaryRemove() {String rem = interpret.bang("_REM");if (Util.empty(rem)) return null;return Util.splitSafe(rem, "\\|").underlying;}
    List<String> tertiaryRemoveStart() {String remstart = interpret.bang("_REMSTART");if (Util.empty(remstart)) return null;return Util.splitSafe(remstart, "\\|").underlying;}
    String tertiaryRemAfter() {return interpret.bang("_REMAFTER");}
    String tertiaryRemoveEnd() {return interpret.bang("_REMEND");}
    boolean tertiarySingleOnly() {return interpret.contains("_SIN_");}
    boolean tertiaryDuoOnly() {return interpret.contains("_DUO_");}
    boolean tertiaryEarlier() {return interpret.contains("_EARLIER_");}
    List<String> tertiaryExclusion() { String exc = interpret.bang("_X");
        if (Util.empty(exc)) return null;
        List<String> exclusions = Util.list();
        Util.addAll(exclusions, Util.split(exc, "\\|").underlying);
        return exclusions;}
    List<String> tertiaryLocationRequired() {return interpret.bangList("_LOCREQ");}
    List<String> tertiaryRequired() {return interpret.bangList("_REQ");}
    boolean tertiaryAfterColon() {return interpret.contains("_COLON_");}
    static boolean performLocreq(Event event, Context ctx) {List<String> lsplit = ctx.tertiaryLocationRequired();
        if (Util.empty(lsplit)) return true;
        for (String location : lsplit) {
            if (event.name.toLowerCase().matches(".*\\b" + location.toLowerCase() + "\\b.*")) { event.district = location;
                event.name = event.name.replaceAll("(?i)"+location,"");return true;
            }}return false;}
    static class TimeContext { Interpret interpret;
        TimeContext(String ref, String notes) {this.interpret = new Interpret(ref,  notes);}
        boolean timePm() {return interpret.contains("_PM_");}
        boolean timeDoors() {return interpret.contains("_DOORS_");}}
    static class DateTimeContext {Interpret interpret;
        DateTimeContext(String ref, String notes) {this.interpret = new Interpret(ref,  notes);}
        boolean datetimeReverse() {return interpret.contains("_REVCOMBO_");}}
    static class LanguageContext {
        Interpret interpret; String country;
        LanguageContext(String ref, String notes, String country) { this.interpret = new Interpret(ref,  notes); this.country = country;}
        boolean lang(Util.Lang language){
             return interpret.contains("_" + language.name() + "_") || languagesForCountry().contains(language);
        }
        List<Util.Lang> languages(){
            List<Util.Lang> languages = list();
            languages.add(Util.Lang.ENG);
            for (Util.Lang language : Util.Lang.values()) {
                if (lang(language)){
                    languages.add(language);}}
            return languages;}
        static final List<String> PREPOSITION_WORDS_ENG = list("in", "is", "by", "for", "on", "at", "to", "of", "with", "an", "a", "the", "not", "and", "from", "your", "our", "his", "while", "whose", "as");
        static final List<String> LOWERCASE_ENG = list("is", "this", "more", "do", "present", "presents", "we", "around", "he");
        static List<String> andWords(List<Util.Lang> languages) {return list("and", "+", "&");}
        static Map<String, List<Util.Lang>> countryLanguages;
        static {countryLanguages = Map.of("germany", list(Util.Lang.DEU));}
        List<Util.Lang> languagesForCountry(){
            return safeNull(countryLanguages.get(country));
        }
        List<String> lowercaseWords() {return Util.union(PREPOSITION_WORDS_ENG,LOWERCASE_ENG);}
        List<String> prepositionWords() {return new ArrayList<>(PREPOSITION_WORDS_ENG);}}
    static class DateContext { Interpret interpret;
        DateContext(String ref, String notes) {this.interpret = new Interpret(ref,  notes);}
        boolean dateReverse() {return interpret.contains("_REV_");}
        boolean dateMwd() {return interpret.contains("_MWD_");}
        boolean dateDottedWdmy() {return interpret.contains("_OSLODATE_");}}
        static class Interpret {String ref; String notes;
            Interpret(String ref, String notes) {this.ref = ref;this.notes = notes;}
            String bang(String field){return bang(field, notes);}
            static String bang(String field, String notes){Matcher matcher = matcher(field + "=([^!]*)!", notes);return matcher.find() ? matcher.group(1) : null;}
            List<String> bangList(String field){String note = bang(field);if (empty(note)) return list();return split(note, "\\|").underlying;}
            Util.Multi<String,String> bangPair(String field){String note = bang(field);if (empty(note)) return null;NList split = split(note, "\\|");if (split.size() != 2) return null;return new Util.Multi<>(split.get(0), split.get(1));}
            String normal(String field){Matcher matcher = matcher(field + "=(\\S+)", notes);return matcher.find() ? matcher.group(1) : null;}
            Util.Multi<String,String> noteMatchPair(String field){String pair = normal(field);if (empty(pair)) return null;NList split = split(pair, "\\|");return new Util.Multi<>(split.get(0), split.get(1));}
            boolean contains(String... notelist){for (String tag : notelist) {if (notes.contains(tag)) return true;}return false;}}}