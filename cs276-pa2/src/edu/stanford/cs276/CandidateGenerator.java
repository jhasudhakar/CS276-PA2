package edu.stanford.cs276;

import edu.stanford.cs276.util.Pair;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
        Set<String> candidates = vocabulary.known(edits1(query));
        results.addAll(candidates);
        for (String s : candidates) {
            results.addAll(vocabulary.known(edits1(s)));
        }

        // System.out.println("Number of candidates:" + results.size());
        return results;
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

        Set<String> inserts = new HashSet<String>();
        Set<String> deletes = new HashSet<String>();
        Set<String> replaces = new HashSet<String>();
        HashSet<String> transposes = new HashSet<String>();
        for (Pair<String, String> p : splits) {
            String w1 = p.getFirst();
            String w2 = p.getSecond();
            if (isSingleChar(w1, w2)) {
                continue;
            }
            for (Character c : alphabet) {
                inserts.add(tidy(w1 + c + w2));
            }
            if (w2.length() >= 1) {
                deletes.add(tidy(p.getFirst() + w2.substring(1)));
                for (Character c : alphabet) {
                    replaces.add(tidy(w1 + c + w2.substring(1)));
                }
            }
            if (w2.length() > 1) {
                transposes.add(tidy(w1 + w2.charAt(1) + w2.charAt(0) + w2.substring(2)));
            }
        }

        results.addAll(deletes);
        results.addAll(inserts);
        results.addAll(replaces);
        results.addAll(transposes);

        return results;
    }

    private static boolean isSingleChar(String w1, String w2) {
        return (w1.length() == 0 || w1.charAt(w1.length() - 1) == ' ') && (w2.length() <= 1 || w2.charAt(1) == ' ');
    }

    private static String tidy(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }

    public static void main(String args[]) throws Exception {
        System.out.println(isSingleChar("", "a world"));
        System.out.println(isSingleChar("a world ", ""));
        System.out.println(isSingleChar("a world", " a"));
        System.out.println("Test begins.");
        LanguageModel languageModel;
        languageModel = LanguageModel.load();
        System.out.println("Load finished.");
        CandidateGenerator cg = CandidateGenerator.get();
        // Set<String> candidates = cg.getCandidates("i wantto learndatabase", languageModel);
        Set<String> candidates = cg.getCandidates("page 1 page 2 page", languageModel);
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
