package com.events;

import java.util.List;
import java.util.Map;

import static com.events.FiltersLists.START_QUOTES;
import static com.events.Util.list;
import static com.events.Util.map;

class Upper {
    static final List<String> FORCE_UPPERCASES_CONDITIONAL = list("LA", "IRA");
    static final List<String> FORCE_UPPERCASES = list("QM", "GRL", "GNG", "LDN", "LGBT", "LGBTQ+", "LGBTQ", "DSM", "AJR", "MPG", "BMA", "NRG", "WXW", "MGF", "SSO", "GND", "DB", "DH", "YBC", "LRBS", "MC", "BJ", "ISQ", "PJ", "JD", "JG", "JW", "JP", "JQ", "K-X-P", "DJ", "AJ", "BC", "HK", "HC", "TV", "TJ", "U.K", "UK", "CD", "WSTRN", "LFF", "JMSN", "CDR", "SPQR", "ESP", "LRB", "EU", "MBE", "EUT", "ADC", "TCR", "LPO", "XCX", "MPB", "LIAF", "NYE", "CSI", "R&B", "ABC", "DV", "BF", "BYO", "RSNO", "VFD", "RSNO", "DVS1", "ENTT", "CF19", "LIMF", "NCT", "FTS", "TTW", "RCM", "RCMJD", "ECFF", "J.P.", "TC", "UCL", "LKFF", "L.A", "L.A.", "BBC", "RWCMD", "(UK)", "(US)", "UK", "AC30", "BDRMM", "AKA", "A.K.A", "JFK", "Q&A", "OCD", "SYML", "AC/DC", "EFG", "UKJFF", "RSC", "OMD", "ADF", "C.L.A.W.", "RBC", "CBSO", "ELO", "CYOA", "DS:UK", "RPO", "RIBA", "AJR", "IAM", "SCO", "12A", "3D", "3-D", "JZ", "KCL", "DG", "MHQT", "WIP", "DVTR", "MLB", "FX", "S-X", "LSA", "LSO", "D.C", "CBD", "WWE", "RRR", "HVOB", "XXII", "XXIII", "XIV", "IV", "VI", "VII", "VIII", "XV", "I", "II", "III", "XII", "(WIP)", "TV", "(UK)", "(USA)", "EP", "LGBTQ", "LGBTQ+", "LGBT", "DC", "RPG", "BTS", "(III)", "IV", "VI", "VII", "VIII", "(PG)", "(PG*)", "(12A)", "[12A]", "[R18]", "[TBC]", "SDFF", "LIFF", "LIFF:", "LITM", "NLTS:", "UKBP", "TOTP", "IQ", "GNL", "RBE", "RCM", "XI", "NW5");
    static final Map<String, String> CASE_SPECIFIC = map();
    static final List<Character> VOWELS = list('a', 'e', 'i', 'o', 'u', 'y');
    static final List<String> COMMON_SHORT = list("eat");

    static {
        CASE_SPECIFIC.put("entombed ad", "Entombed AD");
        CASE_SPECIFIC.put("why don't we", "Why Don't We");
        CASE_SPECIFIC.put("haelos", "Hælos");
        CASE_SPECIFIC.put("cua", "cua");
        CASE_SPECIFIC.put("and the", "and The");
        CASE_SPECIFIC.put(" x the ", " X The ");
        CASE_SPECIFIC.put("mckellen", "McKellen");
        CASE_SPECIFIC.put("and his", "and His");
        CASE_SPECIFIC.put("and he", "and He");
        CASE_SPECIFIC.put("& his", "& His");
        CASE_SPECIFIC.put("us thanksgiving", "US Thanksgiving");
        CASE_SPECIFIC.put("victoria and albert museum", "V&A");
        CASE_SPECIFIC.put("victoria & albert museum", "V&A");
        CASE_SPECIFIC.put("v&a", "V&A");
        CASE_SPECIFIC.put("ry x", "RY X");
    }

    static String uppercaseParts(String title) {
        Util.NList split = Util.split(title, " - ");
        for (int i = 0; i < split.size(); i++) {
            split.set(i, Util.uppercaseFirstLetter(split.get(i)));
        }
        return split.reconstruct();
    }

    static void uppercase(Util.StringMutable clean, Context.LanguageContext langCtx) {
        if (Util.empty(clean)) return;
        String string = PhraseUtil.uppercaseAllWordsExcept(clean.string, langCtx.lowercaseWords());
        List<String> words = Util.split(string, "\\s").underlying;
        if (words.size() > 1) {
            for (int i = 1; i < words.size(); i++) {
                if ("\u2021".equals(words.get(i - 1)) || words.get(i - 1).endsWith("?")) {
                    words.set(i, Util.uppercaseFirstLetter(words.get(i)));
                }
            }
        }
        clean.string = Util.string(words);
    }

