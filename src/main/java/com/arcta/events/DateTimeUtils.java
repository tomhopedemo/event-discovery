package com.arcta.events;
import java.util.*;
import static com.arcta.events.Util.list;
import static com.arcta.events.Util.set;
class DateTimeUtils {
    static void removeIndexPairs(Util.StringMutable clean, Util.MultiList<Integer,Integer> indexPairsToRemove){ List<Integer> gapCharacters = list();
        List<Integer> filteredIndices = Util.integerList(clean.string.length());
        Set<Integer> indicesToExcludeSet = set();
        for (Util.Multi<Integer, Integer> indexPair : indexPairsToRemove.underlying) {
            for (int i = indexPair.a; i < indexPair.b; i++) { indicesToExcludeSet.add(i);}}
        List<Integer> indicesToExclude = new ArrayList<>(indicesToExcludeSet);
        Collections.sort(indicesToExclude);
        int previous = -2;
        for (Integer integer : indicesToExclude) { filteredIndices.remove(integer);
            if (previous + 1 != integer) gapCharacters.add(integer);
            previous = integer;}
        List<Integer> combinedIndices = list();
        combinedIndices.addAll(filteredIndices);
        combinedIndices.addAll(gapCharacters);
        Collections.sort(combinedIndices);
        Collections.sort(filteredIndices);
        List<Character> characters = list();
        char[] original = clean.string.toCharArray();
        for (Integer index : combinedIndices) {
            if (gapCharacters.contains(index)){ characters.add(' ');
                characters.add('\u2021');
                characters.add(' ');}
            if (filteredIndices.contains(index)) { characters.add(original[index]);}}
        StringBuilder sb = new StringBuilder(characters.size());
        for (Character character : characters) sb.append(character.charValue());
        clean.set(sb.toString());}}