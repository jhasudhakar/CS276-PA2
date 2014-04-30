package edu.stanford.cs276;

import edu.stanford.cs276.lm.InterpolationLM;
import edu.stanford.cs276.util.MapUtility;

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

public abstract class LanguageModel implements Vocabulary, Serializable {
    // the number of terms in the training corpus
    protected double totalTokens;
    // total number of terms
    protected double totalTerms;
    // w -> probability
    protected Map<String, Integer> unigramCounts;
    // <w1, w2> -> probability
    protected Map<String, Map<String, Integer>> bigramCounts;

    // Do not call constructor directly since this is a Singleton
    protected LanguageModel(String corpusFilePath) throws Exception {
        constructDictionaries(corpusFilePath);
    }

    public static LanguageModel create(final String smoothing, final String corpusFilePath) throws Exception {
        if (smoothing.equals("interpolation")) {
            return new InterpolationLM(corpusFilePath);
        }

        return null;
    }

    @Override
    public boolean exists(String word) {
        // if the word exists in vocabulary, it must be a key
        String[] tokens = word.trim().split("\\s+");
        if (tokens.length == 0) {
            return false;
        }
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
        int count = 0;
        if (unigramCounts.containsKey(w)) {
            count = unigramCounts.get(w);
        }
        return (count + 1) / (totalTokens + totalTerms); // Apply add-one smoothing
    }

    /**
     * Compute bigram probability of w2 given w1 in the training corpus.
     *
     * @param w1
     * @param w2
     * @return the probability (with possible smoothing applied)
     */
    public abstract double bigramProbability(String w1, String w2);

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
            prob += Math.log(bigramProbability(tokens[i - 1], tokens[i]));
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
                MapUtility.incrementCount(tokens[0], unigramCounts);

                // process the rest tokens
                for (int i = 1; i < tokens.length; ++i) {
                    // increment unigram count
                    MapUtility.incrementCount(tokens[i], unigramCounts);

                    // increment bigram count
                    Map<String, Integer> counts = bigramCounts.get(tokens[i - 1]);
                    if (counts == null) {
                        counts = new HashMap<String, Integer>();
                        bigramCounts.put(tokens[i - 1], counts);
                    }
                    MapUtility.incrementCount(tokens[i], counts);
                }
            }
            input.close();
        }

        // cache total number of terms
        totalTokens = sum(unigramCounts);
        totalTerms = unigramCounts.keySet().size() + 1; // Account for unknown token

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
        LanguageModel lm_ = null;

        try {
            FileInputStream fiA = new FileInputStream(Config.languageModelFile);
            ObjectInputStream oisA = new ObjectInputStream(fiA);
            lm_ = (LanguageModel) oisA.readObject();
        } catch (Exception e) {
            throw new Exception("Unable to load language model.  You may have not run build corrector");
        }

        return lm_;
    }

    // Saves the object (and all associated data) to disk
    public void save() throws Exception {
        FileOutputStream saveFile = new FileOutputStream(Config.languageModelFile);
        ObjectOutputStream save = new ObjectOutputStream(saveFile);
        save.writeObject(this);
        save.close();
    }
}
