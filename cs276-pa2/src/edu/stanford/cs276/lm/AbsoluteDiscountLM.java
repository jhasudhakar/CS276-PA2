package edu.stanford.cs276.lm;

import edu.stanford.cs276.LanguageModel;

import java.util.Map;

/**
 * Created by kavinyao on 4/29/14.
 */
public class AbsoluteDiscountLM extends LanguageModel {
    private double D;
    
    public AbsoluteDiscountLM(String corpusFilePath) throws Exception {
        super(corpusFilePath);
    }

    @Override
    public void constructDictionaries(String corpusFilePath) throws Exception {
        super.constructDictionaries(corpusFilePath);

        // compute n1 and n2
        int n1 = 0, n2 = 0;
        for (Map<String, Integer> counts : bigramCounts.values()) {
            for (int c : counts.values()) {
                if (c == 1) {
                    ++n1;
                } else if (c == 2) {
                    ++n2;
                }
            }
        }

        // use n1 and n2 to estimate discount D
        D = 1.0 * n1 / (n1 + 2 * n2);

        System.out.println(String.format("n1 = %d, n2 = %d, D = %f", n1, n2, D));
    }

    @Override
    public double bigramProbability(String w1, String w2) {
        // bigram part with absolute discounting
        double prefixCount = 1;
        if (unigramCounts.containsKey(w1)) {
            prefixCount = unigramCounts.get(w1);
        }

        double w2Count = 0, N1Plus = 0;
        if (bigramCounts.containsKey(w1)) {
            Map<String, Integer> prefix = bigramCounts.get(w1);
            N1Plus = prefix.size();
            if (prefix.containsKey(w2)) {
                w2Count = prefix.get(w2);
            }
        }

        double discountedCount = Math.max(w2Count - D, 0);

        double bigramPart = discountedCount / prefixCount;

        // unigram part
        double w2UnigramProb = unigramProbability(w2);
        double unigramPart = D * N1Plus * w2UnigramProb / prefixCount;

        return bigramPart + unigramPart;
    }
}
