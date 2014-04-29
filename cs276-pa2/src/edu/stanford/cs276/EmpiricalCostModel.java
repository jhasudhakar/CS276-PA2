package edu.stanford.cs276;

import edu.stanford.cs276.util.MapUtility;
import edu.stanford.cs276.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EmpiricalCostModel implements EditCostModel{

    // the alphabet, use Set to fast lookup
    private Set<Character> alphabet;

    // all characters not present in the pre-defined alphabet
    // will be mapped to this character
    private char unknownCharacter;

    // counts of $c_i$ and ${c_i}{c_i+1}$ in correct queries
    // here unigram and bigram are in terms of characters
    private Map<Character, Integer> unigramCounts;
    private Map<String, Integer> bigramCounts;

    // counts of different edits
    private Map<Pair<Character, Character>, Integer> insertionCounts;
    private Map<Pair<Character, Character>, Integer> deletionCounts;
    private Map<Pair<Character, Character>, Integer> substitutionCounts;
    private Map<Pair<Character, Character>, Integer> transpositionCounts;

    public EmpiricalCostModel(String editsFile) throws IOException {
        loadAlphabet();

        // initialize counts
        unigramCounts = new HashMap<Character, Integer>();
        bigramCounts = new HashMap<String, Integer>();
        insertionCounts = new HashMap<Pair<Character, Character>, Integer>();
        deletionCounts = new HashMap<Pair<Character, Character>, Integer>();
        substitutionCounts = new HashMap<Pair<Character, Character>, Integer>();
        transpositionCounts = new HashMap<Pair<Character, Character>, Integer>();

        BufferedReader input = new BufferedReader(new FileReader(editsFile));
        System.out.println("Constructing edit distance map...");

        String line = null;
        while ((line = input.readLine()) != null) {
            // use .split instead of Scanner for better performance
            String[] parts = line.split("\t");
            String noisy = parts[0];
            String clean = parts[1];

            updateCharacterCounts(clean);

            // determine edit type
            Edit ed = EditDistance.determineOneEdit(clean, noisy);

            if (ed == null) {
                // wow, they're the same!
                continue;
            }

            if (ed.type == EditType.DELETION) {
                incrementEditCount(ed.x, ed.y, deletionCounts);
            } else if (ed.type == EditType.INSERTION) {
                incrementEditCount(ed.x, ed.y, insertionCounts);
            } else if (ed.type == EditType.SUBSTITUTION) {
                incrementEditCount(ed.x, ed.y, substitutionCounts);
            } else if (ed.type == EditType.TRANSPOSITION) {
                incrementEditCount(ed.x, ed.y, transpositionCounts);
            }
        }

        input.close();
        System.out.println("Done.");
    }

    private void updateCharacterCounts(final String word) {
        // use StringBuilder for better performance
        // @@ is for providing the initial 2 character space
        StringBuilder bigram = new StringBuilder("@@");

        // handle beginning character
        MapUtility.incrementCount(EditDistance.BEGIN_CHAR, unigramCounts);

        char prevChar = EditDistance.BEGIN_CHAR;
        for (int i = 0; i < word.length(); ++i) {
            char currChar = characterClass(word.charAt(i));
            MapUtility.incrementCount(currChar, unigramCounts);

            bigram.setCharAt(0, prevChar);
            bigram.setCharAt(1, currChar);
            MapUtility.incrementCount(bigram.toString(), bigramCounts);

            prevChar = currChar;
        }
    }

    private void incrementEditCount(char x, char y, Map<Pair<Character, Character>, Integer> counts) {
        // transform x, y to proper class
        char xClass = characterClass(x);
        char yClass = characterClass(y);

        // increment corresponding count
        Pair<Character, Character> p = new Pair<Character, Character>(xClass, yClass);
        MapUtility.incrementCount(p, counts);
    }

    private void loadAlphabet() {
        alphabet = new HashSet<Character>();
        for (Character c : CandidateGenerator.alphabet) {
            alphabet.add(c);
        }

        // add begin of sentence meta character
        alphabet.add(EditDistance.BEGIN_CHAR);

        final String unknownCharCandidates = "~!@#$%^&*";
        int i = 0;
        for (; i < unknownCharCandidates.length(); ++i) {
            char c = unknownCharCandidates.charAt(i);
            if (!alphabet.contains(c)) {
                unknownCharacter = c;
                break;
            }
        }

        if (i == unknownCharCandidates.length()) {
            System.err.println("Warning: no proper unknown is found.");
        }
    }

    /**
     * For character in alphabet, the character is returned unchanged.
     * Otherwise, unknownCharacter is returned.
     * @param c
     * @return
     */
    private char characterClass(char c) {
        if (alphabet.contains(c)) {
            return c;
        }

        return unknownCharacter;
    }

    @Override
    public double editProbability(String Q, String R, int distance) {
        if (distance > 1) {
            throw new RuntimeException("Not supported distance > 1 (passed in value = " + distance + ")");
        }

        if (Q.equals(R)) {
            // no edit between them
            return Math.log(0.9);
        }

        Edit edit = EditDistance.determineOneEdit(Q, R);
        char x = characterClass(edit.x);
        char y = characterClass(edit.y);
        Pair<Character, Character> p = new Pair<Character, Character>(x, y);

        StringBuilder bigram = new StringBuilder("@@");
        bigram.setCharAt(0, x);
        bigram.setCharAt(1, y);

        double count = 1;
        double total = 2;

        if (edit.type == EditType.DELETION) {
            count = MapUtility.getWithFallback(deletionCounts, p, 0);
            total = bigramCounts.get(bigram.toString());
        } else if (edit.type == EditType.INSERTION) {
            count = MapUtility.getWithFallback(insertionCounts, p, 0);
            total = unigramCounts.get(x);
        } else if (edit.type == EditType.SUBSTITUTION) {
            count = MapUtility.getWithFallback(substitutionCounts, p, 0);
            total = unigramCounts.get(x);
        } else if (edit.type == EditType.TRANSPOSITION) {
            count = MapUtility.getWithFallback(transpositionCounts, p, 0);
            total = bigramCounts.get(bigram.toString());
        }

        return Math.log(smooth(count, total));
    }

    // apply Laplace smoothing
    private double smooth(double count, double total) {
        return (count + 1) / (total + alphabet.size());
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        EmpiricalCostModel model = new EmpiricalCostModel(args[0]);
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("RUNNING TIME: " + totalTime + " ms. ");
    }
}
