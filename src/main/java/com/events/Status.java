package com.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.events.Util.*;

class Status {
    Map<String, String> referenceStatuses;
    static List<String> A = list("A1", "A+", "A-", "A"), AB = list("A1", "A+", "A-", "B", "B-", "A");
    static List<String> Bx = list("B", "B-", "B2", "B3", "B4");
    static List<String> WESTEND = list("WEND"), PROMOTED = list("A+");
    static List<String> PROMOTED1 = list("A1"), MAJOR = list("M");
    static List<String> LOCAL = list("LOCAL", "LOCAL-"), UNTRUSTED = list("A-", "B-", "LOCAL-");
    static List<String> FESTIVAL = list("F2"), FESTIVAL_STALE = list("F", "F1");

    Status(Map<String, String> referenceStatuses) {
        this.referenceStatuses = referenceStatuses;
    }

    List<String> get(String ref) {
        return splitList(referenceStatuses.get(ref), " ");
    }

    List<String> get(Collection<String> refs, List<String> acceptableStatuses) {
        List<String> acceptableRefs = list();
        for (String ref : refs) {
            if (intersection(get(ref), acceptableStatuses)) {
                acceptableRefs.add(ref);
            }
        }
        return acceptableRefs;
    }

    List<String> promoted() {
        return get(referenceStatuses.keySet(), PROMOTED);
    }

    List<String> promoted1() {
        return get(referenceStatuses.keySet(), PROMOTED1);
    }

    List<String> major() {
        return get(referenceStatuses.keySet(), MAJOR);
    }

    List<String> untrusted() {
        return get(referenceStatuses.keySet(), UNTRUSTED);
    }

    List<String> ABFestivalLocalLong(List<String> refs) {
        List<String> statuses = new ArrayList<>(Status.AB);
        statuses.addAll(FESTIVAL);
        statuses.addAll(LOCAL);
        return get(refs, statuses);
    }

    List<String> getLocal(List<String> refs) {
        return get(refs, LOCAL);
    }

    List<String> getABFestivals(List<String> refs) {
        List<String> statuses = new ArrayList<>(Status.AB);
        statuses.addAll(FESTIVAL);
        return get(refs, statuses);
    }

    List<String> getAB(List<String> refs) {
        return get(refs, new ArrayList<>(Status.AB));
    }

    List<String> getAFestivals(List<String> refs) {
        List<String> statuses = new ArrayList<>(A);
        statuses.addAll(FESTIVAL);
        return get(refs, statuses);
    }

    List<String> getFestivals(List<String> refs) {
        List<String> statuses = new ArrayList<>(FESTIVAL);
        return get(refs, statuses);
    }

    List<String> get(List<String> refs, String status) {
        return get(refs, list(status));
    }

    List<String> getBx(List<String> refs) {
        return get(refs, Bx);
    }

    List<String> getFestivalStale(List<String> refs) {
        return get(refs, FESTIVAL_STALE);
    }

    List<String> getWestEnd(List<String> refs) {
        return get(refs, WESTEND);
    }
}