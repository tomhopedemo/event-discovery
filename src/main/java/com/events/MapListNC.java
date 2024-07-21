package com.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.events.Util.list;

class MapListNC<K, T> {
    Map<K, ArrayList<T>> mapList;

    MapListNC() {
        this.mapList = new HashMap<>();
    }

    void put(K key, T listElement) {
        mapList.putIfAbsent(key, list());
        mapList.get(key).add(listElement);
    }
}