package edu.stanford.cs276.lm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kavinyao on 4/29/14.
 */
public class KneserNeyLM extends AbsoluteDiscountLM {
    // cache
    private Map<String, Integer> possiblePrefixCount;
    private int totalPairs;

    public KneserNeyLM(String corpusFilePath) throws Exception {
        super(corpusFilePath);

        possiblePrefixCount = new HashMap<String, Integer>();
    }

    @Override
    public void constructDictionaries(String corpusFilePath) throws Exception {
        super.constructDictionaries(corpusFilePath);

        totalPairs = 0;
        for (Map<String, Integer> w2s : bigramCounts.values()) {
            totalPairs += w2s.size();
        }
    }

    private double possiblePrefix(final String w2) {
        Integer totalPrefix = possiblePrefixCount.get(w2);
        if (totalPrefix == null) {
            // compute on-the-fly
            int total = 0;
            for (Map<String, Integer> w2s : bigramCounts.values()) {
                if (w2s.containsKey(w2)) {
                    ++total;
                }
            }

            totalPrefix = total;
            possiblePrefixCount.put(w2, totalPrefix);
        }

        return totalPrefix;
    }

    @Override
    protected double smoothedUnigramProbability(String w2) {
        return possiblePrefix(w2) / totalPairs;
    }
}