    static void camelcase(Util.StringMutable clean, String originalText) {
        if (Util.empty(clean)) return;
        String string = clean.string;
        List<String> originalWords = Util.split(originalText, " ").underlying;
        List<String> uppercaseInMiddleWords = list();
        for (String word : originalWords) {
            if (word.length() < 2) continue;
            if (word.equals(word.toUpperCase())) continue;
            if (word.startsWith("Mc") && word.length() > 5) continue;
            char[] chars = word.toCharArray();
            for (int i = 1; i < chars.length; i++) {
                if (Character.isUpperCase(chars[i])) {
                    uppercaseInMiddleWords.add(word);
                    break;
                }
            }
        }
        Util.NList split = Util.split(string, " ");
        for (int i = 0; i < split.size(); i++) {
            String match = Util.getIgnoreCase(uppercaseInMiddleWords, split.get(i));
            if (match != null) split.underlying.set(i, match);
        }
        clean.string = split.reconstruct();
    }

    static String uppercaseQuoted(String word) {
        if (word.length() > 2 && (Util.startsWith(word, START_QUOTES))) {
            return word.charAt(0) + Character.toString(word.charAt(1)).toUpperCase() + word.substring(2);
        } else {
            return word;
        }
    }

    static String cleanUppercases(String title) {
        if (title == null) return title;
        if (title.toUpperCase().equals(title)) {
            List<String> underlying = Util.splitList(title, "\\s");
            List<String> newlist = list();
            for (String word : underlying) {
                boolean comma = word.endsWith(",");
                if (comma) {
                    word = word.substring(0, word.length() - 1);
                }
                if (word.contains(".") || word.contains("&")) {
                    newlist.add(word);
                } else {
                    word = PhraseUtil.uppercaseFirstLetterComplex(word.toLowerCase());
                    word = uppercaseQuoted(word);
                    newlist.add(word);
                }
            }
            title = Util.string(newlist, " ");
        }
        return title;
    }

    static String cleanForceUppercase(String title, String original) {
        if (title == null) return title;
        title = title.replaceAll("\\u00A0", " ");
        List<String> underlying = Util.splitList(title, "\\s");
        List<String> newlist = list();
        for (String word : underlying) {
            String deCommadWord = (word.endsWith(",") || word.endsWith(":")) ? Util.substringRemoveLast(word) : word;
            deCommadWord = deCommadWord.startsWith("[") ? Util.substringRemoveFirst(deCommadWord) : deCommadWord;
            if (FORCE_UPPERCASES.contains(deCommadWord.toUpperCase())) {
                newlist.add(word.toUpperCase());
            } else if (FORCE_UPPERCASES_CONDITIONAL.contains(deCommadWord.toUpperCase()) && original.contains(deCommadWord.toUpperCase())) {
                newlist.add(word.toUpperCase());
            } else if (word.length() == 2 && word.toUpperCase().charAt(0) == word.toUpperCase().charAt(1)) {
                newlist.add(word.toUpperCase());
            } else if (isAcronym(word)) {
                newlist.add(word.toUpperCase());
            } else {
                newlist.add(word);
            }
        }
        title = Util.string(newlist, " ");
        for (String key : CASE_SPECIFIC.keySet()) {
            title = title.replaceAll("(?i)\\b" + key + "\\b", CASE_SPECIFIC.get(key));
        }
        return title;
    }

    static boolean isAcronym(String word) {
        char[] chars = word.toCharArray();
        if (chars.length == 5) {
            return (chars[1] == '.') && (chars[3] == '.');
        }
        if (chars.length == 3) {
            return (chars[1] == '.');
        }
        if (chars.length < 3) return false;
        if (chars.length % 2 == 1) return false;
        for (int i = 0; i < chars.length; i++) {
            if (i % 2 == 1) {
                if (chars[i] != '.') return false;
            }
        }
        return true;
    }

    static String cleanUppercasesByWord(String title, Context.LanguageContext langCtx) {
        if (title == null) return title;
        List<String> newlist = list();
        for (String word : Util.splitList(title, "\\s")) {
            String wordToAdd = word;
            if (word.endsWith(",")) word = Util.substringRemoveLast(word);
            if (word.length() < 4 && !langCtx.prepositionWords().contains(word.toLowerCase()) && !COMMON_SHORT.contains(word.toLowerCase())) {
                newlist.add(wordToAdd);
            } else if (!word.toUpperCase().equals(word)) {
                newlist.add(wordToAdd);
            } else if (word.substring(0, word.length() - 1).contains(".") || word.contains("&")) {
                newlist.add(wordToAdd);
            } else {
                List<Character> characters = list();
                for (char aChar : word.toLowerCase().toCharArray()) {
                    characters.add(aChar);
                }
                if (word.length() == 4 && !Util.intersection(characters, VOWELS)) {
                    newlist.add(wordToAdd);
                } else {
                    wordToAdd = PhraseUtil.uppercaseFirstLetterComplex(wordToAdd.toLowerCase());
                    newlist.add(wordToAdd);
                }
            }
        }
        return Util.string(newlist, " ");
    }
}