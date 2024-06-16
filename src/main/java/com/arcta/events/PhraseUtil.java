package com.arcta.events;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.arcta.events.Util.*;

class PhraseUtil {
    static List<Character> chars = list('"', '(');

    static int num(char character, String word) {
        int count = 0;
        for (char aChar : word.toCharArray()) {
            if (aChar == character) count++;
        }
        return count;
    }

    static String uppercaseFirstLetterComplex(String input) {
        if (empty(input)) return input;
        if (chars.contains(input.charAt(0))) {
            if (input.length() == 1) {
                return input;
            } else {
                return input.charAt(0) + uppercaseFirstLetter(input.substring(1));
            }
        } else {
            return uppercaseFirstLetter(input);
        }
    }

    static String firstWord(String sentence) {
        if (sentence == null) return null;
        return Util.split(sentence, " ").get(0);
    }

    static List<String> phraseIntersection(String name, String name_, int phraseLength, Integer startRestriction) {
        List<String> words_ = Util.split(name_, "\\s+").underlying;
        List<String> words = Util.split(name, "\\s+").underlying;
        words_.removeIf(w -> w.length() < 3);
        words.removeIf(w -> w.length() < 3);
        words_ = Util.sublist(words_, 20);
        words = Util.sublist(words, 20);
        if (words.size() < phraseLength || words_.size() < phraseLength) return null;
        int limit = words.size() - (phraseLength - 1);
        if (startRestriction != null) {
            limit = Math.min(limit, startRestriction);
        }
        for (int i = 0; i < limit; i++) {
            for (Integer index_ : indexesOfAll(words.get(i), Util.sublist(words_, startRestriction == null ? words_.size() : startRestriction))) {
                if (words.get(i + 1).equals(Util.get(words_, index_ + 1))) {
                    if (phraseLength > 2) {
                        if (words.get(i + 2).equals(Util.get(words_, index_ + 2))) {
                            if (phraseLength > 3) {
                                if (words.get(i + 3).equals(Util.get(words_, index_ + 3))) {
                                    if (phraseLength > 4) {
                                        if (words.get(i + 4).equals(Util.get(words_, index_ + 4))) {
                                            if (phraseLength > 5) {
                                                if (words.get(i + 5).equals(Util.get(words_, index_ + 5))) {
                                                    return list(words.get(i), words.get(i + 1), words.get(i + 2), words.get(i + 3), words.get(i + 4), words.get(i + 5));
                                                }
                                            } else {
                                                return list(words.get(i), words.get(i + 1), words.get(i + 2), words.get(i + 3), words.get(i + 4));
                                            }
                                        }
                                    } else {
                                        return list(words.get(i), words.get(i + 1), words.get(i + 2), words.get(i + 3));
                                    }
                                }
                            } else {
                                return list(words.get(i), words.get(i + 1), words.get(i + 2));
                            }
                        }
                    } else {
                        return list(words.get(i), words.get(i + 1));
                    }
                }
            }
        }
        return null;
    }

    static <T> List<Integer> indexesOfAll(T object, List<T> list) {
        List<Integer> to_return = list();
        for (int i = 0; i < list.size(); i++) {
            if (object.equals(list.get(i))) to_return.add(i);
        }
        return to_return;
    }

    static String uppercaseAllWordsExcept(String input, List<String> except) {
        StringBuilder sb = new StringBuilder();
        boolean firstword = true;
        String previousWord = ".";
        List<String> uppercaseIndicators = list(".", ":");
        uppercaseIndicators.addAll(Util.HYPHENS);
        for (String word : uppercaseAllWords(input).split("\\s")) {
            word = word.trim();
            if (firstword) {
                sb.append(word + " ");
                firstword = false;
            } else {
                if (Util.safeNull(except).contains(word.toLowerCase()) && !uppercaseIndicators.contains(previousWord.substring(previousWord.length() - 1))) {
                    sb.append(word.toLowerCase() + " ");
                } else {
                    sb.append(word + " ");
                }
            }
            previousWord = word;
        }
        return sb.substring(0, sb.length() - 1);
    }

    static String subSentence(String sentence, int numberOfWords) {
        MultiList<Integer, String> indexWords = Util.sublist(words(sentence), numberOfWords);
        List<String> words = Util.bList(indexWords);
        return Util.string(words);
    }

    static String stripPunctuation(String string) {
        if (string == null) return null;
        for (String character : list(".", ":", ",")) {
            if (string.startsWith(character)) {
                string = Util.substringRemoveFirst(string);
            }
            if (string.endsWith(character)) {
                string = Util.substringRemoveLast(string);
            }
        }
        return string;
    }

    static String removeWordSafely(String input, String wordToRemove) {
        return removeWordSafely(input, wordToRemove, false);
    }

    static Double averageLengthWords(String title) {
        if (empty(title)) return null;
        return divide(title.length(), Util.bList(words(title)).size());
    }

    static String removeWordSafely(String input, String wordToRemove, boolean caseInsensitive) {
        return replaceWordSafely(input, wordToRemove, caseInsensitive, " ");
    }

    static String replaceWordSafely(String input, String wordToRemove, boolean caseInsensitive, String replacement) {
        if (empty(input) || empty(wordToRemove)) return input;
        String toReturn = input;
        List<String> variations = list("^" + wordToRemove + "$", "^" + wordToRemove + "[\\.|:|,|\\?|\\s]", "[\\s|\\(]" + wordToRemove + "$", "[\\s|\\(]" + wordToRemove + "[\\.|:|,|\\?|\\s]", "[\\s|\\(]" + wordToRemove + "s(?![a-z|A-Z])");
        for (String variation : variations) {
            if (caseInsensitive) variation = "(?i)" + variation;
            toReturn = toReturn.replaceAll(variation, replacement);
        }
        return toReturn;
    }

    static String uppercaseAllWords(String input) {
        StringBuilder sb = new StringBuilder();
        for (String s : input.split("\\s")) {
            String trim = s.trim();
            if (trim.length() > 0) {
                if (s.startsWith("(") && s.length() > 1) {
                    sb.append("(" + s.substring(1, 2).toUpperCase() + (s.length() > 2 ? s.substring(2) : ""));
                } else {
                    sb.append(uppercaseFirstLetter(s));
                }
                sb.append(" ");
            }
        }
        return Util.substringRemoveLast(sb);
    }

    static int num(String sub, String word) {
        Matcher matcher = Util.matcher(Pattern.quote(sub), word);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    static MultiList<Integer, String> words(String sentence) {
        if (sentence == null) return null;
        MultiList<Integer, String> indexWord = new MultiList<>();
        String[] split = sentence.split("\\s|\u00A0");
        int cumulativeLength = 0;
        for (int i = 0; i < split.length; i++) {
            String word = split[i];
            if (empty(word)) {
                cumulativeLength++;
                continue;
            }
            indexWord.add(new Multi<>(cumulativeLength, word));
            cumulativeLength = cumulativeLength + word.length() + 1;
        }
        return indexWord;
    }

    static List<String> wordList(String string, Integer minLength) {
        List<String> strings = Util.bList(words(string));
        if (empty(strings) || minLength == null) return strings;
        strings.removeIf(s -> s.length() < minLength);
        return strings;
    }

    static List<String> wordList(String string) {
        return wordList(string, null);
    }

    static int wordCount(String string) {
        return Util.size(wordList(string));
    }
}