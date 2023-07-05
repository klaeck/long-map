package de.comparus.opensource.longmap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LongMapImplTest {

    LongMapImpl<String> map;

    @Before
    public void setUp() {
        map = new LongMapImpl<>();

        for (int i = 0; i < 10; i++) {
            map.put(i, createValue(i));
        }
    }

    private static String createValue(int i) {
        return "Test" + i;
    }

    @After
    public void tearDown() {
        map = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCapacityConstructor() {
        map = new LongMapImpl<>(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOversizeCapacityConstructor() {
        map = new LongMapImpl<>(Integer.MAX_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeLoadFactor() {
        int someCapacity = 20;
        map = new LongMapImpl<>(someCapacity, -0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOversizeLoadFactor() {
        int someCapacity = 20;
        map = new LongMapImpl<>(someCapacity, 1.5);
    }

    @Test
    public void testPutOne() {
        int key = 1;
        String value = "TestValue";
        map = new LongMapImpl<>(2); //in order to avoid map resizing

        String savedValue = map.put(key, value);

        assertEquals(1, map.size());
        assertEquals(value, savedValue);
    }

    @Test(expected = IllegalStateException.class)
    public void testPutOverwhelmed() {
        TestLongMapImpl<String> testMap = TestLongMapImpl.createFullFilledTestMap();

        long key = 1;
        String value = "Could not be saved";
        testMap.put(key, value);
    }

    /**
     * Intended for {@link #testPutOverwhelmed()} <br>
     * Overwrites {@code MAX_CAPACITY} value of original map
     */
    private static class TestLongMapImpl<V> extends LongMapImpl<V> {
        private static final int MAX_CAPACITY = 20;

        @Override
        protected int getMaxCapacity() {
            return MAX_CAPACITY;
        }

        private static TestLongMapImpl<String> createFullFilledTestMap() {
            TestLongMapImpl<String> testMap = new TestLongMapImpl<>();

            for (int i = 0; i < MAX_CAPACITY; i++) {
                testMap.put(i, createValue(i));
            }

            return testMap;
        }
    }

    @Test
    public void testGetExisting() {
        for (int i = 0; i < 10; i++) {
            assertEquals(createValue(i), map.get(i));
        }
    }

    @Test
    public void testGetNotExisting() {
        int notExistingKey = 111;

        assertNull(map.get(notExistingKey));
    }

    @Test
    public void testRemoveExisting() {
        long initialSize = map.size();
        long key = 111;
        String value = "Value to remove";
        map.put(key, value);

        String removedValue = map.remove(key);

        assertEquals(removedValue, value);
        assertEquals(initialSize, map.size());
        assertNull(map.get(key));
    }

    @Test
    public void testRemoveNotExisting() {
        long notExistingKey = 111;

        assertNull(map.remove(notExistingKey));
    }

    @Test
    public void testIsEmpty() {
        assertFalse(map.isEmpty());

        map = new LongMapImpl<>();

        assertTrue(map.isEmpty());
    }

    @Test
    public void testContainsKey() {
        for (int i = 0; i < 10; i++) {
            assertTrue(map.containsKey(i));
        }

        int notExistingKey = 111;
        assertFalse(map.containsKey(notExistingKey));
    }

    @Test
    public void testContainsValue() {
        for (int i = 0; i < 10; i++) {
            String value = createValue(i);
            assertTrue(map.containsValue(value));
        }

        String notExistingValue = "Not existing value";
        assertFalse(map.containsValue(notExistingValue));
    }

    @Test
    public void testKeysWithFilledMap() {
        long[] expectedKeys = new long[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        assertArrayEquals(expectedKeys, map.keys());
    }

    @Test
    public void testKeysWithEmptyMap() {
        long[] expectedKeys = new long[0];

        map = new LongMapImpl<>();

        assertArrayEquals(expectedKeys, map.keys());
    }

    @Test
    public void testValuesWithFilledMap() {
        String[] expectedValues = new String[10];

        for (int i = 0; i < 10; i++) {
            expectedValues[i] = createValue(i);
        }

        assertArrayEquals(expectedValues, map.values());
    }

    @Test
    public void testValuesWithEmptyMap() {
        String[] expectedValues = new String[0];

        map = new LongMapImpl<>();

        assertArrayEquals(expectedValues, map.values());
    }

    @Test
    public void size() {
        int expectedSize = 10;

        assertEquals(expectedSize, map.size());
    }

    @Test
    public void clear() {
        map.clear();

        assertEquals(0, map.size());
    }
}