package de.comparus.opensource.longmap;

import javafx.util.Pair;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Realization of map based on {@link javafx.util.Pair Pair<Long, V>} <br>
 * Main point is to reach max memory efficiency with adequate performance
 *
 * @param <V> type of map value
 */
class LongMapImpl<V> implements LongMap<V> {
    private static final int MAX_CAPACITY = Integer.MAX_VALUE - 8;
    private static final int INITIAL_CAPACITY = 8;

    private double loadFactor = 0.75d;
    private int entriesCount = 0;
    private Pair<Long, V>[] data;

    @SuppressWarnings("unchecked")
    LongMapImpl() {
        data = new Pair[INITIAL_CAPACITY];
    }

    @SuppressWarnings("unchecked")
    LongMapImpl(int capacity) {
        if (capacity <= 0 || capacity > getMaxCapacity()) {
            throw new IllegalArgumentException("Illegal capacity");
        }

        data = new Pair[capacity];
    }

    LongMapImpl(int capacity, double loadFactor) {
        this(capacity);

        if (loadFactor <= 0 || loadFactor > 1) {
            throw new IllegalArgumentException("Illegal load balance");
        }

        this.loadFactor = loadFactor;
    }

    /**
     * Puts value to the map <br>
     * In case of collision entry will be placed to nearest free map index in order to
     * reduce memory usage comparing with buckets containing collisions list
     *
     * @param key   entry key
     * @param value entry value
     * @return saved value
     */
    @Override
    public V put(long key, V value) {
        if (entriesCount == getMaxCapacity()) {
            throw new IllegalStateException("Map is full");
        }

        int index = obtainFreeIndex(key);
        data[index] = new Pair<>(key, value);
        entriesCount++;

        checkResize();

        return value;
    }

    private int obtainFreeIndex(long key) {
        int index = (int) key % data.length;

        boolean freeIndex = false;

        while (!freeIndex) {
            if (data[index] != null) {
                index = obtainNextIndex(index);
                continue;
            }

            freeIndex = true;
        }

        return index;
    }

    private void checkResize() {
        if (data.length == getMaxCapacity()) {
            return;
        }

        double currentLoad = (double) entriesCount / data.length;

        if (currentLoad > loadFactor) {
            resize();
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Pair<Long, V>[] entries = data;

        int newSize = data.length * 2 + 1;

        if (newSize > getMaxCapacity()) {
            newSize = getMaxCapacity();
        }

        data = new Pair[newSize];
        entriesCount = 0;

        Arrays.stream(entries)
                .filter(Objects::nonNull)
                .forEach(entry -> put(entry.getKey(), entry.getValue()));
    }

    /**
     * Obtains value of the entry with given key from map
     *
     * @param key key of entry to return
     * @return value of the entry or null if key not presented
     */
    @Override
    public V get(long key) {
        return doActionForEntry(key, this::obtainValue);
    }

    private V obtainValue(int index) {
        if (index < 0) {
            return null;
        }

        return data[index].getValue();
    }

    /**
     * Intended to avoid duplicated code <br>
     * Searches for entry with given key and performs action on it or just return value based on index.
     * Be noticed that if key not presented negative index will be passed to {@link IntFunction}
     *
     * @param key    key of entry to search for
     * @param action function to define what to do and return in case of entry found or not found<br>
     *               Not found behavior based on negative input for {@link IntFunction}
     * @return value defined by {@code action}
     */
    private <T> T doActionForEntry(long key, IntFunction<T> action) {
        int initialIndex = (int) (key % data.length);
        int index = initialIndex;

        if (entryHasKey(index, key)) {
            return action.apply(index);
        }

        index = obtainNextIndex(index);

        while (index != initialIndex) {
            if (entryHasKey(index, key)) {
                return action.apply(index);
            }

            index = obtainNextIndex(index);
        }

        return action.apply(-1);
    }

    private boolean entryHasKey(int index, long key) {
        return data[index] != null && data[index].getKey() == key;
    }

    private int obtainNextIndex(int index) {
        return index == data.length - 1 ? 0 : index + 1;
    }

    /**
     * Remove entry with given key from map
     *
     * @param key key of entry to remove
     * @return value if entry presented or {@code null} if no entry with such key found
     */
    @Override
    public V remove(long key) {
        return doActionForEntry(key, this::removeEntry);
    }

    private V removeEntry(int index) {
        if (index < 0) {
            return null;
        }

        V value = data[index].getValue();
        data[index] = null;

        --entriesCount;

        return value;
    }

    /**
     * Checks if map has no entries
     *
     * @return true if map contains no entries
     */
    @Override
    public boolean isEmpty() {
        return entriesCount == 0;
    }

    /**
     * Checks if given key presented in map
     *
     * @param key key to look for
     * @return {@code true} if key presented or {@code false} if it`s not
     */
    @Override
    public boolean containsKey(long key) {
        return doActionForEntry(key, this::isKeyFound);
    }

    private boolean isKeyFound(int index) {
        return index >= 0;
    }

    /**
     * Checks if given value presented in map
     *
     * @param value value to look for
     * @return {@code true} if value presented or {@code false} if it`s not
     */
    @Override
    public boolean containsValue(V value) {
        return Arrays.stream(data)
                .filter(Objects::nonNull)
                .anyMatch(e -> e.getValue().equals(value));
    }

    /**
     * Returns keys of map entries
     *
     * @return keys of filled entries
     */
    @Override
    public long[] keys() {
        if (entriesCount == 0) {
            return new long[0];
        }

        long[] raw = new long[data.length];
        int rawIndex = 0;

        for (Pair<Long, V> entry : data) {
            if (entry == null) {
                continue;
            }

            raw[rawIndex++] = entry.getKey();
        }

        return Arrays.copyOfRange(raw, 0, entriesCount);
    }

    /**
     * Returns values of map entries
     *
     * @return array with values of filled entries
     */
    @SuppressWarnings("unchecked")
    @Override
    public V[] values() {
        if (entriesCount == 0) {
            return (V[]) new Object[0];
        }

        V[] raw = (V[]) new Object[data.length];
        int rawIndex = 0;

        for (Pair<Long, V> entry : data) {
            if (entry == null) {
                continue;
            }

            raw[rawIndex++] = entry.getValue();
        }

        return Arrays.copyOfRange(raw, 0, entriesCount);
    }

    /**
     * Returns count of entries in map
     *
     * @return count of filled map entries
     */
    @Override
    public long size() {
        return entriesCount;
    }

    /**
     * Clears map entries
     */
    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        data = new Pair[INITIAL_CAPACITY];
        entriesCount = 0;
    }

    /**
     * Intended for testing purposes
     *
     * @return max capacity of map
     */
    protected int getMaxCapacity() {
        return MAX_CAPACITY;
    }
}
