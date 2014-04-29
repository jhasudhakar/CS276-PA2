package edu.stanford.cs276;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of Damerau–Levenshtein edit distance.
 * Created by kavinyao on 4/28/14.
 */
public class EditDistance {
    private static List<Edit> EMPTY = new ArrayList<Edit>();

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

        // run DP to determine minimal edits
        for (int i = 1; i <= N; ++i) {
            for (int j = 1; j <= M; ++j) {
                // 1. compute min(deletion, insertion, substitution) first
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

                // 2. compute transposition if possible
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

        System.out.println(String.format("ED(%s, %s) = %d", clean, noisy, D[N][M]));

        int i = N, j = M;
        while (i > -1 && j > -1) {
            System.out.println("   "  + i + ", " + j + ": " + B[i][j]);
            if (B[i][j] == 'I') {
                --j;
            } else if (B[i][j] == 'D') {
                --i;
            } else if (B[i][j] == 'T') {
                i -= 2;
                j -= 2;
            } else {
                --i;
                --j;
            }
        }

        List<Edit> edits = new ArrayList<Edit>();
        return edits;
    }

    public static void main(String[] args) {
        determineEdits("abcd", "abcd");
        determineEdits("abcd", "accd");
        determineEdits("abcd", "acd");
        determineEdits("abcd", "aabcd");
        determineEdits("abcd", "acbd");
        determineEdits("cs276 query", "cs276qeury");
        determineEdits("on facebook share on twitter", "on face ppk share on twitter");
    }
}
