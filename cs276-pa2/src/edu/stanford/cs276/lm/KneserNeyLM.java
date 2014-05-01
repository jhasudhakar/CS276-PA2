package edu.stanford.cs276.lm;

import edu.stanford.cs276.util.MapUtility;

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
    }

    @Override
    public void constructDictionaries(String corpusFilePath) throws Exception {
        super.constructDictionaries(corpusFilePath);

        possiblePrefixCount = new HashMap<String, Integer>();

        totalPairs = 0;
        for (Map<String, Integer> w2s : bigramCounts.values()) {
            totalPairs += w2s.size();

            for (String w2 : w2s.keySet()) {
                MapUtility.incrementCount(w2, possiblePrefixCount);
            }
        }
    }

    private double possiblePrefix(final String w2) {
        Integer totalPrefix = possiblePrefixCount.get(w2);
        return totalPrefix == null ? 1 : totalPrefix;
    }

    @Override
    protected double smoothedUnigramProbability(String w2) {
        return possiblePrefix(w2) / totalPairs;
    }
}
