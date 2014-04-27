package edu.stanford.cs276.util;

import java.util.Map;

/**
 * Created by kavinyao on 4/27/14.
 */
public class CounterUtility {
    // for convenience
    private static final Integer ZERO = 0;

    public static <T> void incrementCount(T key, Map<T, Integer> counts) {
        Integer count = counts.get(key);
        int val = count == null ? ZERO : count;
        counts.put(key, val+1);
    }
}
