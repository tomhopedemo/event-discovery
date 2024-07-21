package com.events;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.events.FiltersLists.REPEAT_PHRASES_ALLOWED;
import static com.events.Util.map;
import static com.events.Util.sout;

class Replica {
    static void cleanReplicaWords(Util.StringMutable clean, String url, Context.LanguageContext langCtx) {
        if (Util.empty(clean)) return;
        String replaced = clean.string.replaceAll("\u2021", " ");
        String urlend = "";
        if (url != null) {
            urlend = Util.last(Util.splitList(url, "/"));
            urlend = urlend.replaceAll("-", " ");
        }
        List<Integer> indices = replicaPhraseIndices(replaced, 2, langCtx);
        indices.addAll(replicaPhraseIndices(replaced, 3, langCtx));
        indices.addAll(replicaPhraseIndices(replaced, 4, langCtx));
        String original = clean.string;
        clean.string = Util.removeIndices(clean.string, indices);
        cleanRepeatWord(clean);
        reapplyUrlTriplet(clean, urlend, original);
    }

    static void reapplyUrlTriplet(Util.StringMutable clean, String urlend, String original) {
        List<String> strings = PhraseUtil.wordList(urlend);
        if (Util.empty(strings)) return;
        String x = strings.get(0);
        int count = 0;
        boolean found = false;
        for (int i = 0; i < strings.size(); i++) {
            if (strings.get(i).equals(x)) {
                count++;
            } else {
                x = strings.get(i);
                count = 0;
            }
            if (count == 3) {
                found = true;
                break;
            }
        }
        String triplet = x + " " + x + " " + x;
        if (found && original.contains(triplet)) {
            clean.string = clean.string.replaceFirst(Pattern.quote(x), triplet);
            sout("TRIPLETXXX: " + clean.string);
        }
    }

    static List<Integer> replicaPhraseIndices(String string, int phraseLength, Context.LanguageContext langCtx) {
        List<Integer> indexesToRemove = Util.list();
        Util.MultiList<Integer, String> words = PhraseUtil.words(string);
        if (words.size() < phraseLength) return indexesToRemove;
        Map<String, Util.Multi<Integer, Integer>> values = map();
        for (int i = 0; i < words.size() - (phraseLength - 1); i++) {
            Util.Multi<Integer, String> previousWord = (i == 0) ? null : words.get(i - 1);
            Util.MultiList<Integer, String> list = new Util.MultiList<>();
            for (int j = 0; j < phraseLength; j++) {
                list.add(words.get(i + j));
            }
            String phrase = Util.string(list.getBList(), " ");
            Integer startInclusive = list.get(0).a;
            int endInclusive = list.get(phraseLength - 1).a + list.get(phraseLength - 1).b.length() - 1;
            if (values.containsKey(phrase)) {
                if ("of".equals(Util.safeB(previousWord))) startInclusive = previousWord.a;
                indexesToRemove.addAll(Util.between(startInclusive, endInclusive));
            } else {
                if (langCtx.prepositionWords().containsAll(list.getBList())) continue;
                values.put(phrase, new Util.Multi<>(startInclusive, endInclusive));
            }
        }
        return indexesToRemove;
    }

    static void cleanRepeatWord(Util.StringMutable clean) {
        List<Integer> indicesToExclude = Util.list();
        Util.NList split = Util.split(clean.string, "\\s+");
        if (split.size() < 2) return;
        for (int i = 0; i < split.size() - 1; i++) {
            String a = PhraseUtil.stripPunctuation(split.get(i));
            String b = PhraseUtil.stripPunctuation(split.get(i + 1));
            if (a.equals(b)) {
                if (Util.containsIgnoreCase(a, REPEAT_PHRASES_ALLOWED)) continue;
                indicesToExclude.add(i);
            }
        }
        clean.string = split.reconstructExcept(indicesToExclude);
    }
}