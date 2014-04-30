package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class RunCorrector {

    public static LanguageModel languageModel;
    public static NoisyChannelModel nsm;
    public static CandidateGenerator cg;

    private static double mu = 1.5;


    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        // Parse input arguments
        String uniformOrEmpirical = null;
        String queryFilePath = null;
        String goldFilePath = null;
        String extra = null;
        BufferedReader goldFileReader = null;
        if (args.length == 2) {
            // Run without extra and comparing to gold
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
        }
        else if (args.length == 3) {
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
            if (args[2].equals("extra")) {
                extra = args[2];
            } else {
                goldFilePath = args[2];
            }
        }
        else if (args.length == 4) {
            uniformOrEmpirical = args[0];
            queryFilePath = args[1];
            extra = args[2];
            goldFilePath = args[3];
        }
        else {
            System.err.println(
                    "Invalid arguments.  Argument count must be 2, 3 or 4" +
                    "./runcorrector <uniform | empirical> <query file> \n" +
                    "./runcorrector <uniform | empirical> <query file> <gold file> \n" +
                    "./runcorrector <uniform | empirical> <query file> <extra> \n" +
                    "./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n" +
                    "SAMPLE: ./runcorrector empirical data/queries.txt \n" +
                    "SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n" +
                    "SAMPLE: ./runcorrector empirical data/queries.txt extra \n" +
                    "SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
            return;
        }

        if (goldFilePath != null ){
            goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
        }

        if (uniformOrEmpirical.equals("empirical")) {
            mu = 1.5;
        } else {
            mu = 0.5;
        }

        // Load models from disk
        System.out.println("Loading language model...");
        languageModel = LanguageModel.load();
        System.out.println("-- Completed!");
        System.out.println("Loading noisy channel model...");
        nsm = NoisyChannelModel.load();
        System.out.println("-- Completed!");

        BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
        nsm.setProbabilityType(uniformOrEmpirical);

        // Load candidate generator
        cg = CandidateGenerator.get();

        int totalCount = 0;
        int yourCorrectCount = 0;
        String query = null;

        /*
         * Each line in the file represents one query.  We loop over each query and find
         * the most likely correction
         */
        double maxSoFar, prob;
        while ((query = queriesFileReader.readLine()) != null) {

            Set<String> candidates = cg.getCandidates(query, languageModel);
            String correctedQuery = query;
            maxSoFar = Double.NEGATIVE_INFINITY;
            for (String s : candidates) {
                if (uniformOrEmpirical.equals("empirical")) {
                    prob = nsm.ecm_.editProbability(query, s, 1);
                } else {
                    prob = nsm.ecm_.editProbability(query, s, editDistance(query, s));
                }
                prob += languageModel.computeProbability(s) * mu;
//                System.out.format("%s, %f\n", s, prob);
                if (prob > maxSoFar) {
                    maxSoFar = prob;
                    correctedQuery = s;
                }
            }

            if ("extra".equals(extra)) {
                /*
                 * If you are going to implement something regarding to running the corrector,
                 * you can add code here. Feel free to move this code block to wherever
                 * you think is appropriate. But make sure if you add "extra" parameter,
                 * it will run code for your extra credit and it will run you basic
                 * implementations without the "extra" parameter.
                 */
            }


            // If a gold file was provided, compare our correction to the gold correction
            // and output the running accuracy
            if (goldFileReader != null) {
                String goldQuery = goldFileReader.readLine();
                if (goldQuery.equals(correctedQuery)) {
                    System.out.format("%d √: %s\n", totalCount, correctedQuery);
                    yourCorrectCount++;
                } else {
                    System.out.format("%d x: %s -> %s -> %s\n", totalCount, query, correctedQuery, goldQuery);
                }
                totalCount++;
            } else {
                System.out.format("%d: %s\n", totalCount, correctedQuery);
            }
        }
        queriesFileReader.close();
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println(yourCorrectCount);
        System.out.println(1.0 * yourCorrectCount / totalCount);
        System.out.println("RUNNING TIME: "+totalTime/1000+" seconds ");
    }

    private static int editDistance(String s, String t) {
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

}
