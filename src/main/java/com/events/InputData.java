package com.events;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class InputData {
    String city;
    List<String> references;
    Status status;
    Map<String, String> referenceNotes;
    Map<String, List<String>> referenceUrls;
    Map<String, String> referenceDistrict;
    Map<String, String> referencePreferredName;
    Map<String, String> referenceCategory;

    InputData(String city, BaseDirs dirs) {
        this.city = city;
        if (city == null) return;
        Util.Table cityConfig = new Util.Table(Util.readTable(dirs.getInputDir() + city + ".csv", ","));
        references = cityConfig.createList(0);
        Map<String, String> referenceQualities = cityConfig.createMap(0, 1);
        status = new Status(referenceQualities);
        referenceNotes = cityConfig.createMap(0, 2);
        referenceUrls = mapValue(cityConfig.createMap(0, 3), value -> Util.splitList(value, " "));
        referenceDistrict = mapValue(cityConfig.createMap(0, 4), value -> Util.trim(value));
        referencePreferredName = mapValue(cityConfig.createMap(0, 5), value -> Util.trim(value));
        referenceCategory = mapValue(cityConfig.createMap(0, 6), value -> Util.trim(value));
    }

    Status getStatus() {
        return status;
    }

    String getCity() {
        return this.city;
    }

    List<String> getRefs() {
        return references;
    }

    String getNotes(String ref) {
        return referenceNotes.get(ref);
    }

    String getPreferredName(String ref) {
        return referencePreferredName.get(ref);
    }

    String getCategory(String ref) {
        return referenceCategory.get(ref);
    }

    String getDistrict(String ref) {
        return referenceDistrict.get(ref);
    }

    List<String> getUrls(String ref) {
        return referenceUrls.get(ref);
    }

    static <T> Map<String, T> mapValue(Map<String, String> map, final Function<String, T> valueMappper) {
        return map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> valueMappper.apply(e.getValue())));
    }

    List<String> getLocal() {
        return status.getLocal(references);
    }

    List<String> ABFestivalLocalLong() {
        return status.ABFestivalLocalLong(references);
    }

    List<String> getFestivals() {
        return status.getFestivals(references);
    }

    List<String> getABFestivals(List<String> refs) {
        return status.getABFestivals(refs);
    }

    List<String> getABFestivalLocalLong(List<String> refs) {
        return status.ABFestivalLocalLong(refs);
    }

    List<String> getStatuses(String ref) {
        return status.get(ref);
    }

    List<String> getBx(List<String> references) {
        return status.getBx(references);
    }

    List<String> getAFestivals(List<String> references) {
        return status.getAFestivals(references);
    }

    List<String> getWestEndData() {
        return status.getWestEnd(references);
    }
}