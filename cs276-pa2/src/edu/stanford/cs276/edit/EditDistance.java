package edu.stanford.cs276.edit;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of Damerau–Levenshtein edit distance.
 * Created by kavinyao on 4/28/14.
 */
public class EditDistance {
    private static List<Edit> EMPTY = new ArrayList<Edit>();
    // meta character representing the beginning of a sentence
    public static char BEGIN_CHAR = '$';

    private EditDistance() {}

    /**
     * Determine minimal edits which transforms clean to noisy. If there are
     * more than one possibility, return any of them.
     * Damerau–Levenshtein edit distance algorithm is implemented with uniform
     * cost for every kind of edit.
     *
     * @param clean the original string
     * @param noisy the transformed string
     * @return non-null list with size 0 indicating no edits happened
     */
    public static List<Edit> determineEdits(final String clean, final String noisy) {
        if (clean.equals(noisy)) {
            // don't waste time
            return EMPTY;
        }

        final int N = clean.length();
        final int M = noisy.length();

        // allocate and initialize DP and backtrace matrix
        int[][] D = new int[N+1][];
        char[][] B = new char[N+1][];
        for (int i = 0; i <= N; ++i) {
            D[i] = new int[M+1];
            B[i] = new char[M+1];
        }

        for (int i = 0; i <= N; ++i) {
            D[i][0] = i;
            B[i][0] = 'D';
        }

        for (int i = 0; i <= M; ++i) {
            D[0][i] = i;
            B[0][i] = 'I';
        }

        B[0][0] = 'N';

        // 1. run DP to determine minimal #edits
        // store edits in B
        for (int i = 1; i <= N; ++i) {
            for (int j = 1; j <= M; ++j) {
                // a. compute min(deletion, insertion, substitution) first
                if (clean.charAt(i-1) == noisy.charAt(j-1)) {
                    D[i][j] = D[i-1][j-1];
                    B[i][j] = 'N';
                } else {
                    int deletionCost = D[i-1][j] + 1;
                    int insertionCost = D[i][j-1] + 1;
                    if (deletionCost <= insertionCost) {
                        D[i][j] = deletionCost;
                        B[i][j] = 'D';
                    } else {
                        D[i][j] = insertionCost;
                        B[i][j] = 'I';
                    }

                    int substitutionCost = D[i-1][j-1] + 1;
                    if (substitutionCost < D[i][j]) {
                        D[i][j] = substitutionCost;
                        B[i][j] = 'S';
                    }
                }

                // b. consider transposition if possible
                if (i > 1 && j > 1
                        && (clean.charAt(i-1) == noisy.charAt(j-2))
                        && (clean.charAt(i-2) == noisy.charAt(j-1))) {
                    int transpositionCost = D[i-2][j-2] + 1;
                    if (transpositionCost < D[i][j]) {
                        D[i][j] = transpositionCost;
                        B[i][j] = 'T';
                    }
                }
            }
        }

        // 2. backtrace to determine which edits happened
        List<Edit> edits = new ArrayList<Edit>();
        int i = N, j = M;
        while (i > -1 && j > -1) {
            if (B[i][j] == 'I') {
                char x = i > 0 ? clean.charAt(i-1) : BEGIN_CHAR;
                char y = noisy.charAt(j-1);
                edits.add(new Edit(EditType.INSERTION, x, y));

                --j;
            } else if (B[i][j] == 'D') {
                char x = i > 1 ? clean.charAt(i-2) : BEGIN_CHAR;
                char y = clean.charAt(i-1);
                edits.add(new Edit(EditType.DELETION, x, y));

                --i;
            } else if (B[i][j] == 'T') {
                char x = clean.charAt(i-2);
                char y = clean.charAt(i-1);
                edits.add(new Edit(EditType.TRANSPOSITION, x, y));

                i -= 2;
                j -= 2;
            } else {
                if (B[i][j] == 'S') {
                    char x = clean.charAt(i-1);
                    char y = noisy.charAt(j-1);
                    edits.add(new Edit(EditType.SUBSTITUTION, x, y));
                }
                --i;
                --j;
            }
        }

        return edits;
    }

    /**
     * Return the edit type and involved characters.
     * Assume at most 1 edit between clean and noisy.
     *
     * @param clean the correct value
     * @param noisy the corrupted value
     * @return null if no edit, or an Edit instance
     */
    public static Edit determineOneEdit(String clean, String noisy) {
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
            return new Edit(EditType.DELETION, clean.charAt(idx-1), clean.charAt(idx));
        }

