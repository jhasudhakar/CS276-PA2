package edu.stanford.cs276;

import edu.stanford.cs276.util.CounterUtility;
import edu.stanford.cs276.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EmpiricalCostModel implements EditCostModel{
    // meta character representing the beginning of a sentence
    private static char BEGIN_CHAR = '$';

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
            Edit ed = determineEdit(clean, noisy);

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
        CounterUtility.incrementCount(BEGIN_CHAR, unigramCounts);

        char prevChar = BEGIN_CHAR;
        for (int i = 0; i < word.length(); ++i) {
            char currChar = characterClass(word.charAt(i));
            CounterUtility.incrementCount(currChar, unigramCounts);

            bigram.setCharAt(0, prevChar);
            bigram.setCharAt(1, currChar);
            CounterUtility.incrementCount(bigram.toString(), bigramCounts);

            prevChar = currChar;
        }
    }

    /**
     * Return the edit type and involved characters.
     * Assume at most 1 edit between clean and noisy.
     *
     * @param clean the correct value
     * @param noisy the corrupted value
     * @return null if no edit, or an Edit instance
     */
    private Edit determineEdit(String clean, String noisy) {
        if (clean.equals(noisy)) {
            // the same
            return null;
        }

        // find first difference
        int idx = 0;
        for (; idx < noisy.length() && idx < clean.length(); ++idx) {
            if (noisy.charAt(idx) != clean.charAt(idx)) {
                break;
            }
        }

        // handle two special cases
        if (idx == noisy.length()) {
//                System.out.println("DELETION: ");
//                System.out.println("    C: " + clean);
//                System.out.println("    N: " + noisy + "*");
//                System.out.println("       " + clean.substring(idx-1) + " --> " + clean.substring(idx));

            return new Edit(EditType.DELETION, clean.charAt(idx-1), clean.charAt(idx));
        }

        if (idx == clean.length()) {
//                System.out.println("INSERTION: ");
//                System.out.println("    C: " + clean + "*");
//                System.out.println("    N: " + noisy);
//                System.out.println("       " + clean.substring(idx-1) + " --> " + noisy.substring(idx-1));

            return new Edit(EditType.INSERTION, clean.charAt(idx-1), noisy.charAt(idx));
        }

        String restOfNoisy = noisy.substring(idx + 1);
        String restOfClean = clean.substring(idx + 1);
        if (restOfNoisy.length() == restOfClean.length()) {
            if (restOfNoisy.equals(restOfClean)) {
//                    System.out.println("SUBSTITUTION: ");
//                    System.out.println("    C: " + clean.substring(0, idx) + "[" + clean.substring(idx, idx + 1) + "]" + restOfClean);
//                    System.out.println("    N: " + noisy.substring(0, idx) + "[" + noisy.substring(idx, idx + 1) + "]" + restOfNoisy);
//                    System.out.println("       " + clean.substring(idx, idx+1) + " --> " + noisy.substring(idx, idx+1));

                return new Edit(EditType.SUBSTITUTION, clean.charAt(idx), noisy.charAt(idx));
            } else if (restOfNoisy.length() > 0) {
                String restOfRestN = restOfNoisy.substring(1);
                String restOfRestC = restOfClean.substring(1);
                if (restOfRestN.equals(restOfRestC)) {
//                        System.out.println("TRANSPOTITION: ");
//                        System.out.println("    C: " + clean.substring(0, idx) + "[" + clean.substring(idx, idx+2) + "]" + restOfRestC);
//                        System.out.println("    N: " + noisy.substring(0, idx) + "[" + noisy.substring(idx, idx+2) + "]" + restOfRestN);
//                        System.out.println("       " + clean.substring(idx, idx+2) + " --> " + noisy.substring(idx, idx+2));

                    return new Edit(EditType.TRANSPOSITION, clean.charAt(idx), clean.charAt(idx+1));
                } else {
//                        System.out.println("I DONT UNDERSTAND:");
//                        System.out.println("   C: " + clean);
//                        System.out.println("   N: " + noisy);
//                        pause();
                }
            }
        } else if (restOfNoisy.length() < restOfClean.length()) {
            if (noisy.substring(idx).equals(restOfClean)) {
//                    System.out.println("DELETION: ");
//                    System.out.println("    C: " + clean);
//                    System.out.println("    N: " + noisy.substring(0, idx) + "*" + noisy.substring(idx));

                if (idx == 0) {
//                        System.out.println("       $" + clean.substring(idx, idx + 1) + " --> $");

                    return new Edit(EditType.DELETION, BEGIN_CHAR, clean.charAt(idx));
                } else {
//                        System.out.println("       " + clean.substring(idx - 1, idx + 1) + " --> " + clean.substring(idx-1, idx));

                    return new Edit(EditType.DELETION, clean.charAt(idx - 1), clean.charAt(idx));
                }
            } else {
//                    System.out.println("I DONT UNDERSTAND:");
//                    System.out.println("   C: " + clean);
//                    System.out.println("   N: " + noisy);
//                    pause();
            }
        } else {
            if (clean.substring(idx).equals(restOfNoisy)) {
//                    System.out.println("INSERTION: ");
//                    System.out.println("    C: " + clean.substring(0, idx) + "*" + clean.substring(idx));
//                    System.out.println("    N: " + noisy);
                if (idx == 0) {
//                        System.out.println("       $" + " --> $" + noisy.substring(idx, idx + 1));

                    return new Edit(EditType.INSERTION, BEGIN_CHAR, noisy.charAt(idx));
                } else {
//                        System.out.println("       " + clean.substring(idx - 1, idx) + " --> " + noisy.substring(idx-1, idx + 1));

                    return new Edit(EditType.INSERTION, clean.charAt(idx-1), noisy.charAt(idx));
                }
            } else {
//                System.out.println("I DONT UNDERSTAND:");
//                System.out.println("   C: " + clean);
//                System.out.println("   N: " + noisy);
//                pause();
            }
        }

        // should never reach here
        System.err.println("Shouldn't reach here.");
        return null;
    }

    private void incrementEditCount(char x, char y, Map<Pair<Character, Character>, Integer> counts) {
        // transform x, y to proper class
        char xClass = characterClass(x);
        char yClass = characterClass(y);

        // increment corresponding count
        Pair<Character, Character> p = new Pair<Character, Character>(xClass, yClass);
        CounterUtility.incrementCount(p, counts);
    }

    private void loadAlphabet() {
        alphabet = new HashSet<Character>();
        for (Character c : CandidateGenerator.alphabet) {
            alphabet.add(c);
        }

        // add begin of sentence meta character
        alphabet.add(BEGIN_CHAR);

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

    static void pause() {
        try {
            // wait for human judgement
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        Edit edit = determineEdit(Q, R);
        char x = characterClass(edit.x);
        char y = characterClass(edit.y);
        Pair<Character, Character> p = new Pair<Character, Character>(x, y);

        StringBuilder bigram = new StringBuilder("@@");
        bigram.setCharAt(0, x);
        bigram.setCharAt(1, y);

        double count = 1;
        double total = 2;

        if (edit.type == EditType.DELETION) {
            count = deletionCounts.get(p);
            total = bigramCounts.get(bigram.toString());
        } else if (edit.type == EditType.INSERTION) {
            count = insertionCounts.get(p);
            total = unigramCounts.get(x);
        } else if (edit.type == EditType.SUBSTITUTION) {
            count = substitutionCounts.get(p);
            total = unigramCounts.get(x);
        } else if (edit.type == EditType.TRANSPOSITION) {
            count = transpositionCounts.get(p);
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

    /*
     * helper enum and classes
     */
    private static enum EditType {
        DELETION, INSERTION, SUBSTITUTION, TRANSPOSITION
    }

    private static class Edit {
        private EditType type;
        private char x;
        private char y;

        public Edit(EditType t, char x, char y) {
            this.type = t;
            this.x = x;
            this.y = y;
        }
    }
}
