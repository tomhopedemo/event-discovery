package com.arcta.events;

import java.io.Serializable;
import java.util.Objects;

class MultiA<B> implements Serializable, Comparable<MultiA<B>> {
    String a;
    B b;

    MultiA(String a, B b) {
        this.a = a;
        this.b = b;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiA<B> multiA = (MultiA<B>) o;
        return Objects.equals(a, multiA.a);
    }

    public int hashCode() {
        return Objects.hash(a);
    }

    public int compareTo(MultiA<B> o) {
        if (a == null) {
            if (o == null || o.a == null) return 0;
            return -1;
        }
        return a.compareTo(o.a);
    }
}