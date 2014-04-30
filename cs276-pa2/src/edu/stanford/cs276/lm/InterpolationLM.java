package edu.stanford.cs276.lm;

import edu.stanford.cs276.LanguageModel;

/**
 * Created by kavinyao on 4/29/14.
 */
public class InterpolationLM extends LanguageModel {
    // for bigram probability interpolation
    private static double LAMBDA = 0.1;

    public InterpolationLM(String corpusFilePath) throws Exception {
        super(corpusFilePath);
    }

    @Override
    public double bigramProbability(String w1, String w2) {
        double w2UnigramProb = unigramProbability(w2);

        double w1TotalCount = 1;
        if (unigramCounts.containsKey(w1)) {
            w1TotalCount += unigramCounts.get(w1);
        }
        int w2Count = 0;
        if (bigramCounts.containsKey(w1) && bigramCounts.get(w1).containsKey(w2)) {
            w2Count = bigramCounts.get(w1).get(w2);
        }
        double w2BigramProb = w2Count / w1TotalCount;

        return LAMBDA * w2UnigramProb + (1 - LAMBDA) * w2BigramProb;
    }
}
