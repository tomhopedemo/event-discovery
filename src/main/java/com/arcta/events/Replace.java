package com.arcta.events;

import java.util.List;

class Replace {
    static String replace(String title) {
        title = properHyphenation(title);
        title = title.replaceAll("\u2021 (?i)present ", "present ").replaceAll("(\\s|\\u00A0)+", " ");
        title = title.replaceAll(" Vol\\. ", " Vol.").replaceAll(" (?i)w/ ", " with ");
        title = title.replaceAll("(?<!w)/ ", " - ").replace("\\u2026", "\u2026");
        title = title.replaceAll("(\\s){2,4}", " - ").replaceAll("&nbsp;", " ");
        title = title.replaceAll("&nbsp", " ").replace("\u2192", " ");
        title = title.replaceAll("\\u00a0", " ").replace(((char) 160), ' ');
        title = title.replaceAll("\u00a0", " ").replaceAll("\u00AD", "");
        title = title.replaceAll("\u27a2", "").replaceAll("\u00AB", " - ");
        title = title.replaceAll("\u25BA", " - ").replaceAll("\u00BB", " - ");
        title = title.replaceAll("\u201c", "\"").replaceAll("\\(\\)", "");
        title = title.replaceAll("\\[\\]", "").replaceAll("\ufffd", "");
        title = title.replaceAll("\u266b", " ").replaceAll("\\-\\s,", "\\-");
        title = title.replaceAll("\u2021\\s,", "\u2021").replaceAll("\u2022", " ");
        title = title.replaceAll("\\s+", " ").replaceAll("\u2021\\s*\u2021", "\u2021");
        title = title.replaceAll("\u2021\\s*\u2021", "\u2021").replaceAll("\u2021", "-");
        title = title.replaceAll(" - ,", " -").replaceAll(" \u00B7 ", " - ");
        title = title.replaceAll(" -,", " -").replaceAll(" £-", " -");
        title = title.replaceAll("~", "-").replaceAll("; ", " - ");
        title = title.replaceAll(" , ", " - ").replace("- @ -", "-");
        title = title.replace("\u2010 ", " - ").replace("\u2013 ", " - ");
        title = title.replace("\u2014 ", " - ").replace("\u002d ", " - ");
        title = title.replace("\u2727 ", " - ").replace("\u2726 ", " - ");
        title = title.replace("- ", " - ").replace(" -", " - ");
        title = title.replaceAll("\\s+", " ").replaceAll(" (?i)description ", " - ");
        title = title.replaceFirst("\\s[0-9]{2,3}\\s" + "(?i)mins", "").replaceAll("(?i)class=\".*\"", "");
        title = title.replaceAll(" Vs ", " vs ").replaceAll(" 's ", " ").replaceAll("^'s ", " ");
        String mPunctuation = "(\u2010|\u2013|\u2014|\\-|;|:|\\.|\\|)";
        for (int i = 0; i < 3; i++) {
            title = title.replaceAll(mPunctuation + "\\s*" + mPunctuation, " - ");
            title = Util.trim(title);
        }
        return title;
    }

    static String properHyphenation(String title) {
        Util.NList split = Util.split(title, " ");
        for (int i = 0; i < split.size(); i++) {
            String word = split.get(i);
            if (word.length() > 1 && word.endsWith("-")) {
                List<String> words = Util.list(word.substring(0, word.length() - 1), "-");
                title = split.reconstructReplacing(i, words);
                break;
            }
        }
        return title;
    }

    static void cleanPunctuation(Util.StringMutable clean) {
        if (Util.empty(clean)) return;
        String string = clean.string;
        if (string.startsWith(",")) {
            string = string.replaceAll("^,\\s+", "");
        }
        string = string.replaceAll(" ,", ",").replaceAll(",(\\S)", ", $1").replaceAll(" 's ", " ").replaceAll("^'s ", " ").replaceAll("\\u200b", "");
        clean.set(string);
    }
}