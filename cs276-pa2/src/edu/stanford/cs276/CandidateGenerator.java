package edu.stanford.cs276;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
                    ' ',','};

    // Generate all candidates for the target query
    public Set<String> getCandidates(String query) throws Exception {
        Set<String> candidates = new HashSet<String>();
        /*
         * Test edit distance 1 first
         */
        candidates.addAll(edits1(query));
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
            String w = p.getSecond();
            if (w.length() >= 1) {
                deletes.add(p.getFirst() + w.substring(1));
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

    public static void main(String[] args) {
        try {
            CandidateGenerator cg = CandidateGenerator.get();
            Set<String> candidates = cg.getCandidates("abcd");
            for (String cand : candidates) {
                System.out.println(cand);
            }
            System.out.println(candidates.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
