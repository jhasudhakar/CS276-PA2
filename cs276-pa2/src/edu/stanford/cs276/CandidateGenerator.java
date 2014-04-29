package edu.stanford.cs276;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import edu.stanford.cs276.util.Pair;

public class CandidateGenerator implements Serializable {


    private static CandidateGenerator cg_;

    // Don't use the constructor since this is a Singleton instance
    private CandidateGenerator() {}

    public static CandidateGenerator get() throws Exception{
        if (cg_ == null ){
            cg_ = new CandidateGenerator();
        }
        return cg_;
    }


    public static final Character[] alphabet = {
                    'a','b','c','d','e','f','g','h','i','j','k','l','m','n',
                    'o','p','q','r','s','t','u','v','w','x','y','z',
                    '0','1','2','3','4','5','6','7','8','9',
                    ' ',',','-','\''};

    // Generate all candidates for the target query
    public Set<String> getCandidates(String query, Vocabulary vocabulary) throws Exception {
        Set<String> results = new HashSet<String>();
        String[] tokens = query.split("\\s+");

        // Single word
        // System.out.println("Single word.");
        for (int i = 0; i < tokens.length; i++) {
            // Generate candidates
            Set<String> candidates = getCandidatesForToken(tokens[i], vocabulary, 2);
            // For each candidate, add token[0:i] + candidate + token[i+1:] to results
            StringBuilder s1 = new StringBuilder();
            for (int j = 0; j < i; j++) {
                s1.append(tokens[j] + " ");
            }
            StringBuilder s2 = new StringBuilder();
            for (int j = i + 1; j < tokens.length; j++) {
                s2.append(" " + tokens[j]);
            }
            // If token[0:i] and token[i+1:] are not valid, continue
            if (!vocabulary.exists(s1.toString() + s2.toString())) {
                continue;
            }
            for (String c : candidates) {
                results.add(s1.toString() + c.trim() + s2.toString());
            }
        }

        // Combine two words at first
        // System.out.println("Combine two words at first.");
        for (int i = 0; i < tokens.length - 1; i++) {
            String token = tokens[i] + tokens[i + 1];
            Set<String> candidates = getCandidatesForToken(token, vocabulary, 1);
            // For each candidate, add token[0:i] + candidate + token[i+2:] to results
            StringBuilder s1 = new StringBuilder();
            for  (int j = 0; j < i; j++) {
                s1.append(tokens[j] + " ");
            }
            StringBuilder s2 = new StringBuilder();
            for (int j = i + 2; j < tokens.length; j++) {
                s2.append(" " + tokens[j]);
            }
            // If token[0:i] and token[i+1:] are not valid, continue
            if (!vocabulary.exists(s1.toString() + s2.toString())) {
                continue;
            }
            for (String c : candidates) {
                results.add(s1.toString() + c.trim() + s2.toString());
            }
        }

        // System.out.println("Number of candidates:" + results.size());
        return results;
    }

    /**
     * Get candidates for a single word.
     * @param token
     * @param vocabulary
     * @return
     */
    private static Set<String> getCandidatesForToken(String token, Vocabulary vocabulary, int distance) {
        Set<String> candidates = new HashSet<String>();
        // If the vocabulary exists in the dictionary, add it to our candidates set.
        if (vocabulary.exists(token)) {
            candidates.add(token);
        }
        // Add tokens that are within edit distance 1.
        candidates.addAll(vocabulary.known(edits1(token)));
        if (!candidates.isEmpty()) {
            return candidates;
        }
        // Add tokens that are within edit distance 2.
        if (candidates.isEmpty() || distance == 2) {
            candidates.addAll(vocabulary.known(edits2(token)));
        }
        // If there are no candidates found, simply return.
        if (candidates.isEmpty()) {
            System.out.println("No candidate found.");
            candidates.add(token);
        }
        return candidates;
    }

    /**
     * Return all candidates whose edit distance are 1.
     * @param word
     * @return
     */
    private static Set<String> edits1(String word) {
        Set<String> results = new HashSet<String>();

        Set<Pair<String, String>> splits = new HashSet<Pair<String, String>>();
        int length = word.length();
        for (int i = 0; i <= length; i++) {
            splits.add(new Pair(word.substring(0, i), word.substring(i)));
        }

        Set<String> deletes = new HashSet<String>();
        for (Pair<String, String> p : splits) {
            String w1 = p.getFirst();
            String w2 = p.getSecond();

            if (w1.length() == 0 && w2.length() == 1)
                continue;

            if (w2.length() >= 1) {
                deletes.add(w1 + w2.substring(1));
            }
        }

        Set<String> inserts = new HashSet<String>();
        for (Pair<String, String> p : splits) {
            String w1 = p.getFirst();
            String w2 = p.getSecond();
            for (Character c : alphabet) {
                inserts.add(w1 + c + w2);
            }
        }

        Set<String> replaces = new HashSet<String>();
        for (Pair<String, String> p : splits) {
            String w1 = p.getFirst();
            String w2 = p.getSecond();
            if (w2.length() >= 1) {
                for (Character c : alphabet) {
                    replaces.add(w1 + c + w2.substring(1));
                }
            }
        }

        HashSet<String> transposes = new HashSet<String>();
        for (Pair<String, String> p : splits) {
            String w1 = p.getFirst();
            String w2 = p.getSecond();
            if (w2.length() > 1) {
                transposes.add(w1 + w2.charAt(1) + w2.charAt(0) + w2.substring(2));
            }
        }

        results.addAll(deletes);
        results.addAll(inserts);
        results.addAll(replaces);
        results.addAll(transposes);

        return results;
    }

    /**
     * Return all candidates whose edit distance are 2.
     * @param word
     * @return
     */
    private static Set<String> edits2(String word) {
        Set<String> candidates = edits1(word);
        Set<String> results = new HashSet<String>();
        for (String s : candidates) {
            results.addAll(edits1(s));
        }
        return results;
    }

    public static void main(String args[]) throws Exception {
        System.out.println("Test begins.");
        LanguageModel languageModel;
        languageModel = LanguageModel.load();
        System.out.println("Load finished.");
        CandidateGenerator cg = CandidateGenerator.get();
        Set<String> candidates = cg.getCandidates("i wantto learndatabase", languageModel);
        for (String s : candidates) {
            System.out.println(s);
        }
        System.out.println(candidates.size());

        candidates = cg.getCandidates("i want to eat some ing", languageModel);
        for (String s : candidates) {
            System.out.println(s);
        }
        System.out.println(candidates.size());

        candidates = cg.getCandidates("beautifuloup", languageModel);
        for (String s : candidates) {
            System.out.println(s);
        }
        System.out.println(candidates.size());
    }
}