        if (idx == clean.length()) {
            return new Edit(EditType.INSERTION, clean.charAt(idx-1), noisy.charAt(idx));
        }

        String restOfNoisy = noisy.substring(idx + 1);
        String restOfClean = clean.substring(idx + 1);
        if (restOfNoisy.length() == restOfClean.length()) {
            if (restOfNoisy.equals(restOfClean)) {
                return new Edit(EditType.SUBSTITUTION, clean.charAt(idx), noisy.charAt(idx));
            } else if (restOfNoisy.length() > 0) {
                String restOfRestN = restOfNoisy.substring(1);
                String restOfRestC = restOfClean.substring(1);
                if (restOfRestN.equals(restOfRestC)) {
                    return new Edit(EditType.TRANSPOSITION, clean.charAt(idx), clean.charAt(idx+1));
                } else {
//                    System.out.println("I DONT UNDERSTAND:");
//                    System.out.println("   C: " + clean);
//                    System.out.println("   N: " + noisy);
//                    pause();
                }
            }
        } else if (restOfNoisy.length() < restOfClean.length()) {
            if (noisy.substring(idx).equals(restOfClean)) {
                if (idx == 0) {
                    return new Edit(EditType.DELETION, BEGIN_CHAR, clean.charAt(idx));
                } else {
                    return new Edit(EditType.DELETION, clean.charAt(idx - 1), clean.charAt(idx));
                }
            } else {
//                System.out.println("I DONT UNDERSTAND:");
//                System.out.println("   C: " + clean);
//                System.out.println("   N: " + noisy);
//                pause();
            }
        } else {
            if (clean.substring(idx).equals(restOfNoisy)) {
                if (idx == 0) {
                    return new Edit(EditType.INSERTION, BEGIN_CHAR, noisy.charAt(idx));
                } else {
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
        // System.err.println("Shouldn't reach here.");
        // System.out.println(clean + " " + noisy);
        return null;
    }

    /**
     * Compute Damerau–Levenshtein edit distance between s and t.
     */
    public static int editDistance(String s, String t) {
        final int N = s.length();
        final int M = t.length();

        // allocate and initialize DP and backtrace matrix
        int[][] D = new int[N+1][];
        for (int i = 0; i <= N; ++i) {
            D[i] = new int[M+1];
        }

        for (int i = 0; i <= N; ++i) {
            D[i][0] = i;
        }

        for (int i = 0; i <= M; ++i) {
            D[0][i] = i;
        }

        // 1. run DP to determine minimal #edits
        // store edits in B
        for (int i = 1; i <= N; ++i) {
            for (int j = 1; j <= M; ++j) {
                // a. compute min(deletion, insertion, substitution) first
                if (s.charAt(i-1) == t.charAt(j-1)) {
                    D[i][j] = D[i-1][j-1];
                } else {
                    int deletionCost = D[i-1][j] + 1;
                    int insertionCost = D[i][j-1] + 1;
                    if (deletionCost <= insertionCost) {
                        D[i][j] = deletionCost;
                    } else {
                        D[i][j] = insertionCost;
                    }

                    int substitutionCost = D[i-1][j-1] + 1;
                    if (substitutionCost < D[i][j]) {
                        D[i][j] = substitutionCost;
                    }
                }

                // b. consider transposition if possible
                if (i > 1 && j > 1
                        && (s.charAt(i-1) == t.charAt(j-2))
                        && (s.charAt(i-2) == t.charAt(j-1))) {
                    int transpositionCost = D[i-2][j-2] + 1;
                    if (transpositionCost < D[i][j]) {
                        D[i][j] = transpositionCost;
                    }
                }
            }
        }

        return D[N][M];
    }

    public static void main(String[] args) {
        // no edit
        determineEdits("abcd", "abcd");
        // substitution at beginning
        determineEdits("abcd", "bbcd");
        // substitution not at beginning
        determineEdits("abcd", "accd");
        // deletion at beginning
        determineEdits("abcd", "bcd");
        // deletion not at beginning
        determineEdits("abcd", "acd");
        // insertion at beginning
        determineEdits("abcd", "aabcd");
        // insertion not at beginning
        determineEdits("abcd", "axbcd");
        // deletion and tranposition
        determineEdits("cs276 query", "cs276qeury");
        // 3 substitutions?
        determineEdits("on facebook share on twitter", "on face ppk share on twitter");
    }
}
