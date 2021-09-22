package fr.alex6.discord.takobonker;

import java.io.Serializable;
import java.util.Objects;

public class Pair<K,V> implements Serializable {
    private K key;

    public K getKey() { return key; }

    public void setKey(K key) { this.key = key; }

    private V value;

    public V getValue() { return value; }

    public void setValue(V value) { this.value = value; }

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (key != null ? key.hashCode() : 0);
        hash = 31 * hash + (value != null ? value.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return key.equals(pair.key) && Objects.equals(value, pair.value);
    }
}