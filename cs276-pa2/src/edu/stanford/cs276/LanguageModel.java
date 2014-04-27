package edu.stanford.cs276;

import edu.stanford.cs276.util.CounterUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LanguageModel implements Vocabulary, Serializable {
    // the singleton instance
    private static LanguageModel lm_;
    // for bigram probability interpolation
    private static double LAMBDA = 0.1;

    // the number of terms in the training corpus
    private double totalTokens;
    // w -> probability
    private Map<String, Integer> unigramCounts;
    // <w1, w2> -> probability
    private Map<String, Map<String, Integer>> bigramCounts;

    // Do not call constructor directly since this is a Singleton
    private LanguageModel(String corpusFilePath) throws Exception {
        constructDictionaries(corpusFilePath);
    }

    @Override
    public boolean exists(String word) {
        // if the word exists in vocabulary, it must be a key
        String[] tokens = word.split("\\s+");
        for (String token : tokens) {
            if (!unigramCounts.containsKey(token)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<String> known(Set<String> candidates) {
        Set<String> results = new HashSet<String>();
        for (String s : candidates) {
            if (this.exists(s)) {
                results.add(s);
            }
        }
        return results;
    }

    /**
     * Compute unigram probability of w in the training corpus.
     *
     * @param w
     * @return the probability, 0 if the world doesn't exist in corpus
     */
    public double unigramProbability(String w) {
        return unigramCounts.get(w) / totalTokens;
    }

    /**
     * Compute bigram probability of w2 given w1 in the training corpus.
     *
     * @param w1
     * @param w2
     * @return the probability (with possible smoothing applied)
     */
    public double bigramProbability(String w1, String w2) {
        double w2UnigramProb = unigramProbability(w2);

        double w1TotalCount = unigramCounts.get(w1);
        int w2Count = 0;
        if (bigramCounts.get(w1).containsKey(w2)) {
            w2Count = bigramCounts.get(w1).get(w2);
        }
        double w2BigramProb = w2Count / w1TotalCount;

        return LAMBDA * w2UnigramProb + (1 - LAMBDA) * w2BigramProb;
    }

    /**
     * P(w1, w2, ..., wn) = uP(w1)bP(w2|w1)bP(w3|w2)...bP(wn|wn-1)
     * All words should exist in the training corpus.
     *
     * @param sentence
     * @return
     */
    public double computeProbability(String sentence) {
        String[] tokens = sentence.split("\\s+");
        double prob = Math.log(unigramProbability(tokens[0]));
        for (int i = 1; i < tokens.length; ++i)
            prob += Math.log(bigramProbability(tokens[i-1], tokens[i]));
        return prob;
    }

    public void constructDictionaries(String corpusFilePath)
            throws Exception {
        unigramCounts = new HashMap<String, Integer>();
        bigramCounts = new HashMap<String, Map<String, Integer>>();

        System.out.println("Constructing dictionaries...");
        File dir = new File(corpusFilePath);
        for (File file : dir.listFiles()) {
            // ignore hidden files
            if (file.getName().charAt(0) == '.') {
                continue; // Ignore the self and parent aliases.
            }

            System.out.printf("Reading data file %s ...\n", file.getName());
            BufferedReader input = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = input.readLine()) != null) {
                String[] tokens = line.trim().split("\\s+");
                if (tokens.length == 0) {
                    continue;
                }

                // process first token: only need to update unigramCounts
                CounterUtility.incrementCount(tokens[0], unigramCounts);

                // process the rest tokens
                for (int i = 1; i < tokens.length; ++i) {
                    // increment unigram count
                    CounterUtility.incrementCount(tokens[i], unigramCounts);

                    // increment bigram count
                    Map<String, Integer> counts = bigramCounts.get(tokens[i-1]);
                    if (counts == null) {
                        counts = new HashMap<String, Integer>();
                        bigramCounts.put(tokens[i-1], counts);
                    }
                    CounterUtility.incrementCount(tokens[i], counts);
                }
            }
            input.close();
        }

        // cache total number of terms
        totalTokens = sum(unigramCounts);

        // Note: no need to pre-compute all unigram and bigram probabilities
        //       as we will only use a fraction of them

        System.out.println("Done.");
    }

    private static int sum(Map<String, Integer> unigramCounts) {
        int sum = 0;

        for (int i : unigramCounts.values()) {
            sum += i;
        }

        return sum;
    }

    // Loads the object (and all associated data) from disk
    public static LanguageModel load() throws Exception {
        try {
            if (lm_==null){
                FileInputStream fiA = new FileInputStream(Config.languageModelFile);
                ObjectInputStream oisA = new ObjectInputStream(fiA);
                lm_ = (LanguageModel) oisA.readObject();
            }
        } catch (Exception e){
            throw new Exception("Unable to load language model.  You may have not run build corrector");
        }
        return lm_;
    }

    // Saves the object (and all associated data) to disk
    public void save() throws Exception{
        FileOutputStream saveFile = new FileOutputStream(Config.languageModelFile);
        ObjectOutputStream save = new ObjectOutputStream(saveFile);
        save.writeObject(this);
        save.close();
    }

    // Creates a new lm object from a corpus
    public static LanguageModel create(String corpusFilePath) throws Exception {
        if(lm_ == null ){
            lm_ = new LanguageModel(corpusFilePath);
        }
        return lm_;
    }
}
