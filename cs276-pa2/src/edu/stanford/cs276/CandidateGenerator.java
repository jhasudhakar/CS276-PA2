package edu.stanford.cs276;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.javafx.geom.AreaOp;
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
                    ' ',',','-'};

    // Generate all candidates for the target query
    public Set<String> getCandidates(String query, Vocabulary vocabulary) throws Exception {
        Set<String> candidates = new HashSet<String>();
        // If query is a valid word in the dictionary, simply returns.
        if (vocabulary.exists(query)) {
            candidates.add(query);
            return candidates;
        }
        // If there are candidates in edits1(query), return.
        Set<String> edit1Candidates = vocabulary.known(edits1(query));
        if (!edit1Candidates.isEmpty()) {
            return edit1Candidates;
        }
        // If there are candidates in edits2(query), return.
        Set<String> edit2Candidates = vocabulary.known(edits2(query));
        System.out.println(edit2Candidates.size());
        if (!edit2Candidates.isEmpty()) {
            return edit2Candidates;
        }
        // If there are no candidates found, simply return.
        candidates.add(query);
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
        Set<String> candidates = cg.getCandidates("spellig", languageModel);
        for (String s : candidates) {
            System.out.println(s);
        }
        System.out.println(candidates.size());

        candidates = cg.getCandidates("somhing", languageModel);
        for (String s : candidates) {
            System.out.println(s);
        }
        System.out.println(candidates.size());

        candidates = cg.getCandidates("acres", languageModel);
        for (String s : candidates) {
            System.out.println(s);
        }
        System.out.println(candidates.size());
    }
}
