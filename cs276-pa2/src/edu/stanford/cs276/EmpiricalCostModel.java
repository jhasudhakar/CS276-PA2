package edu.stanford.cs276;

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

    // meta character representing the beginning of a sentence
    private static char BEGIN_CHAR = '$';

    // all characters not present in the pre-defined alphabet
    // will be mapped to this character
    private char unknownCharacter;

    // counts of different edits
    private Map<Pair<Character, Character>, Integer> insertionCounts;
    private Map<Pair<Character, Character>, Integer> deletionCounts;
    private Map<Pair<Character, Character>, Integer> substitutionCounts;
    private Map<Pair<Character, Character>, Integer> transpositionCounts;

    // for convenience (see incrementCount)
    private static final Integer ZERO = 0;

    public EmpiricalCostModel(String editsFile) throws IOException {
        loadAlphabet();

        // initialize counts
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

            if (noisy.equals(clean)) {
                // wow, no need to process
                continue;
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

                incrementCount(clean.charAt(idx-1), clean.charAt(idx), deletionCounts);
                continue;
            }

            if (idx == clean.length()) {
//                System.out.println("INSERTION: ");
//                System.out.println("    C: " + clean + "*");
//                System.out.println("    N: " + noisy);
//                System.out.println("       " + clean.substring(idx-1) + " --> " + noisy.substring(idx-1));

                incrementCount(clean.charAt(idx-1), noisy.charAt(idx), insertionCounts);
                continue;
            }

            String restOfNoisy = noisy.substring(idx + 1);
            String restOfClean = clean.substring(idx + 1);
            if (restOfNoisy.length() == restOfClean.length()) {
                if (restOfNoisy.equals(restOfClean)) {
//                    System.out.println("SUBSTITUTION: ");
//                    System.out.println("    C: " + clean.substring(0, idx) + "[" + clean.substring(idx, idx + 1) + "]" + restOfClean);
//                    System.out.println("    N: " + noisy.substring(0, idx) + "[" + noisy.substring(idx, idx + 1) + "]" + restOfNoisy);
//                    System.out.println("       " + clean.substring(idx, idx+1) + " --> " + noisy.substring(idx, idx+1));

                    incrementCount(clean.charAt(idx), noisy.charAt(idx), substitutionCounts);
                } else if (restOfNoisy.length() > 0) {
                    String restOfRestN = restOfNoisy.substring(1);
                    String restOfRestC = restOfClean.substring(1);
                    if (restOfRestN.equals(restOfRestC)) {
//                        System.out.println("TRANSPOTITION: ");
//                        System.out.println("    C: " + clean.substring(0, idx) + "[" + clean.substring(idx, idx+2) + "]" + restOfRestC);
//                        System.out.println("    N: " + noisy.substring(0, idx) + "[" + noisy.substring(idx, idx+2) + "]" + restOfRestN);
//                        System.out.println("       " + clean.substring(idx, idx+2) + " --> " + noisy.substring(idx, idx+2));

                        incrementCount(clean.charAt(idx), clean.charAt(idx+1), transpositionCounts);
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

                        incrementCount(BEGIN_CHAR, clean.charAt(idx), deletionCounts);
                    } else {
//                        System.out.println("       " + clean.substring(idx - 1, idx + 1) + " --> " + clean.substring(idx-1, idx));

                        incrementCount(clean.charAt(idx - 1), clean.charAt(idx), deletionCounts);
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

                        incrementCount(BEGIN_CHAR, noisy.charAt(idx), insertionCounts);
                    } else {
//                        System.out.println("       " + clean.substring(idx - 1, idx) + " --> " + noisy.substring(idx-1, idx + 1));

                        incrementCount(clean.charAt(idx-1), noisy.charAt(idx), insertionCounts);
                    }
                } else {
                    System.out.println("I DONT UNDERSTAND:");
                    System.out.println("   C: " + clean);
                    System.out.println("   N: " + noisy);
                    pause();
                }
            }
        }

        input.close();
        System.out.println("Done.");
    }

    private void incrementCount(char x, char y, Map<Pair<Character, Character>, Integer> counts) {
        // transform x, y to proper class
        char xClass = characterClass(x);
        char yClass = characterClass(y);

        // increment corresponding count
        Pair<Character, Character> p = new Pair<Character, Character>(xClass, yClass);
        Integer currentCount = counts.get(p);
        currentCount = currentCount == null ? ZERO : currentCount;
        counts.put(p, currentCount + 1);
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
        return 0.5;
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        EmpiricalCostModel model = new EmpiricalCostModel(args[0]);
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("RUNNING TIME: "+totalTime/1000+" seconds ");
    }
}
