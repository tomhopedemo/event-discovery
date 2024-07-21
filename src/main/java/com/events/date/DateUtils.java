package com.events.date;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {

    public static Matcher matcher(String regex, String text) {
        return Pattern.compile(regex).matcher(text);
    }

    public static boolean empty(Collection collection) {
        return (collection == null || collection.size() == 0 || (collection.size() == 1 && collection.iterator().next() == null));
    }

    public static <T> T get(List<T> list, int index) {
        if (empty(list)) return null;
        if (index >= list.size()) return null;
        if (index == -1) return null;
        return list.get(index);
    }
}
