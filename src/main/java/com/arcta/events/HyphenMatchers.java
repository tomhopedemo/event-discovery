package com.arcta.events;

import java.util.ArrayList;

class HyphenMatchers {
    static final String M_HYPHENS;     //no spaces already grouped
    static final String M_HYPHENSO;     //no spaces already grouped
    static final String M_HYPHENS_UNDERSCORES_DOTS;     //no spaces already grouped
    static final String M_HYPHENS_TO;     //no spaces already grouped
    static final String M_HYPHENS_TO_UNTIL;     //no spaces already grouped

    static {
        StringBuilder sbHyphen = new StringBuilder();
        StringBuilder sbHyphUnder = new StringBuilder();
        StringBuilder sbHyphTo = new StringBuilder();
        StringBuilder sbHyphToUntil = new StringBuilder();
        for (String hyphen : Util.HYPHENS) {
            sbHyphen.append(hyphen + "|");
        }
        ArrayList<String> hyphensAndUnderscoresAndDots = new ArrayList<>(Util.HYPHENS);
        hyphensAndUnderscoresAndDots.add("_");
        hyphensAndUnderscoresAndDots.add("\\.");
        for (String symbol : hyphensAndUnderscoresAndDots) {
            sbHyphUnder.append(symbol + "|");
        }
        ArrayList<String> hyphensAndTo = new ArrayList<>(Util.HYPHENS);
        hyphensAndTo.add("to");
        for (String symbol : hyphensAndTo) {
            sbHyphTo.append(symbol + "|");
        }
        ArrayList<String> hyphensAndToUntil = new ArrayList<>(hyphensAndTo);
        hyphensAndToUntil.add("until");
        hyphensAndToUntil.add("till");
        hyphensAndToUntil.add("\u00bb");
        for (String symbol : hyphensAndToUntil) {
            sbHyphToUntil.append(symbol + "|");
        }
        M_HYPHENS = "(" + sbHyphen.substring(0, sbHyphen.length() - 1) + ")";
        M_HYPHENSO = M_HYPHENS + "?";
        M_HYPHENS_TO = "(" + sbHyphTo.substring(0, sbHyphTo.length() - 1) + ")";
        M_HYPHENS_TO_UNTIL = "(" + sbHyphToUntil.substring(0, sbHyphToUntil.length() - 1) + ")";
        M_HYPHENS_UNDERSCORES_DOTS = "(" + sbHyphUnder.substring(0, sbHyphUnder.length() - 1) + ")";
    }
}